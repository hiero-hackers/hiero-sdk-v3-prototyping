package org.hiero.sdk.v3.codegen.model;

import java.util.Arrays;

public enum PrimitiveKind {
    BOOL("bool"),
    INT32("int32"),
    INT64("int64"),
    UINT16("uint16"),
    UINT64("uint64"),
    DOUBLE("double"),
    STRING("string"),
    BYTES("bytes"),
    ZONED_DATE_TIME("zonedDateTime"),
    VOID("void");

    private final String schemaName;

    PrimitiveKind(String schemaName) {
        this.schemaName = schemaName;
    }

    public String schemaName() {
        return schemaName;
    }

    public static PrimitiveKind fromSchemaName(String name) {
        return Arrays.stream(values())
                .filter(value -> value.schemaName.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown primitive: " + name));
    }

    public static boolean isPrimitive(String name) {
        return Arrays.stream(values()).anyMatch(value -> value.schemaName.equals(name));
    }
}
