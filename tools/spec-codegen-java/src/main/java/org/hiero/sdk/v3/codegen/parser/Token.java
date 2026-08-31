package org.hiero.sdk.v3.codegen.parser;

import java.util.Objects;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public record Token(TokenType type, String lexeme, SourceLocation location) {

    public Token {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(lexeme, "lexeme");
        Objects.requireNonNull(location, "location");
    }
}
