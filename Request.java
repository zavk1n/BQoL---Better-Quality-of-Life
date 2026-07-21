package com.zavk1n.bqol.utils.liteapi;

import java.util.UUID;

public abstract class Request {
    private final String id;
    private final String method;

    protected Request(String method) {
        this.id = UUID.randomUUID().toString();
        this.method = method;
    }

    public String id() {
        return id;
    }

    public String method() {
        return method;
    }
}
// v1.0