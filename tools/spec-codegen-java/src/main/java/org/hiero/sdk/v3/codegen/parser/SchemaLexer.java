package org.hiero.sdk.v3.codegen.parser;

import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public final class SchemaLexer {

    private final ExtractedSchema schema;
    private final String content;
    private final List<Token> tokens = new ArrayList<>();
    private int index;
    private int line;
    private int column = 1;

    public SchemaLexer(ExtractedSchema schema) {
        this.schema = schema;
        this.content = schema.content();
        this.line = schema.schemaStartLine();
    }

    public List<Token> tokenize() {
        while (!atEnd()) {
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", location()));
        return List.copyOf(tokens);
    }

    private void scanToken() {
        var current = peek();
        if (current == ' ' || current == '\t' || current == '\f') {
            advance();
            return;
        }
        if (current == '\n') {
            var location = location();
            advance();
            tokens.add(new Token(TokenType.NEWLINE, "\n", location));
            return;
        }
        if (match("//")) {
            scanComment();
            return;
        }
        if (match("@@")) {
            scanNamedToken(TokenType.ANNOTATION, "annotation");
            return;
        }
        if (match("$$")) {
            scanNamedToken(TokenType.TYPE_VARIABLE, "type variable");
            return;
        }
        if (match("...")) {
            add(TokenType.ELLIPSIS, "...", 3);
            return;
        }
        if (current == '"') {
            scanString();
            return;
        }
        if (Character.isDigit(current) || (current == '-' && Character.isDigit(peek(1)))) {
            scanNumber();
            return;
        }
        if (isIdentifierStart(current)) {
            scanIdentifier();
            return;
        }

        var type = switch (current) {
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '<' -> TokenType.LANGLE;
            case '>' -> TokenType.RANGLE;
            case ':' -> TokenType.COLON;
            case ',' -> TokenType.COMMA;
            case '=' -> TokenType.EQUALS;
            case '.' -> TokenType.DOT;
            default -> null;
        };
        if (type == null) {
            throw new SchemaException(
                    "UNSUPPORTED_SYNTAX", location(), "unsupported character '" + current + "'");
        }
        add(type, String.valueOf(current), 1);
    }

    private void scanComment() {
        var start = location();
        advance(2);
        var textStart = index;
        while (!atEnd() && peek() != '\n') {
            advance();
        }
        tokens.add(new Token(TokenType.COMMENT, content.substring(textStart, index).strip(), start));
    }

    private void scanNamedToken(TokenType type, String description) {
        var start = location();
        advance(2);
        var nameStart = index;
        while (!atEnd() && isIdentifierPart(peek())) {
            advance();
        }
        if (nameStart == index) {
            throw new SchemaException("UNSUPPORTED_SYNTAX", start, "missing " + description + " name");
        }
        tokens.add(new Token(type, content.substring(nameStart, index), start));
    }

    private void scanIdentifier() {
        var start = location();
        var begin = index;
        advance();
        while (!atEnd() && (isIdentifierPart(peek()) || peek() == '-')) {
            advance();
        }
        tokens.add(new Token(TokenType.IDENTIFIER, content.substring(begin, index), start));
    }

    private void scanNumber() {
        var start = location();
        var begin = index;
        if (peek() == '-') {
            advance();
        }
        while (!atEnd() && (Character.isDigit(peek()) || peek() == '_')) {
            advance();
        }
        tokens.add(new Token(TokenType.NUMBER, content.substring(begin, index), start));
    }

    private void scanString() {
        var start = location();
        advance();
        var value = new StringBuilder();
        while (!atEnd() && peek() != '"') {
            if (peek() == '\n') {
                throw new SchemaException("UNTERMINATED_STRING", start, "string literal crosses a line boundary");
            }
            if (peek() == '\\') {
                advance();
                if (atEnd()) {
                    break;
                }
                value.append(switch (peek()) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> peek();
                });
                advance();
            } else {
                value.append(peek());
                advance();
            }
        }
        if (atEnd()) {
            throw new SchemaException("UNTERMINATED_STRING", start, "string literal is not closed");
        }
        advance();
        tokens.add(new Token(TokenType.STRING, value.toString(), start));
    }

    private void add(TokenType type, String lexeme, int length) {
        var start = location();
        advance(length);
        tokens.add(new Token(type, lexeme, start));
    }

    private boolean match(String value) {
        return content.startsWith(value, index);
    }

    private char peek() {
        return peek(0);
    }

    private char peek(int offset) {
        var target = index + offset;
        return target < content.length() ? content.charAt(target) : '\0';
    }

    private void advance() {
        var consumed = content.charAt(index++);
        if (consumed == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
    }

    private void advance(int count) {
        for (var offset = 0; offset < count; offset++) {
            advance();
        }
    }

    private boolean atEnd() {
        return index >= content.length();
    }

    private SourceLocation location() {
        return new SourceLocation(schema.sourcePath(), line, column);
    }

    private static boolean isIdentifierStart(char value) {
        return Character.isLetter(value) || value == '_';
    }

    private static boolean isIdentifierPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }
}
