package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public final class SchemaException extends IllegalArgumentException {

    private final String code;
    private final SourceLocation location;

    public SchemaException(String code, SourceLocation location, String message) {
        super(Objects.requireNonNull(location, "location").display()
                + " ["
                + Objects.requireNonNull(code, "code")
                + "] "
                + Objects.requireNonNull(message, "message"));
        this.code = code;
        this.location = location;
    }

    public String code() {
        return code;
    }

    public SourceLocation location() {
        return location;
    }
}
