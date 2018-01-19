package com.productivepathway.liquibase;

import liquibase.*;
import liquibase.database.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;

import org.apache.log4j.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;

public class Lambda implements RequestHandler<Request, Response> {

    public Response handleRequest(Request request, Context context) {
        logger.info("Request: " + request);
        Response response = new Response();
        long startTime = System.currentTimeMillis();
        try {
            request.validate();
            String s3Key = request.getPath();//S3 key prefix to directory. Expects jdbc.properties and changelog.xml
            logger.info("Updating from " + s3Key);

            AmazonS3 s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
            File downloadDir = new File("/tmp/download");//Lambda can only create under /tmp
            downloadDir.mkdirs();
            downloadS3(s3Client, s3Key, downloadDir);
            
            Properties connectionProperties = new Properties();
            connectionProperties.load(new FileInputStream(new File(downloadDir, "jdbc.properties")));
            Connection connection = DriverManager.getConnection(connectionProperties.getProperty("url"), connectionProperties);

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(downloadDir.getPath() + "/changelog.xml", new FileSystemResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());

            response.setSuccess(true);
        } catch (Throwable e) {
            logger.error("Unable to process " + request, e);
            response.addError(e.toString());
        }
        return response;
    }

    private static Logger logger = Logger.getLogger(Lambda.class);
    private final static String BUCKET_NAME = "ppi-liquibase";

    private void downloadS3(AmazonS3 s3client, String keyPrefix, File localDirectory) throws IOException, AmazonClientException {
        logger.info("Downloading " + BUCKET_NAME + ":" + keyPrefix + " to " + localDirectory);
        final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(BUCKET_NAME);//.withPrefix(keyPrefix);
        ListObjectsV2Result result;
        do {
            result = s3client.listObjectsV2(req);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                if(objectSummary.getKey().endsWith("/")) {
                    logger.debug("Skipping folder: " + objectSummary.getKey());
                    continue;
                }
                logger.info("Downloading " + objectSummary.getKey());
                S3Object s3Object = s3client.getObject(new GetObjectRequest(BUCKET_NAME, objectSummary.getKey()));
                InputStream inputStream = s3Object.getObjectContent();
                String localRelativePath = objectSummary.getKey().substring(keyPrefix.length());
                File local = new File(localDirectory, localRelativePath);
                logger.debug("Downloading to " + local);
                BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(local));

                int read;
                byte[] buffer = new byte[4096];
                while((read = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                    fos.write(buffer, 0, read);
                }
                s3Object.close();
                fos.close();
                logger.debug("Downloaded " + local);
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while(result.isTruncated());
        logger.info("Finished download");
    }
}

