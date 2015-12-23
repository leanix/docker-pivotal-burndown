package net.leanix.pivotal.burndown.api;

import com.sun.jersey.api.client.ClientResponse;

public class ApiException extends Exception {

    int code = 0;

    public ApiException() {
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(int code, String message, Exception cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(getCode()).append(' ')
                .append(ClientResponse.Status.fromStatusCode(getCode()).getReasonPhrase()).append(") ");
        sb.append(super.getMessage());
        return sb.toString();
    }
}
