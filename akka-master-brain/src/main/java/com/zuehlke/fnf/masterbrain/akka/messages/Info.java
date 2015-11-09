package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * Created by tho on 07.08.2015.
 */
public class Info {
    private final String type;
    private final String code;
    private final String message;

    public Info(String type, String code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("type=%s, code=%s, message=%s", type, code, message);
    }
}
