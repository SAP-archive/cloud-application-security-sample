package com.sap.cp.appsec.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Common structure for an Error Response.
 */
@JsonTypeName("error")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class ErrorDto {
    private HttpStatus status;
    private String message; // user-facing (localizable) message, describing the error
    private String target; // endpoint of origin request
    private List<DetailError> details;

    public ErrorDto(HttpStatus status, String message, WebRequest request, DetailError... errors) {
        this.status = status;
        this.message = message;
        if (message == null) {
            this.message = status.getReasonPhrase();
        }
        this.details = Arrays.asList(errors);
        this.target = request.getDescription(false).substring(4);
    }

    public int getStatus() {
        return status.value();
    }

    public String getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

    public List<DetailError> getDetails() {
        return details;
    }

    public static class DetailError {
        private final String message; // user-facing (localizable) message, describing the error

        public DetailError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}