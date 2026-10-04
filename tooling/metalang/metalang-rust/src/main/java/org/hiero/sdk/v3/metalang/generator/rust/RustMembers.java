package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

/**
 * Renders the members of the generated types: getters and setters of attributes, methods (in a trait, in the
 * implementation of a trait, as inherent method, as function) with their Rust signature: owned parameters, varargs as
 * {@code Vec}, {@code Option} for {@code @@nullable}, {@code Result} for {@code @@throws}, {@code async fn} or
 * {@code BoxFuture} for {@code @@async}, {@code BoxStream} for {@code @@streaming}.
 */
final class RustMembers {

    private static final Pattern SELF = Pattern.compile("(?<![\\w&])Self\\b");

    /** Where a method is rendered. */
    enum Kind {
        /** a method of a trait declaration (no body) */
        TRAIT,
        /** a method of a trait implementation */
        TRAIT_IMPL,
        /** an inherent method or associated function of a struct or enum */
        INHERENT,
        /** an associated function of a trait object ({@code impl dyn Trait}) */
        DYN_STATIC,
        /** a function of a namespace */
        FUNCTION,
        /** a method of an extension trait (with a default body) */
        EXTENSION
    }

    /**
     * The Rust signature of a method.
     *
     * @param text       the signature without visibility and body, e.g. {@code fn sign(&self, data: Vec<u8>) -> Vec<u8>}
     * @param parameters the parameter types (for comparisons)
     * @param result     the return type (for comparisons)
     * @param names      the parameter names
     * @param async      whether it is an {@code async fn}
     */
    record Signature(String text, List<String> parameters, String result, List<String> names, boolean async) {
    }

    /**
     * The getter of an attribute.
     *
     * @param type       the return type
     * @param expression the expression that returns the value of the field {@code self.<name>}
     */
    record Getter(String type, String expression) {
    }

    private RustMembers() {
    }

    // --- attributes ----------------------------------------------------------------------------------

    /** The storage type of an attribute: its Rust type, {@code Option} if it is nullable. */
    static RustType storage(final FieldDefinition field, final Map<Type.TypeVariable, RustType> scope,
                            final RustContext context) {
        final RustType type = context.rustType(field.type(), scope);
        return field.hasAnnotation("nullable") ? new RustType.Optional(type) : type;
    }

    /**
     * The getter of an attribute stored as {@code self.<field>}: values that are {@code Copy} by value, strings,
     * bytes and lists as slices, other values by reference, shared values ({@code Arc}) as clone.
     */
    static Getter getter(final RustType type, final String field, final RustImports imports) {
        final String access = "self." + field;
        return switch (type) {
            case RustType.Optional optional -> switch (optional.inner()) {
                case RustType.Text ignored -> new Getter("Option<&str>", access + ".as_deref()");
                case RustType.Bytes ignored -> new Getter("Option<&[u8]>", access + ".as_deref()");
                case RustType.VecOf vec -> new Getter("Option<&[" + vec.element().render(imports) + "]>",
                        access + ".as_deref()");
                case RustType.Pairs pairs -> new Getter("Option<&[(" + pairs.key().render(imports) + ", "
                        + pairs.value().render(imports) + ")]>", access + ".as_deref()");
                case RustType inner when isCopy(inner, imports.context()) ->
                        new Getter("Option<" + inner.render(imports) + ">", access);
                case RustType inner when isShared(inner) -> new Getter("Option<" + inner.render(imports) + ">",
                        access + ".clone()");
                case RustType inner -> new Getter("Option<&" + inner.render(imports) + ">", access + ".as_ref()");
            };
            case RustType.Text ignored -> new Getter("&str", "&" + access);
            case RustType.Bytes ignored -> new Getter("&[u8]", "&" + access);
            case RustType.VecOf vec -> new Getter("&[" + vec.element().render(imports) + "]", "&" + access);
            case RustType.Pairs pairs -> new Getter("&[(" + pairs.key().render(imports) + ", "
                    + pairs.value().render(imports) + ")]", "&" + access);
            case RustType inner when isCopy(inner, imports.context()) -> new Getter(inner.render(imports), access);
            case RustType inner when isShared(inner) -> new Getter(inner.render(imports), access + ".clone()");
            case RustType.SelfType ignored -> new Getter("Self", access + ".clone()");
            case RustType inner -> new Getter("&" + inner.render(imports), "&" + access);
        };
    }

    /** Whether a value is {@code Copy} (returned by value). */
    static boolean isCopy(final RustType type, final RustContext context) {
        return !(type instanceof RustType.Parameter) && context.capabilities(type).copy();
    }

    /** Whether a value is shared ({@code Arc}): returned as clone of the {@code Arc}. */
    static boolean isShared(final RustType type) {
        return type instanceof RustType.Dyn || type instanceof RustType.AnyValue || type instanceof RustType.Function;
    }

    /**
     * The getter declaration of a trait.
     *
     * @return {@code fn name(&self) -> Type;}, with {@code where Self: Sized} if it returns {@code Self}
     */
    static String traitGetter(final FieldDefinition field, final Map<Type.TypeVariable, RustType> scope,
                              final RustImports imports) {
        final Getter getter = getter(storage(field, scope, imports.context()), "x", imports);
        return "fn " + RustNames.member(field.name()) + "(&self) -> " + getter.type()
                + (SELF.matcher(getter.type()).find() ? " where Self: Sized" : "");
    }

    /** The setter declaration of a trait. */
    static String traitSetter(final FieldDefinition field, final Map<Type.TypeVariable, RustType> scope,
                              final RustImports imports) {
        final String type = storage(field, scope, imports.context()).render(imports);
        final String name = RustNames.member(field.name());
        return "fn set_" + RustNames.snake(field.name()) + "(&mut self, " + name + ": " + type + ")"
                + (RustConstraints.isChecked(field) ? " -> Result<(), " + imports.support("InvalidArgumentError")
                + ">" : "") + (SELF.matcher(type).find() ? " where Self: Sized" : "");
    }

    // --- methods -------------------------------------------------------------------------------------

    /**
     * Renders the signature of a method or function.
     *
     * @param method  the method
     * @param name    its Rust name
     * @param scope   the type variables of the type with their Rust form
     * @param kind    where it is rendered
     * @param imports the imports of the file
     * @return the signature
     */
    static Signature signature(final MethodDefinition method, final String name,
                               final Map<Type.TypeVariable, RustType> scope, final Kind kind,
                               final RustImports imports) {
        final RustContext context = imports.context();
        final Map<Type.TypeVariable, RustType> methodScope = context.scope(scope, method);
        final List<String> generics = new ArrayList<>();
        for (final TypeParameterDefinition parameter : method.typeParameters()) {
            final String variable = context.variableName(parameter.name());
            final RustType bound = parameter.bound() == null ? null : context.rustType(parameter.bound(), methodScope);
            generics.add(bound instanceof RustType.Dyn dyn ? variable + ": " + dyn.render(imports)
                    .replaceFirst("^.*?<dyn ", "").replaceFirst(">$", "") : variable);
        }
        final List<String> names = new ArrayList<>();
        final List<String> types = new ArrayList<>();
        for (final ParameterDefinition parameter : method.parameters()) {
            names.add(RustNames.member(parameter.name()));
            RustType type = context.rustType(parameter.type(), methodScope);
            if (parameter.varargs()) {
                type = new RustType.VecOf(type);
            }
            if (parameter.hasAnnotation("nullable")) {
                type = new RustType.Optional(type);
            }
            types.add(type.render(imports));
        }
        final String value = value(method, methodScope, imports);
        final boolean asyncFn = method.hasAnnotation("async")
                && (kind == Kind.INHERENT || kind == Kind.FUNCTION || kind == Kind.DYN_STATIC);
        final String result;
        if (method.hasAnnotation("streaming")) {
            // streaming methods are never static
            result = imports.support("BoxStream") + "<'_, " + (value == null ? "()" : value) + ">";
        } else if (method.hasAnnotation("async") && !asyncFn) {
            // only trait methods return a boxed future (static ones are async functions of the trait object)
            result = imports.support("BoxFuture") + "<'_, " + (value == null ? "()" : value) + ">";
        } else {
            result = value;
        }
        final StringBuilder text = new StringBuilder(asyncFn ? "async fn " : "fn ").append(name);
        if (!generics.isEmpty()) {
            text.append('<').append(String.join(", ", generics)).append('>');
        }
        final List<String> parameters = new ArrayList<>();
        if (!method.isStatic() && kind != Kind.FUNCTION) {
            parameters.add("&self");
        }
        for (int i = 0; i < names.size(); i++) {
            parameters.add(names.get(i) + ": " + types.get(i));
        }
        text.append('(').append(String.join(", ", parameters)).append(')');
        if (result != null) {
            text.append(" -> ").append(result);
        }
        final boolean traitKind = kind == Kind.TRAIT || kind == Kind.TRAIT_IMPL || kind == Kind.EXTENSION;
        if (traitKind && (!generics.isEmpty() || SELF.matcher(String.join(",", types) + " " + result).find())) {
            text.append(" where Self: Sized");
        }
        return new Signature(text.toString(), types, result == null ? "" : result, names, asyncFn);
    }

    /** The value a method returns, before async and streaming: {@code Option}, {@code Result}; null for none. */
    private static String value(final MethodDefinition method, final Map<Type.TypeVariable, RustType> scope,
                                final RustImports imports) {
        final RustContext context = imports.context();
        RustType type = context.rustType(method.returnType(), scope);
        if (method.hasAnnotation("nullable") && !(type instanceof RustType.Unit)) {
            type = new RustType.Optional(type);
        }
        final String rendered = type instanceof RustType.Unit ? null : type.render(imports);
        final Optional<String> error = errorType(method, imports);
        if (error.isEmpty()) {
            return rendered;
        }
        return "Result<" + (rendered == null ? "()" : rendered) + ", " + error.get() + ">";
    }

    /** The error type of a method: its error type, the error enum of several, or none. */
    static Optional<String> errorType(final MethodDefinition method, final RustImports imports) {
        final RustContext context = imports.context();
        final List<String> ids = RustContext.errorIds(method);
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        final Optional<RustContext.ErrorEnum> errors = context.errorEnum(method);
        if (errors.isPresent()) {
            return Optional.of(imports.error(new RustContext.ErrorType(errors.get().namespace(),
                    errors.get().name())));
        }
        return Optional.of(imports.error(context.error(ids.getFirst())));
    }

    /** The body of a method that is not implemented yet. */
    static String stub(final String owner, final MethodDefinition method) {
        return "todo!(" + RustLiterals.quote(owner + "." + method.name()) + ")";
    }

    /**
     * Renders a method with documentation and body.
     *
     * @param method    the method
     * @param signature its signature
     * @param kind      where it is rendered
     * @param body      the body (an expression), {@code null} for a declaration without body
     * @param indent    the indentation
     * @return the code
     */
    static String render(final MethodDefinition method, final Signature signature, final Kind kind,
                         final String body, final String indent) {
        final StringBuilder out = new StringBuilder();
        if (kind != Kind.TRAIT_IMPL) {
            out.append(RustDoc.render(indent, docParagraphs(method), method.hasAnnotation("deprecated")));
        }
        if (signature.names().size() + (method.isStatic() || kind == Kind.FUNCTION ? 0 : 1) > 7) {
            out.append(indent).append("#[allow(clippy::too_many_arguments)]\n");
        }
        out.append(indent);
        if (kind == Kind.INHERENT || kind == Kind.FUNCTION || kind == Kind.DYN_STATIC) {
            out.append("pub ");
        }
        out.append(signature.text());
        if (body == null) {
            out.append(";\n");
        } else {
            out.append(" {\n").append(indent).append("    ").append(body).append('\n').append(indent)
                    .append("}\n");
        }
        return out.toString();
    }

    /** The documentation of a method with the section about its errors. */
    static List<String> docParagraphs(final MethodDefinition method) {
        final List<String> paragraphs = new ArrayList<>();
        paragraphs.add(method.documentation());
        final List<String> ids = RustContext.errorIds(method);
        if (!ids.isEmpty()) {
            paragraphs.add("# Errors\n\nFails with " + ids.stream().map(id -> "`" + id + "`")
                    .collect(Collectors.joining(", ")) + ".");
        }
        return paragraphs;
    }

    /** The arguments of a delegating call: the parameter names. */
    static String arguments(final Signature signature) {
        return String.join(", ", signature.names());
    }
}
