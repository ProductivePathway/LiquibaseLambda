package com.productivepathway.liquibase;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.log4j.*;

public class Request {
    protected Logger logger = Logger.getLogger(getClass().getName());
    private String path;
    private String documentationPath;//Optional: Path to which documentation should be written

    public Request(String path) {
        this.path = decode(path);
    }

    public Request() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = decode(path);
    }

    public String getDocumentationPath() {
        return documentationPath;
    }

    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = decode(documentationPath);
    }

    public void validate() {
        require("path", path);
    }

    public String toString() {
        return getClass().getSimpleName() + ":[" + path
            + (documentationPath != null ? (" documentationPath=" + documentationPath) : "")
            + "]";
    }

    protected void require(String name, String value) {
        if(!isSet(value)) {
            throw new IllegalArgumentException(name + " mandatory");
        }
    }

    public static boolean isSet(String value) {
        return value != null && value.length() > 0;
    }

    protected String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch(UnsupportedEncodingException uee) {
            logger.error("Unable to decode " + value, uee);
            return value;
        }
    }
}

