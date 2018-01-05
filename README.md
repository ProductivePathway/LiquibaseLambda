# LiquibaseLambda
AWS Lambda for applying liquibase

Setup
1. Edit pom.xml to use the correct JDBC driver. (It defaults to postgreSQL)
2. Edit BUCKET_NAME in Lambda.java (Defaults to safetyDb-liquibase). This should be a private S3 bucket
3. Compile: mvn package
4. Create the AWS Lambda function. Give it rights to read from the S3 bucket and connect to the DB.
5. Put your connection.properties and changelog.xml files in the S3 bucket (probably under a folder).
6. Call the Lambda with the path parameter set to the S3 key path to the folder.