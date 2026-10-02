package org.hiero.sdk.v3.metalang.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Comment;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.MethodSyntax;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.Requires;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeParameter;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.grammar.MetaLangLexer;
import org.hiero.sdk.v3.metalang.grammar.MetaLangParser;
import org.hiero.sdk.v3.metalang.source.SchemaSource;

/**
 * Converts an ANTLR parse tree into the immutable AST. Must only be called for parse trees without
 * syntax errors.
 */
final class AstBuilder {

    private final SchemaSource source;
    private final CommonTokenStream tokens;

    AstBuilder(final SchemaSource source, final CommonTokenStream tokens) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.tokens = Objects.requireNonNull(tokens, "tokens must not be null");
    }

    SchemaFile build(final MetaLangParser.SchemaContext ctx) {
        final MetaLangParser.NamespaceDeclContext ns = ctx.namespaceDecl();
        final List<Requires> requires = ctx.requiresDecl().stream().map(this::requires).toList();
        final List<Declaration> declarations = ctx.topLevelDecl().stream().map(this::declaration).toList();
        return new SchemaFile(qualifiedName(ns.qualifiedName()), requires, declarations, comments(),
                location(ns.start));
    }

    // --- top level -------------------------------------------------------------------------------

    private Requires requires(final MetaLangParser.RequiresDeclContext ctx) {
        final MetaLangParser.RequiresListContext list = ctx.requiresList();
        final boolean wildcard = list.STAR() != null;
        final List<String> types = list.identifier().stream().map(this::identifier).toList();
        return new Requires(qualifiedName(ctx.qualifiedName()), types, wildcard, location(ctx.start));
    }

    private Declaration declaration(final MetaLangParser.TopLevelDeclContext ctx) {
        if (ctx.constantDecl() != null) {
            return constant(ctx.constantDecl());
        }
        if (ctx.enumDecl() != null) {
            return enumType(ctx.enumDecl());
        }
        if (ctx.complexTypeDecl() != null) {
            return complexType(ctx.complexTypeDecl());
        }
        return function(ctx.functionDecl());
    }

    private Declaration.Constant constant(final MetaLangParser.ConstantDeclContext ctx) {
        return new Declaration.Constant(identifier(ctx.identifier()), annotations(ctx.annotation()),
                documentation(ctx), typeRef(ctx.typeRef()), literal(ctx.literal()), location(ctx.CONSTANT().getSymbol()));
    }

    private Declaration.ComplexType complexType(final MetaLangParser.ComplexTypeDeclContext ctx) {
        final List<TypeParameter> typeParameters = typeParameters(ctx.typeParameters());
        final List<Field> fields = new ArrayList<>();
        final List<Method> methods = new ArrayList<>();
        for (final MetaLangParser.MemberContext member : ctx.typeBody().member()) {
            if (member.fieldDecl() != null) {
                fields.add(field(member.fieldDecl()));
            } else {
                methods.add(method(member.methodDecl()));
            }
        }
        return new Declaration.ComplexType(identifier(ctx.identifier()), annotations(ctx.annotation()),
                documentation(ctx), ctx.ABSTRACTION() != null, ctx.TYPE() != null, typeParameters,
                supertypes(ctx.extendsClause()), fields, methods, location(ctx.identifier().start));
    }

    private Declaration.EnumType enumType(final MetaLangParser.EnumDeclContext ctx) {
        final List<EnumValue> values = new ArrayList<>();
        final List<SourceLocation> placeholders = new ArrayList<>();
        final List<Field> fields = new ArrayList<>();
        final List<Method> methods = new ArrayList<>();
        for (final MetaLangParser.EnumEntryContext entry : ctx.enumEntry()) {
            switch (entry) {
                case MetaLangParser.EnumValueContext value -> values.add(new EnumValue(
                        identifier(value.identifier()), annotations(value.annotation()), documentation(value),
                        location(value.identifier().start)));
                case MetaLangParser.EnumPlaceholderContext placeholder ->
                        placeholders.add(location(placeholder.start));
                case MetaLangParser.EnumFieldContext field -> fields.add(field(field.fieldDecl()));
                case MetaLangParser.EnumMethodContext method -> methods.add(method(method.returningMethodDecl()));
                default -> throw new IllegalStateException("Unexpected enum entry: " + entry.getClass());
            }
        }
        return new Declaration.EnumType(identifier(ctx.identifier()), annotations(ctx.annotation()),
                documentation(ctx), supertypes(ctx.extendsClause()), values, placeholders, fields, methods,
                location(ctx.identifier().start));
    }

    private Declaration.Function function(final MetaLangParser.FunctionDeclContext ctx) {
        final Method method = new Method(identifier(ctx.identifier()), annotations(ctx.annotation()),
                typeRef(ctx.typeRef()), typeParameters(ctx.typeParameters()), parameters(ctx.parameterList()),
                MethodSyntax.CLASSIC, documentation(ctx), location(ctx.identifier().start));
        return new Declaration.Function(method);
    }

    // --- members ---------------------------------------------------------------------------------

    private Field field(final MetaLangParser.FieldDeclContext ctx) {
        return new Field(identifier(ctx.identifier()), annotations(ctx.annotation()), typeRef(ctx.typeRef()),
                documentation(ctx), location(ctx.identifier().start));
    }

    private Method method(final MetaLangParser.MethodDeclContext ctx) {
        return switch (ctx) {
            case MetaLangParser.WithReturnMethodContext m -> method(m.returningMethodDecl());
            case MetaLangParser.NoReturnMethodContext m -> new Method(identifier(m.identifier()),
                    annotations(m.annotation()), new TypeRef.Void(location(m.identifier().start)),
                    typeParameters(m.typeParameters()), parameters(m.parameterList()), MethodSyntax.MISSING_RETURN, documentation(m),
                    location(m.identifier().start));
            default -> throw new IllegalStateException("Unexpected method form: " + ctx.getClass());
        };
    }

    private Method method(final MetaLangParser.ReturningMethodDeclContext ctx) {
        return switch (ctx) {
            case MetaLangParser.ClassicMethodContext m -> new Method(identifier(m.identifier()),
                    annotations(m.annotation()), typeRef(m.typeRef()), typeParameters(m.typeParameters()),
                    parameters(m.parameterList()),
                    MethodSyntax.CLASSIC, documentation(m), location(m.identifier().start));
            case MetaLangParser.TrailingReturnMethodContext m -> new Method(identifier(m.identifier()),
                    annotations(m.annotation()), typeRef(m.typeRef()), typeParameters(m.typeParameters()),
                    parameters(m.parameterList()),
                    MethodSyntax.TRAILING_RETURN, documentation(m), location(m.identifier().start));
            default -> throw new IllegalStateException("Unexpected method form: " + ctx.getClass());
        };
    }

    private List<Parameter> parameters(final MetaLangParser.ParameterListContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        return ctx.parameter().stream()
                .map(p -> new Parameter(identifier(p.identifier()), annotations(p.annotation()), typeRef(p.typeRef()),
                        p.ELLIPSIS() != null, location(p.identifier().start)))
                .toList();
    }

    private List<TypeRef> supertypes(final MetaLangParser.ExtendsClauseContext ctx) {
        return ctx == null ? List.of() : ctx.typeRef().stream().map(this::typeRef).toList();
    }

    // --- types -----------------------------------------------------------------------------------

    private List<TypeParameter> typeParameters(final MetaLangParser.TypeParametersContext ctx) {
        return ctx == null ? List.of() : ctx.typeParameter().stream().map(this::typeParameter).toList();
    }

    private TypeParameter typeParameter(final MetaLangParser.TypeParameterContext ctx) {
        return new TypeParameter(ctx.GENERIC_NAME().getText(), ctx.typeRef() == null ? null : typeRef(ctx.typeRef()),
                location(ctx.start));
    }

    private TypeRef typeRef(final MetaLangParser.TypeRefContext ctx) {
        final SourceLocation location = location(ctx.start);
        if (ctx.functionType() != null) {
            final MetaLangParser.FunctionTypeContext fn = ctx.functionType();
            return new TypeRef.Function(typeRef(fn.typeRef()), identifier(fn.identifier()),
                    parameters(fn.parameterList()), location);
        }
        if (ctx.GENERIC_NAME() != null) {
            return new TypeRef.GenericParameter(ctx.GENERIC_NAME().getText(), location);
        }
        if (ctx.ANY() != null) {
            return new TypeRef.Any(location);
        }
        if (ctx.VOID() != null) {
            return new TypeRef.Void(location);
        }
        final List<TypeRef.TypeArgument> arguments = ctx.typeArguments() == null
                ? List.of()
                : ctx.typeArguments().typeArgument().stream().map(this::typeArgument).toList();
        return new TypeRef.Named(qualifiedName(ctx.qualifiedName()), arguments, location);
    }

    private TypeRef.TypeArgument typeArgument(final MetaLangParser.TypeArgumentContext ctx) {
        return switch (ctx) {
            case MetaLangParser.WildcardArgumentContext w -> new TypeRef.Wildcard(
                    w.typeRef() == null ? null : typeRef(w.typeRef()), location(w.start));
            case MetaLangParser.BoundedGenericArgumentContext b -> new TypeRef.BoundedGeneric(
                    b.GENERIC_NAME().getText(), typeRef(b.typeRef()), location(b.start));
            case MetaLangParser.PlainArgumentContext p -> new TypeRef.Concrete(typeRef(p.typeRef()));
            default -> throw new IllegalStateException("Unexpected type argument: " + ctx.getClass());
        };
    }

    // --- annotations & literals ------------------------------------------------------------------

    private List<Annotation> annotations(final List<MetaLangParser.AnnotationContext> ctxs) {
        return ctxs.stream().map(this::annotation).toList();
    }

    private Annotation annotation(final MetaLangParser.AnnotationContext ctx) {
        final String name = ctx.ANNOTATION_NAME().getText().substring(2);
        final List<Literal> arguments = ctx.annotationArgument().stream()
                .map(a -> a.KEBAB_ID() != null
                        ? new Literal.NameLiteral(a.KEBAB_ID().getText(), location(a.start))
                        : literal(a.literal()))
                .toList();
        return new Annotation(name, arguments, ctx.LPAREN() != null, location(ctx.start));
    }

    private Literal literal(final MetaLangParser.LiteralContext ctx) {
        final SourceLocation location = location(ctx.start);
        return switch (ctx) {
            case MetaLangParser.StringLiteralContext s -> new Literal.StringLiteral(unquote(s.STRING().getText()), location);
            case MetaLangParser.NumberLiteralContext n -> new Literal.NumberLiteral(n.NUMBER().getText(), location);
            case MetaLangParser.ListLiteralContext l -> new Literal.ListLiteral(
                    l.literal().stream().map(this::literal).toList(), location);
            case MetaLangParser.StructLiteralContext s -> new Literal.StructLiteral(qualifiedName(s.qualifiedName()),
                    s.structEntry().stream()
                            .map(e -> new Literal.StructEntry(identifier(e.identifier()), literal(e.literal())))
                            .toList(),
                    location);
            case MetaLangParser.NameLiteralContext n -> new Literal.NameLiteral(qualifiedName(n.qualifiedName()), location);
            default -> throw new IllegalStateException("Unexpected literal: " + ctx.getClass());
        };
    }

    /**
     * Removes the quotes of a string literal. Only {@code \"} and {@code \\} are unescaped; every other
     * escape sequence is kept verbatim so regular expressions such as {@code "^/[^\s]*$"} survive.
     */
    static String unquote(final String quoted) {
        final String body = quoted.substring(1, quoted.length() - 1);
        final StringBuilder result = new StringBuilder(body.length());
        for (int i = 0; i < body.length(); i++) {
            final char c = body.charAt(i);
            if (c == '\\' && i + 1 < body.length() && (body.charAt(i + 1) == '"' || body.charAt(i + 1) == '\\')) {
                result.append(body.charAt(i + 1));
                i++;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // --- names -----------------------------------------------------------------------------------

    private String identifier(final MetaLangParser.IdentifierContext ctx) {
        return ctx.getText();
    }

    private String qualifiedName(final MetaLangParser.QualifiedNameContext ctx) {
        return ctx.identifier().stream().map(this::identifier).collect(Collectors.joining("."));
    }

    // --- comments --------------------------------------------------------------------------------

    /**
     * Documentation of a declaration: the comment lines directly above it (not separated from it by
     * another declaration) followed by a trailing comment on its last line.
     */
    private String documentation(final ParserRuleContext ctx) {
        final List<String> parts = new ArrayList<>();
        final int startIndex = ctx.start.getTokenIndex();
        final List<Token> left = tokens.getHiddenTokensToLeft(startIndex, MetaLangLexer.HIDDEN);
        if (left != null) {
            final int previousLine = previousDefaultTokenLine(startIndex);
            left.stream().filter(t -> t.getLine() > previousLine).map(AstBuilder::commentText).forEach(parts::add);
        }
        final Token stop = ctx.stop;
        if (stop != null) {
            final List<Token> right = tokens.getHiddenTokensToRight(stop.getTokenIndex(), MetaLangLexer.HIDDEN);
            if (right != null) {
                right.stream().filter(t -> t.getLine() == stop.getLine()).map(AstBuilder::commentText)
                        .forEach(parts::add);
            }
        }
        return String.join("\n", parts).strip();
    }

    private int previousDefaultTokenLine(final int tokenIndex) {
        for (int i = tokenIndex - 1; i >= 0; i--) {
            final Token token = tokens.get(i);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                return token.getLine();
            }
        }
        return 0;
    }

    private List<Comment> comments() {
        return tokens.getTokens().stream()
                .filter(t -> t.getChannel() == MetaLangLexer.HIDDEN)
                .map(t -> new Comment(commentText(t), location(t)))
                .toList();
    }

    private static String commentText(final Token token) {
        final String text = token.getText();
        if (text.startsWith("//")) {
            return text.substring(2).strip();
        }
        return text.substring(2, text.length() - 2).strip();
    }

    private SourceLocation location(final Token token) {
        return new SourceLocation(source.file(), token.getLine() + source.lineOffset(),
                token.getCharPositionInLine() + 1);
    }
}
