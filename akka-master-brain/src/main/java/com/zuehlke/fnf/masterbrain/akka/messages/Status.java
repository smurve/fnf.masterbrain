package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by tho on 06.08.2015.
 */
public class Status {
    private Code code;
    private String message;

    public Status() {

    }

    public Status(Code code, String message) {
        this.code = code;
        this.message = message;
    }

    public static Status from(String code, String message) {
        return new Status(Code.valueOf(code), message);
    }

    public Code getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    @JsonIgnore
    public boolean isError() {
        return Code.error.equals(code);
    }
    @JsonIgnore
    public boolean isOk() {
        return Code.ok.equals(code);
    }
    @JsonIgnore
    public boolean isWarning() {
        return Code.warning.equals(code);
    }

    @Override
    public String toString() {
        return "Status{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    public static Status ok() {
        return new Status(Code.ok, "ok");
    }

    public enum Code {ok, error, warning}
}
