package com.zavk1n.bqol.utils.liteapi;

import java.util.Collections;
import java.util.List;

public final class CheckFeaturesResponse extends Response {

    private final List<String> blocklist;

    private CheckFeaturesResponse(String id, boolean ok, List<String> blocklist, String error, String message) {
        super(id, ok, error, message);
        this.blocklist = blocklist;
    }

    public static CheckFeaturesResponse success(String id, List<String> blocklist) {
        return new CheckFeaturesResponse(
            id,
            true,
            Collections.unmodifiableList(blocklist),
            null,
            null
        );
    }

    public static CheckFeaturesResponse failure(String id, String error, String message) {
        return new CheckFeaturesResponse(
            id,
            false,
            Collections.emptyList(),
            error,
            message
        );
    }

    public List<String> blocklist() {
        return blocklist;
    }

    @Override
    public String toString() {
        if (isOk()) {
            return "CheckFeaturesResponse{id='" + id() + "', ok=true, blocklist=" + blocklist + "}";
        }

        return "CheckFeaturesResponse{id='"
            + id()
            + "', ok=false, error='"
            + error()
            + "', message='"
            + message()
            + "'}";
    }
}
// v1.0