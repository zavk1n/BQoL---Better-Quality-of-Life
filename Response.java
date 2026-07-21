package com.zavk1n.bqol.utils.liteapi;

public abstract class Response {
    private final String id;
    private final boolean ok;
    private final String error;
    private final String message;

    protected Response(String id, boolean ok, String error, String message) {
        this.id = id;
        this.ok = ok;
        this.error = error;
        this.message = message;
    }

    public String id() {
        return id;
    }

    public boolean isOk() {
        return ok;
    }

    public String error() {
        return error;
    }

    public String message() {
        return message;
    }
}
// v1.0