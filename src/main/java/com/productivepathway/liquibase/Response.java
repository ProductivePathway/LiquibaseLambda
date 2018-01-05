package com.productivepathway.liquibase;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private List<String> errors;
    private boolean success;

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        if(errors == null) {
            errors = new ArrayList<String>();
        }
        errors.add(error);
        success = false;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Response(List<String> errors) {
        this.errors = errors;
    }

    public Response() {
    }

    public String toString() {
        return "Response: ["
            + (success ? "success" : "")
            + (errors != null ? ("errors=" + errors) : "")
            + "]";
    }
}

