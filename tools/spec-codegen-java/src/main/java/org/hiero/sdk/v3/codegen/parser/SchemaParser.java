package org.hiero.sdk.v3.codegen.parser;

import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.codegen.model.AnnotationUse;
import org.hiero.sdk.v3.codegen.model.CollectionKind;
import org.hiero.sdk.v3.codegen.model.CollectionTypeReference;
import org.hiero.sdk.v3.codegen.model.ConstantDeclaration;
import org.hiero.sdk.v3.codegen.model.Declaration;
import org.hiero.sdk.v3.codegen.model.EnumDeclaration;
import org.hiero.sdk.v3.codegen.model.EnumValue;
import org.hiero.sdk.v3.codegen.model.FieldDeclaration;
import org.hiero.sdk.v3.codegen.model.GenericParameter;
import org.hiero.sdk.v3.codegen.model.ImportDeclaration;
import org.hiero.sdk.v3.codegen.model.MethodDeclaration;
import org.hiero.sdk.v3.codegen.model.NamedTypeReference;
import org.hiero.sdk.v3.codegen.model.NamespaceOperationDeclaration;
import org.hiero.sdk.v3.codegen.model.Parameter;
import org.hiero.sdk.v3.codegen.model.PrimitiveKind;
import org.hiero.sdk.v3.codegen.model.PrimitiveTypeReference;
import org.hiero.sdk.v3.codegen.model.SchemaDocument;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.model.SourceLocation;
import org.hiero.sdk.v3.codegen.model.TypeDeclaration;
import org.hiero.sdk.v3.codegen.model.TypeKind;
import org.hiero.sdk.v3.codegen.model.TypeReference;
import org.hiero.sdk.v3.codegen.model.TypeVariableReference;
import org.hiero.sdk.v3.codegen.model.WildcardTypeReference;

public final class SchemaParser {

    private List<Token> tokens;
    private int current;
    private ExtractedSchema schema;

    public SchemaDocument parse(ExtractedSchema extractedSchema) {
        schema = extractedSchema;
        tokens = new SchemaLexer(extractedSchema).tokenize();
        current = 0;

        readPrelude();
        expectKeyword("namespace");
        var namespace = parseQualifiedName();
        finishLine();

        var imports = new ArrayList<ImportDeclaration>();
        var declarations = new ArrayList<Declaration>();
        while (!check(TokenType.EOF)) {
            var prelude = readPrelude();
            if (check(TokenType.EOF)) {
                break;
            }
            if (checkKeyword("requires")) {
                if (!prelude.annotations().isEmpty()) {
                    throw error(peek(), "UNSUPPORTED_SYNTAX", "imports cannot have annotations");
                }
                imports.add(parseImport());
            } else {
                declarations.add(parseDeclaration(prelude));
            }
        }
        return new SchemaDocument(schema.sourcePath(), namespace, schema.schemaStartLine(), imports, declarations);
    }

    private ImportDeclaration parseImport() {
        var location = expectKeyword("requires").location();
        expect(TokenType.LBRACE, "expected '{' after requires");
        var names = new ArrayList<String>();
        do {
            names.add(expect(TokenType.IDENTIFIER, "expected imported type name").lexeme());
        } while (match(TokenType.COMMA));
        expect(TokenType.RBRACE, "expected '}' after imported types");
        expectKeyword("from");
        var namespace = parseQualifiedName();
        finishLine();
        return new ImportDeclaration(names, namespace, location);
    }

    private Declaration parseDeclaration(Prelude prelude) {
        if (checkKeyword("constant")) {
            return parseConstant(prelude);
        }
        if (checkKeyword("enum")) {
            return parseEnum(prelude);
        }
        if (checkKeyword("abstraction") || checkKeyword("type") || lineContains(TokenType.LBRACE)) {
            return parseTypeDeclaration(prelude);
        }
        return new NamespaceOperationDeclaration(parseMethod(prelude));
    }

    private ConstantDeclaration parseConstant(Prelude prelude) {
        var location = expectKeyword("constant").location();
        var name = expect(TokenType.IDENTIFIER, "expected constant name").lexeme();
        expect(TokenType.COLON, "expected ':' after constant name");
        var type = parseType();
        expect(TokenType.EQUALS, "expected '=' before constant value");
        var valueTokens = new ArrayList<Token>();
        while (!check(TokenType.NEWLINE) && !check(TokenType.COMMENT) && !check(TokenType.EOF)) {
            valueTokens.add(advance());
        }
        var inline = consumeInlineComment();
        consumeOptionalNewline();
        return new ConstantDeclaration(
                name,
                type,
                renderExpression(valueTokens),
                prelude.annotations(),
                combineDocumentation(prelude.documentation(), inline),
                location);
    }

    private TypeDeclaration parseTypeDeclaration(Prelude prelude) {
        var kind = TypeKind.VALUE;
        if (matchKeyword("abstraction")) {
            kind = TypeKind.ABSTRACTION;
        } else {
            matchKeyword("type");
        }
        var nameToken = expect(TokenType.IDENTIFIER, "expected type name");
        var parameters = parseGenericParameters();
        var parents = parseParents();
        expect(TokenType.LBRACE, "expected '{' before type body");
        var fields = new ArrayList<FieldDeclaration>();
        var methods = new ArrayList<MethodDeclaration>();
        parseMembers(fields, methods, false, null);
        return new TypeDeclaration(
                nameToken.lexeme(),
                kind,
                parameters,
                parents,
                prelude.annotations(),
                fields,
                methods,
                prelude.documentation(),
                nameToken.location());
    }

    private EnumDeclaration parseEnum(Prelude prelude) {
        expectKeyword("enum");
        var nameToken = expect(TokenType.IDENTIFIER, "expected enum name");
        var parents = parseParents();
        expect(TokenType.LBRACE, "expected '{' before enum body");
        var values = new ArrayList<EnumValue>();
        var fields = new ArrayList<FieldDeclaration>();
        var methods = new ArrayList<MethodDeclaration>();
        parseMembers(fields, methods, true, values);
        return new EnumDeclaration(
                nameToken.lexeme(),
                parents,
                prelude.annotations(),
                values,
                fields,
                methods,
                prelude.documentation(),
                nameToken.location());
    }

    private void parseMembers(
            List<FieldDeclaration> fields,
            List<MethodDeclaration> methods,
            boolean enumeration,
            List<EnumValue> enumValues) {
        while (true) {
            var prelude = readPrelude();
            if (match(TokenType.RBRACE)) {
                consumeInlineComment();
                consumeOptionalNewline();
                return;
            }
            if (check(TokenType.EOF)) {
                throw error(peek(), "UNSUPPORTED_SYNTAX", "type body is not closed");
            }
            if (enumeration && prelude.annotations().isEmpty() && isEnumValueLine()) {
                enumValues.add(parseEnumValue(prelude.documentation()));
            } else if (check(TokenType.IDENTIFIER) && checkNext(TokenType.COLON)) {
                fields.add(parseField(prelude));
            } else {
                methods.add(parseMethod(prelude));
            }
        }
    }

    private EnumValue parseEnumValue(String documentation) {
        var name = expect(TokenType.IDENTIFIER, "expected enum value");
        match(TokenType.COMMA);
        var inline = consumeInlineComment();
        consumeOptionalNewline();
        return new EnumValue(name.lexeme(), combineDocumentation(documentation, inline), name.location());
    }

    private FieldDeclaration parseField(Prelude prelude) {
        var name = expect(TokenType.IDENTIFIER, "expected field name");
        expect(TokenType.COLON, "expected ':' after field name");
        var type = parseType();
        var inline = consumeInlineComment();
        consumeOptionalNewline();
        return new FieldDeclaration(
                name.lexeme(),
                type,
                prelude.annotations(),
                combineDocumentation(prelude.documentation(), inline),
                name.location());
    }

    private MethodDeclaration parseMethod(Prelude prelude) {
        var returnType = parseType();
        var name = expect(TokenType.IDENTIFIER, "expected method name");
        var qualifiedName = new StringBuilder(name.lexeme());
        while (match(TokenType.DOT)) {
            qualifiedName.append('.').append(expect(TokenType.IDENTIFIER, "expected method name segment").lexeme());
        }
        expect(TokenType.LPAREN, "expected '(' after method name");
        var parameters = new ArrayList<Parameter>();
        if (!check(TokenType.RPAREN)) {
            do {
                var parameterName = expect(TokenType.IDENTIFIER, "expected parameter name");
                expect(TokenType.COLON, "expected ':' after parameter name");
                var parameterType = parseType();
                var varargs = match(TokenType.ELLIPSIS);
                parameters.add(new Parameter(
                        parameterName.lexeme(), parameterType, varargs, parameterName.location()));
            } while (match(TokenType.COMMA));
        }
        expect(TokenType.RPAREN, "expected ')' after parameters");
        var inline = consumeInlineComment();
        consumeOptionalNewline();
        return new MethodDeclaration(
                qualifiedName.toString(),
                returnType,
                parameters,
                prelude.annotations(),
                combineDocumentation(prelude.documentation(), inline),
                name.location());
    }

    private List<GenericParameter> parseGenericParameters() {
        if (!match(TokenType.LANGLE)) {
            return List.of();
        }
        var parameters = new ArrayList<GenericParameter>();
        do {
            var variable = expect(TokenType.TYPE_VARIABLE, "expected generic type variable");
            TypeReference bound = null;
            if (matchKeyword("extends")) {
                bound = parseType();
            }
            parameters.add(new GenericParameter(variable.lexeme(), bound, variable.location()));
        } while (match(TokenType.COMMA));
        expect(TokenType.RANGLE, "expected '>' after generic parameters");
        return List.copyOf(parameters);
    }

    private List<TypeReference> parseParents() {
        if (!matchKeyword("extends")) {
            return List.of();
        }
        var parents = new ArrayList<TypeReference>();
        do {
            parents.add(parseType());
        } while (match(TokenType.COMMA));
        return List.copyOf(parents);
    }

    private TypeReference parseType() {
        if (match(TokenType.TYPE_VARIABLE)) {
            var token = previous();
            return new TypeVariableReference(token.lexeme(), token.location());
        }
        var token = expect(TokenType.IDENTIFIER, "expected type reference");
        if (token.lexeme().equals("ANY")) {
            return new WildcardTypeReference(token.location());
        }
        if (token.lexeme().equals("list") || token.lexeme().equals("set")) {
            expect(TokenType.LANGLE, "expected '<' after collection type");
            var element = parseType();
            expect(TokenType.RANGLE, "expected '>' after collection element type");
            return new CollectionTypeReference(
                    token.lexeme().equals("list") ? CollectionKind.LIST : CollectionKind.SET,
                    element,
                    token.location());
        }
        if (PrimitiveKind.isPrimitive(token.lexeme())) {
            return new PrimitiveTypeReference(PrimitiveKind.fromSchemaName(token.lexeme()), token.location());
        }

        var name = new StringBuilder(token.lexeme());
        while (match(TokenType.DOT)) {
            name.append('.').append(expect(TokenType.IDENTIFIER, "expected qualified type segment").lexeme());
        }
        var arguments = new ArrayList<TypeReference>();
        if (match(TokenType.LANGLE)) {
            do {
                arguments.add(parseType());
            } while (match(TokenType.COMMA));
            expect(TokenType.RANGLE, "expected '>' after type arguments");
        }
        return new NamedTypeReference(name.toString(), arguments, token.location());
    }

    private Prelude readPrelude() {
        var documentation = new ArrayList<String>();
        var annotations = new ArrayList<AnnotationUse>();
        var progressed = true;
        while (progressed) {
            progressed = false;
            while (match(TokenType.NEWLINE)) {
                progressed = true;
            }
            while (match(TokenType.COMMENT)) {
                documentation.add(previous().lexeme());
                match(TokenType.NEWLINE);
                progressed = true;
            }
            while (check(TokenType.ANNOTATION)) {
                annotations.add(parseAnnotation());
                progressed = true;
                match(TokenType.NEWLINE);
            }
        }
        return new Prelude(List.copyOf(annotations), String.join("\n", documentation));
    }

    private AnnotationUse parseAnnotation() {
        var annotation = expect(TokenType.ANNOTATION, "expected annotation");
        var arguments = new ArrayList<String>();
        if (match(TokenType.LPAREN)) {
            if (!check(TokenType.RPAREN)) {
                do {
                    var argumentTokens = new ArrayList<Token>();
                    while (!check(TokenType.COMMA) && !check(TokenType.RPAREN) && !check(TokenType.EOF)) {
                        argumentTokens.add(advance());
                    }
                    if (argumentTokens.isEmpty()) {
                        throw error(peek(), "UNSUPPORTED_SYNTAX", "annotation argument must not be empty");
                    }
                    arguments.add(renderExpression(argumentTokens));
                } while (match(TokenType.COMMA));
            }
            expect(TokenType.RPAREN, "expected ')' after annotation arguments");
        }
        return new AnnotationUse(annotation.lexeme(), arguments, annotation.location());
    }

    private boolean isEnumValueLine() {
        if (!check(TokenType.IDENTIFIER)) {
            return false;
        }
        var next = tokens.get(Math.min(current + 1, tokens.size() - 1)).type();
        return next == TokenType.COMMA
                || next == TokenType.COMMENT
                || next == TokenType.NEWLINE
                || next == TokenType.RBRACE;
    }

    private boolean lineContains(TokenType type) {
        for (var index = current; index < tokens.size(); index++) {
            var candidate = tokens.get(index).type();
            if (candidate == type) {
                return true;
            }
            if (candidate == TokenType.NEWLINE || candidate == TokenType.COMMENT || candidate == TokenType.EOF) {
                return false;
            }
        }
        return false;
    }

    private String parseQualifiedName() {
        var name = new StringBuilder(expect(TokenType.IDENTIFIER, "expected name").lexeme());
        while (match(TokenType.DOT)) {
            name.append('.').append(expect(TokenType.IDENTIFIER, "expected name segment").lexeme());
        }
        return name.toString();
    }

    private void finishLine() {
        consumeInlineComment();
        if (!check(TokenType.NEWLINE) && !check(TokenType.EOF)) {
            throw error(peek(), "UNSUPPORTED_SYNTAX", "unexpected token '" + peek().lexeme() + "'");
        }
        consumeOptionalNewline();
    }

    private String consumeInlineComment() {
        return match(TokenType.COMMENT) ? previous().lexeme() : "";
    }

    private void consumeOptionalNewline() {
        match(TokenType.NEWLINE);
    }

    private static String combineDocumentation(String preceding, String inline) {
        if (preceding.isBlank()) {
            return inline;
        }
        return inline.isBlank() ? preceding : preceding + "\n" + inline;
    }

    private static String renderExpression(List<Token> expression) {
        var result = new StringBuilder();
        TokenType previous = null;
        for (var token : expression) {
            var value = switch (token.type()) {
                case STRING -> "\"" + token.lexeme().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                default -> token.lexeme();
            };
            if (result.length() > 0
                    && token.type() != TokenType.COMMA
                    && token.type() != TokenType.COLON
                    && token.type() != TokenType.RBRACE
                    && token.type() != TokenType.RPAREN
                    && previous != TokenType.LBRACE
                    && previous != TokenType.LPAREN
                    && previous != TokenType.COLON
                    && previous != TokenType.DOT) {
                result.append(' ');
            }
            result.append(value);
            if (token.type() == TokenType.COMMA) {
                result.append(' ');
            } else if (token.type() == TokenType.COLON) {
                result.append(' ');
            }
            previous = token.type();
        }
        return result.toString().stripTrailing();
    }

    private Token expectKeyword(String keyword) {
        if (!checkKeyword(keyword)) {
            throw error(peek(), "UNSUPPORTED_SYNTAX", "expected '" + keyword + "'");
        }
        return advance();
    }

    private boolean matchKeyword(String keyword) {
        if (!checkKeyword(keyword)) {
            return false;
        }
        advance();
        return true;
    }

    private boolean checkKeyword(String keyword) {
        return check(TokenType.IDENTIFIER) && peek().lexeme().equals(keyword);
    }

    private Token expect(TokenType type, String message) {
        if (!check(type)) {
            throw error(peek(), "UNSUPPORTED_SYNTAX", message);
        }
        return advance();
    }

    private boolean match(TokenType type) {
        if (!check(type)) {
            return false;
        }
        advance();
        return true;
    }

    private boolean check(TokenType type) {
        return peek().type() == type;
    }

    private boolean checkNext(TokenType type) {
        return tokens.get(Math.min(current + 1, tokens.size() - 1)).type() == type;
    }

    private Token advance() {
        if (!check(TokenType.EOF)) {
            current++;
        }
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private SchemaException error(Token token, String code, String message) {
        return new SchemaException(code, token.location(), message);
    }

    private record Prelude(List<AnnotationUse> annotations, String documentation) {}
}
