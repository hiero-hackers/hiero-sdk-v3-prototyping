package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

/**
 * Generates the file of one type: a struct for a complex type (private fields, a constructor {@code new} that checks
 * the validation annotations, getters, setters for mutable attributes, the methods, and the implementations of its
 * traits), a trait for an abstraction (static methods as associated functions of the trait object, final methods in
 * an extension trait), an enum for a sealed abstraction (one variant per permitted type) and an enum for an enum
 * (attributes as {@code const fn}, {@code Display}, {@code FromStr}).
 */
final class RustTypeGenerator {

    static final String INDENT = "    ";

    private RustTypeGenerator() {
    }

    static GeneratedFile generate(final TypeDefinition type, final RustContext context) {
        final String folder = context.folder(type.name().namespace());
        final RustImports imports = new RustImports(context, folder, false);
        imports.declare(type.name());
        final String code = switch (type) {
            case TypeDefinition.EnumDefinition enumType -> enumType(enumType, imports);
            case TypeDefinition.ComplexTypeDefinition complex when context.isSealed(complex.name()) ->
                    sealed(complex, imports);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() -> trait(complex, imports);
            case TypeDefinition.ComplexTypeDefinition complex -> struct(complex, imports);
        };
        return RustGenerator.file(RustNames.crateDirectory(folder) + "/src/"
                + RustNames.directory(type.name().namespace()) + "/" + context.fileModule(type) + ".rs", imports, code);
    }

    // --- structs -------------------------------------------------------------------------------------

    private static String struct(final TypeDefinition.ComplexTypeDefinition type, final RustImports imports) {
        final RustContext context = imports.context();
        final Map<Type.TypeVariable, RustType> scope = context.scope(type);
        final String generics = generics(context.keptParameters(type), context);
        final String self = type.name().name() + generics;
        final List<FieldDefinition> fields = type.fields();
        final List<RustType> storages = fields.stream().map(f -> RustMembers.storage(f, scope, context)).toList();
        final List<String> rendered = storages.stream().map(s -> s.render(imports)).toList();
        final List<String> unused = context.keptParameters(type).stream()
                .map(p -> context.variableName(p.name()))
                .filter(p -> rendered.stream().noneMatch(r -> Pattern.compile("\\b" + p + "\\b").matcher(r).find()))
                .toList();
        final RustContext.Capabilities caps = context.definitionCapabilities(type.name());
        final boolean debug = storages.stream().allMatch(RustContext::hasDebug);
        final StringBuilder out = new StringBuilder();

        // declaration
        out.append(RustDoc.render("", List.of(type.documentation()), type.hasAnnotation("deprecated")));
        out.append(derives(debug, caps).replace("Clone", fields.isEmpty() ? "Clone, Default" : "Clone"));
        out.append("pub struct ").append(self).append(" {\n");
        for (int i = 0; i < fields.size(); i++) {
            out.append(INDENT).append(RustNames.member(fields.get(i).name())).append(": ").append(rendered.get(i))
                    .append(",\n");
        }
        if (!unused.isEmpty()) {
            out.append(INDENT).append("_marker: ").append(imports.external("std::marker::PhantomData"))
                    .append("<fn() -> (").append(String.join(", ", unused)).append(",)>,\n");
        }
        out.append("}\n");

        // constructor, attributes, methods
        final boolean fallible = context.isFallible(type.name());
        final String error = fallible ? imports.support("InvalidArgumentError") : null;
        out.append("\n#[allow(unused_variables)]\nimpl").append(generics).append(' ').append(self).append(" {\n");
        out.append(INDENT).append("/// Creates a new `").append(type.name().name()).append("`.\n");
        if (fallible) {
            out.append(INDENT).append("///\n").append(INDENT).append("/// # Errors\n").append(INDENT).append("///\n")
                    .append(INDENT).append("/// Fails with an `InvalidArgumentError` if a value violates its "
                            + "constraints.\n");
        }
        if (fields.size() > 7) {
            out.append(INDENT).append("#[allow(clippy::too_many_arguments)]\n");
        }
        out.append(INDENT).append("pub fn new(");
        final List<String> parameters = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            parameters.add(RustNames.member(fields.get(i).name()) + ": " + rendered.get(i));
        }
        out.append(String.join(", ", parameters)).append(") -> ")
                .append(fallible ? "Result<Self, " + error + ">" : "Self").append(" {\n");
        for (final FieldDefinition field : fields) {
            out.append(RustConstraints.checks(field, RustNames.member(field.name()), INDENT + INDENT, imports));
        }
        final List<String> initializers = new ArrayList<>(fields.stream().map(f -> RustNames.member(f.name()))
                .toList());
        if (!unused.isEmpty()) {
            initializers.add("_marker: " + imports.external("std::marker::PhantomData"));
        }
        final String literal = "Self { " + String.join(", ", initializers) + " }";
        out.append(INDENT).append(INDENT).append(fallible ? "Ok(" + literal + ")" : literal).append('\n');
        out.append(INDENT).append("}\n");
        for (int i = 0; i < fields.size(); i++) {
            final FieldDefinition field = fields.get(i);
            final String name = RustNames.member(field.name());
            final RustMembers.Getter getter = RustMembers.getter(storages.get(i), name, imports);
            out.append('\n').append(RustDoc.render(INDENT, List.of(field.documentation().isBlank()
                    ? "Returns the `" + field.name() + "`." : field.documentation()), field.hasAnnotation("deprecated")));
            out.append(INDENT).append("pub fn ").append(name).append("(&self) -> ").append(getter.type()).append(" {\n")
                    .append(INDENT).append(INDENT).append(getter.expression()).append('\n').append(INDENT).append("}\n");
            if (!field.hasAnnotation("immutable")) {
                out.append('\n').append(setter(field, rendered.get(i), imports));
            }
        }
        final Map<MethodDefinition, String> names = inherentNames(type, context);
        for (final Map.Entry<MethodDefinition, String> method : names.entrySet()) {
            final RustMembers.Signature signature = RustMembers.signature(method.getKey(), method.getValue(), scope,
                    RustMembers.Kind.INHERENT, imports);
            out.append('\n').append(RustMembers.render(method.getKey(), signature, RustMembers.Kind.INHERENT,
                    RustMembers.stub(type.name().name(), method.getKey()), INDENT));
        }
        out.append("}\n");

        if (!debug) {
            out.append('\n').append(debugImpl(type, generics, fields, storages, imports));
        }
        display(type, generics, imports).ifPresent(d -> out.append('\n').append(d));
        for (final Type.DeclaredType trait : context.traits(type)) {
            if (context.isGenerated(trait.name())) {
                out.append('\n').append(traitImpl(type, trait, scope, names, imports));
            }
        }
        for (final Type supertype : type.supertypes()) {
            if (supertype instanceof Type.DeclaredType declared && context.isStruct(declared.name())
                    && context.isGenerated(declared.name())) {
                out.append('\n').append(fromImpl(type, declared, scope, generics, imports));
            }
        }
        return out.toString();
    }

    private static String derives(final boolean debug, final RustContext.Capabilities caps) {
        final List<String> derives = new ArrayList<>();
        if (debug) {
            derives.add("Debug");
        }
        if (caps.cloneable()) {
            derives.add("Clone");
        }
        if (caps.eq()) {
            derives.add("PartialEq");
        }
        if (caps.fullEq()) {
            derives.add("Eq");
        }
        if (caps.hash()) {
            derives.add("Hash");
        }
        return derives.isEmpty() ? "" : "#[derive(" + String.join(", ", derives) + ")]\n";
    }

    private static String setter(final FieldDefinition field, final String type, final RustImports imports) {
        final String name = RustNames.member(field.name());
        final boolean checked = RustConstraints.isChecked(field);
        final StringBuilder out = new StringBuilder();
        out.append(INDENT).append("/// Sets the `").append(field.name()).append("`.\n");
        if (checked) {
            out.append(INDENT).append("///\n").append(INDENT).append("/// # Errors\n").append(INDENT).append("///\n")
                    .append(INDENT).append("/// Fails with an `InvalidArgumentError` if the value violates its "
                            + "constraints; the value is not changed then.\n");
        }
        if (field.hasAnnotation("deprecated")) {
            out.append(RustDoc.render(INDENT, List.of(), true));
        }
        out.append(INDENT).append("pub fn set_").append(RustNames.snake(field.name())).append("(&mut self, ")
                .append(name).append(": ").append(type).append(") -> ")
                .append(checked ? "Result<&mut Self, " + imports.support("InvalidArgumentError") + ">" : "&mut Self")
                .append(" {\n");
        out.append(RustConstraints.checks(field, name, INDENT + INDENT, imports));
        out.append(INDENT).append(INDENT).append("self.").append(name).append(" = ").append(name).append(";\n");
        out.append(INDENT).append(INDENT).append(checked ? "Ok(self)" : "self").append('\n');
        return out.append(INDENT).append("}\n").toString();
    }

    /**
     * The Rust names of the inherent methods of a struct or enum: its methods without the final methods of its traits
     * (they come from the extension traits) and without {@code toString()} ({@code Display}); a name that two traits
     * use for different methods gets the name of the declaring type as suffix.
     */
    static Map<MethodDefinition, String> inherentNames(final TypeDefinition type, final RustContext context) {
        final Map<MethodDefinition, String> names = new LinkedHashMap<>();
        final Set<String> used = new HashSet<>();
        for (final MethodDefinition method : type.methods()) {
            if (RustContext.isDisplay(method) || method.hasAnnotation("finalMethod")
                    && !type.name().equals(method.declaringType())) {
                continue;
            }
            String name = context.methodName(method);
            if (!used.add(name)) {
                name = name + "_" + RustNames.snake(method.declaringType().name());
                used.add(name);
            }
            names.put(method, name);
        }
        return names;
    }

    private static String debugImpl(final TypeDefinition type, final String generics,
                                    final List<FieldDefinition> fields, final List<RustType> storages,
                                    final RustImports imports) {
        final String bounded = boundedGenerics(type, imports.context(), imports, "std::fmt::Debug");
        final StringBuilder out = new StringBuilder("impl").append(bounded).append(' ')
                .append(imports.external("std::fmt::Debug")).append(" for ").append(type.name().name())
                .append(generics).append(" {\n");
        out.append(INDENT).append("fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {\n");
        out.append(INDENT).append(INDENT).append("f.debug_struct(").append(RustLiterals.quote(type.name().name()))
                .append(")\n");
        for (int i = 0; i < fields.size(); i++) {
            if (RustContext.hasDebug(storages.get(i))) {
                final String name = RustNames.member(fields.get(i).name());
                out.append(INDENT).append(INDENT).append(INDENT).append(".field(")
                        .append(RustLiterals.quote(name.replace("r#", ""))).append(", &self.").append(name)
                        .append(")\n");
            }
        }
        out.append(INDENT).append(INDENT).append(INDENT).append(".finish_non_exhaustive()\n");
        return out.append(INDENT).append("}\n}\n").toString();
    }

    /** The {@code Display} implementation of a type with {@code string toString()}. */
    private static Optional<String> display(final TypeDefinition type, final String generics,
                                            final RustImports imports) {
        final Optional<MethodDefinition> method = type.methods().stream().filter(RustContext::isDisplay).findFirst();
        return method.map(m -> {
            final String bounded = boundedGenerics(type, imports.context(), imports, null);
            return RustDoc.render("", List.of(m.documentation())) + "#[allow(unused_variables)]\nimpl" + bounded
                    + " " + imports.external("std::fmt::Display") + " for " + type.name().name() + generics + " {\n"
                    + INDENT + "fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {\n"
                    + INDENT + INDENT + RustMembers.stub(type.name().name(), m) + "\n"
                    + INDENT + "}\n}\n";
        });
    }

    /** The implementation of a trait, delegating to the inherent members where the signatures match. */
    private static String traitImpl(final TypeDefinition type, final Type.DeclaredType trait,
                                    final Map<Type.TypeVariable, RustType> scope,
                                    final Map<MethodDefinition, String> names, final RustImports imports) {
        final RustContext context = imports.context();
        final TypeDefinition definition = context.model().definition(trait);
        final Map<Type.TypeVariable, RustType> traitScope = context.implScope(definition, trait, scope);
        final String generics = generics(context.keptParameters(type), context);
        final String bounded = boundedGenerics(type, context, imports, "std::fmt::Debug + Send + Sync");
        final String traitType = new RustType.Dyn(trait.name(), context.keptArguments(trait, scope)).render(imports)
                .replaceFirst("^.*?<dyn ", "").replaceFirst(">$", "");
        final StringBuilder out = new StringBuilder("#[allow(unused_variables)]\nimpl").append(bounded).append(' ')
                .append(traitType).append(" for ").append(type.name().name()).append(generics).append(" {\n");
        final List<String> members = new ArrayList<>();
        for (final FieldDefinition field : context.traitFields(definition)) {
            final FieldDefinition own = type.field(field.name()).orElseThrow();
            final RustType traitStorage = RustMembers.storage(field, traitScope, context);
            final RustType ownStorage = RustMembers.storage(own, scope, context);
            final String name = RustNames.member(field.name());
            final String declaration = RustMembers.traitGetter(field, traitScope, imports);
            final String ownGetter = "self." + name + "()";
            final String body = convert(ownGetter, ownStorage, traitStorage, true, imports)
                    .orElse("todo!(" + RustLiterals.quote(type.name().name() + "." + field.name()) + ")");
            members.add(INDENT + declaration + " {\n" + INDENT + INDENT + body + "\n" + INDENT + "}\n");
            if (!field.hasAnnotation("immutable")) {
                final String setter = RustMembers.traitSetter(field, traitScope, imports);
                final boolean same = traitStorage.render(imports).equals(ownStorage.render(imports));
                final boolean traitChecked = RustConstraints.isChecked(field);
                final boolean ownChecked = RustConstraints.isChecked(own);
                final String call = "self.set_" + RustNames.snake(field.name()) + "(" + name + ")";
                final String setterBody = !same || ownChecked && !traitChecked
                        ? "todo!(" + RustLiterals.quote(type.name().name() + "." + field.name()) + ")"
                        : traitChecked ? (ownChecked ? call + ".map(|_| ())" : call + ";\n" + INDENT + INDENT + "Ok(())")
                        : call + ";";
                members.add(INDENT + setter + " {\n" + INDENT + INDENT + setterBody + "\n" + INDENT + "}\n");
            }
        }
        for (final MethodDefinition method : context.traitMethods(definition)) {
            final String name = context.methodName(method);
            final RustMembers.Signature signature = RustMembers.signature(method, name, traitScope,
                    RustMembers.Kind.TRAIT_IMPL, imports);
            final Optional<MethodDefinition> own = names.keySet().stream()
                    .filter(m -> m.location().equals(method.location())).findFirst();
            String body = "todo!(" + RustLiterals.quote(type.name().name() + "." + method.name()) + ")";
            if (own.isPresent()) {
                final RustMembers.Signature inherent = RustMembers.signature(own.get(), names.get(own.get()), scope,
                        RustMembers.Kind.INHERENT, imports);
                final String call = "self." + names.get(own.get()) + "(" + RustMembers.arguments(signature) + ")";
                if (inherent.parameters().equals(signature.parameters())) {
                    if (inherent.result().equals(signature.result())) {
                        body = call;
                    } else if (inherent.async() && context.keptParameters(type).isEmpty()
                            && signature.result().endsWith(", " + inherent.result() + ">")
                            && signature.result().contains("BoxFuture")) {
                        body = "Box::pin(" + call + ")";
                    }
                }
            }
            members.add(RustMembers.render(method, signature, RustMembers.Kind.TRAIT_IMPL, body, INDENT));
        }
        out.append(String.join("\n", members));
        return out.append("}\n").toString();
    }

    /**
     * Converts an owned value or a getter result of one storage type to another: {@code Some(..)} for a narrowed
     * attribute, {@code Arc::new(..)} for a trait object.
     *
     * @param expression the value
     * @param from       its storage type
     * @param to         the required storage type
     * @param getter     whether the value is the result of a getter (a reference for most types) or owned
     * @param imports    the imports of the file
     * @return the converted expression (a getter result is converted to the getter type of {@code to}), empty if
     *         there is no conversion
     */
    static Optional<String> convert(final String expression, final RustType from, final RustType to,
                                    final boolean getter, final RustImports imports) {
        final String fromText = from.render(imports);
        final String toText = to.render(imports);
        if (fromText.equals(toText)) {
            return Optional.of(expression);
        }
        if (to instanceof RustType.Optional optional && !(from instanceof RustType.Optional)) {
            return convert(expression, from, optional.inner(), getter, imports).map(v -> "Some(" + v + ")");
        }
        if ((to instanceof RustType.Dyn || to instanceof RustType.AnyValue) && concrete(from)) {
            return Optional.of(imports.external("std::sync::Arc") + "::new(" + (getter ? owned(expression, from,
                    imports.context()) : expression) + ")");
        }
        if (to instanceof RustType.Optional target && from instanceof RustType.Optional source
                && (target.inner() instanceof RustType.Dyn || target.inner() instanceof RustType.AnyValue)
                && concrete(source.inner())) {
            final String value = getter ? owned("v", source.inner(), imports.context()) : "v";
            return Optional.of(expression + ".map(|v| " + imports.external("std::sync::Arc") + "::new(" + value
                    + ") as " + target.inner().render(imports) + ")");
        }
        if (to instanceof RustType.Sealed sealed && concrete(from)) {
            return Optional.of(imports.type(sealed.name()) + "::from(" + (getter ? owned(expression, from,
                    imports.context()) : expression) + ")");
        }
        return Optional.empty();
    }

    /** The Rust types of concrete values (no trait object, no {@code Option}). */
    private static final Set<Class<?>> CONCRETE = Set.of(RustType.Struct.class, RustType.Enum.class,
            RustType.Sealed.class, RustType.Primitive.class, RustType.Text.class, RustType.Bytes.class,
            RustType.VecOf.class, RustType.SetOf.class, RustType.MapOf.class);

    private static boolean concrete(final RustType type) {
        return CONCRETE.contains(type.getClass());
    }

    /** The owned value of a getter result. */
    static String owned(final String getter, final RustType storage, final RustContext context) {
        return switch (storage) {
            case RustType.Optional optional -> switch (optional.inner()) {
                case RustType.Text ignored -> getter + ".map(str::to_string)";
                case RustType.Bytes ignored -> getter + ".map(<[u8]>::to_vec)";
                case RustType.VecOf ignored -> getter + ".map(<[_]>::to_vec)";
                case RustType.Pairs ignored -> getter + ".map(<[_]>::to_vec)";
                case RustType inner when RustMembers.isCopy(inner, context) || RustMembers.isShared(inner) -> getter;
                default -> getter + ".cloned()";
            };
            case RustType.Text ignored -> getter + ".to_string()";
            case RustType.Bytes ignored -> getter + ".to_vec()";
            case RustType.VecOf ignored -> getter + ".to_vec()";
            case RustType.Pairs ignored -> getter + ".to_vec()";
            case RustType.SelfType ignored -> getter;
            case RustType type when RustMembers.isCopy(type, context) || RustMembers.isShared(type) -> getter;
            default -> getter + ".clone()";
        };
    }

    /** {@code From<Sub> for Super}: a struct that extends another struct converts to it. */
    private static String fromImpl(final TypeDefinition type, final Type.DeclaredType supertype,
                                   final Map<Type.TypeVariable, RustType> scope, final String generics,
                                   final RustImports imports) {
        final RustContext context = imports.context();
        final TypeDefinition definition = context.model().definition(supertype);
        final RustType target = context.rustType(supertype, scope);
        final Map<Type.TypeVariable, RustType> superScope = context.implScope(definition, supertype, scope);
        final List<String> values = new ArrayList<>();
        for (final FieldDefinition field : definition.fields()) {
            final FieldDefinition own = type.field(field.name()).orElseThrow();
            final RustType from = RustMembers.storage(own, scope, context);
            final RustType to = RustMembers.storage(field, superScope, context);
            final String value = owned("value." + RustNames.member(field.name()) + "()", from, context);
            values.add(convert(value, from, to, false, imports).orElse("todo!()"));
        }
        final String name = target.render(imports);
        return "impl" + boundedGenerics(type, context, imports, "Clone") + " From<" + type.name().name() + generics + "> for " + name + " {\n"
                + INDENT + "fn from(value: " + type.name().name() + generics + ") -> Self {\n"
                + INDENT + INDENT + imports.type(supertype.name()) + "::new(" + String.join(", ", values) + ")"
                + (context.isFallible(supertype.name()) ? ".expect(\"the values of a subtype are valid\")" : "")
                + "\n" + INDENT + "}\n}\n";
    }

    // --- traits --------------------------------------------------------------------------------------

    private static String trait(final TypeDefinition.ComplexTypeDefinition type, final RustImports imports) {
        final RustContext context = imports.context();
        final Map<Type.TypeVariable, RustType> scope = context.scope(type);
        final String generics = generics(context.keptParameters(type), context);
        final String name = type.name().name();
        final List<String> supertraits = new ArrayList<>();
        for (final Type.DeclaredType supertype : context.supertraits(type)) {
            supertraits.add(new RustType.Dyn(supertype.name(), context.keptArguments(supertype, scope))
                    .render(imports).replaceFirst("^.*?<dyn ", "").replaceFirst(">$", ""));
        }
        supertraits.add(imports.external("std::fmt::Debug"));
        supertraits.add("Send");
        supertraits.add("Sync");
        if (type.declaredMethods().stream().anyMatch(RustContext::isDisplay)) {
            supertraits.add(imports.external("std::fmt::Display"));
        }
        final StringBuilder out = new StringBuilder();
        out.append(RustDoc.render("", List.of(type.documentation()), type.hasAnnotation("deprecated")));
        out.append("pub trait ").append(name).append(generics).append(": ").append(String.join(" + ", supertraits))
                .append(" {\n");
        final List<String> members = new ArrayList<>();
        for (final FieldDefinition field : context.traitFields(type)) {
            members.add(RustDoc.render(INDENT, List.of(field.documentation().isBlank()
                    ? "Returns the `" + field.name() + "`." : field.documentation()), field.hasAnnotation("deprecated"))
                    + INDENT + RustMembers.traitGetter(field, scope, imports) + ";\n");
            if (!field.hasAnnotation("immutable")) {
                members.add(INDENT + "/// Sets the `" + field.name() + "`.\n" + INDENT
                        + RustMembers.traitSetter(field, scope, imports) + ";\n");
            }
        }
        for (final MethodDefinition method : context.traitMethods(type)) {
            members.add(RustMembers.render(method, RustMembers.signature(method, context.methodName(method), scope,
                    RustMembers.Kind.TRAIT, imports), RustMembers.Kind.TRAIT, null, INDENT));
        }
        out.append(String.join("\n", members)).append("}\n");

        final List<MethodDefinition> statics = type.declaredMethods().stream().filter(MethodDefinition::isStatic)
                .toList();
        if (!statics.isEmpty()) {
            out.append("\n#[allow(unused_variables)]\nimpl").append(generics).append(" dyn ").append(name)
                    .append(generics).append(" {\n");
            out.append(statics.stream().map(m -> RustMembers.render(m, RustMembers.signature(m, context.methodName(m),
                    scope, RustMembers.Kind.DYN_STATIC, imports), RustMembers.Kind.DYN_STATIC,
                    RustMembers.stub(name, m), INDENT)).collect(Collectors.joining("\n")));
            out.append("}\n");
        }
        final List<MethodDefinition> finals = type.declaredMethods().stream()
                .filter(m -> !m.isStatic() && m.hasAnnotation("finalMethod")).toList();
        if (!finals.isEmpty()) {
            final String extension = name + "Ext";
            imports.reserve(extension);
            out.append("\n/// The final methods of `").append(name).append("`, available on every implementation.\n");
            out.append("#[allow(unused_variables)]\npub trait ").append(extension).append(generics).append(": ")
                    .append(name).append(generics).append(" {\n");
            out.append(finals.stream().map(m -> RustMembers.render(m, RustMembers.signature(m, context.methodName(m),
                    scope, RustMembers.Kind.EXTENSION, imports), RustMembers.Kind.EXTENSION,
                    RustMembers.stub(name, m), INDENT)).collect(Collectors.joining("\n")));
            out.append("}\n\n");
            final List<String> parameters = new ArrayList<>(context.keptParameters(type).stream()
                    .map(p -> context.variableName(p.name())).toList());
            final String implementation = "ExtSelf";
            parameters.add(implementation + ": " + name + generics + " + ?Sized");
            out.append("impl<").append(String.join(", ", parameters)).append("> ").append(extension).append(generics)
                    .append(" for ").append(implementation).append(" {}\n");
        }
        return out.toString();
    }

    // --- sealed abstractions -------------------------------------------------------------------------

    private static String sealed(final TypeDefinition.ComplexTypeDefinition type, final RustImports imports) {
        final RustContext context = imports.context();
        final String name = type.name().name();
        final List<QualifiedName> variants = context.sealedVariants(type).stream().filter(context::isGenerated)
                .toList();
        final RustContext.Capabilities caps = context.definitionCapabilities(type.name());
        final StringBuilder out = new StringBuilder();
        out.append(RustDoc.render("", List.of(type.documentation()), type.hasAnnotation("deprecated")));
        out.append(derives(true, caps));
        out.append("pub enum ").append(name).append(" {\n");
        final Map<QualifiedName, String> payloads = new LinkedHashMap<>();
        for (final QualifiedName variant : variants) {
            final String payload = (context.isAbstraction(variant) ? new RustType.Dyn(variant, List.of())
                    : new RustType.Struct(variant, List.of())).render(imports);
            payloads.put(variant, payload);
            final TypeDefinition definition = context.model().type(variant).orElseThrow();
            out.append(RustDoc.render(INDENT, List.of(definition.documentation())));
            out.append(INDENT).append(variant.name()).append('(').append(payload).append("),\n");
        }
        out.append("}\n");
        final Map<Type.TypeVariable, RustType> scope = context.scope(type);
        final List<String> members = new ArrayList<>();
        for (final FieldDefinition field : type.fields()) {
            final RustType storage = RustMembers.storage(field, scope, context);
            final RustMembers.Getter getter = RustMembers.getter(storage, "x", imports);
            final StringBuilder body = new StringBuilder("match self {\n");
            for (final QualifiedName variant : variants) {
                final TypeDefinition definition = context.model().type(variant).orElseThrow();
                final RustType variantStorage = definition.field(field.name())
                        .map(f -> RustMembers.storage(f, context.scope(definition), context)).orElse(storage);
                final String value = convert("v." + RustNames.member(field.name()) + "()", variantStorage, storage,
                        true, imports).orElse("todo!()");
                body.append(INDENT).append(INDENT).append(INDENT).append(name).append("::").append(variant.name())
                        .append("(v) => ").append(value).append(",\n");
            }
            body.append(INDENT).append(INDENT).append('}');
            members.add(RustDoc.render(INDENT, List.of(field.documentation().isBlank()
                    ? "Returns the `" + field.name() + "`." : field.documentation())) + INDENT + "pub fn "
                    + RustNames.member(field.name()) + "(&self) -> " + getter.type() + " {\n" + INDENT + INDENT
                    + (variants.isEmpty() ? "match *self {}" : body) + "\n" + INDENT + "}\n");
        }
        for (final MethodDefinition method : type.methods()) {
            if (RustContext.isDisplay(method)) {
                continue;
            }
            members.add(RustMembers.render(method, RustMembers.signature(method, context.methodName(method), scope,
                    RustMembers.Kind.INHERENT, imports), RustMembers.Kind.INHERENT, RustMembers.stub(name, method),
                    INDENT));
        }
        if (!members.isEmpty()) {
            out.append("\n#[allow(unused_variables)]\nimpl ").append(name).append(" {\n")
                    .append(String.join("\n", members)).append("}\n");
        }
        display(type, "", imports).ifPresent(d -> out.append('\n').append(d));
        payloads.forEach((variant, payload) -> out.append("\nimpl From<").append(payload).append("> for ").append(name)
                .append(" {\n").append(INDENT).append("fn from(value: ").append(payload).append(") -> Self {\n")
                .append(INDENT).append(INDENT).append(name).append("::").append(variant.name()).append("(value)\n")
                .append(INDENT).append("}\n}\n"));
        return out.toString();
    }

    // --- enums ---------------------------------------------------------------------------------------

    private static String enumType(final TypeDefinition.EnumDefinition type, final RustImports imports) {
        final RustContext context = imports.context();
        final String name = type.name().name();
        final List<EnumValueDefinition> values = type.values();
        final StringBuilder out = new StringBuilder();
        out.append(RustDoc.render("", List.of(type.documentation()), type.hasAnnotation("deprecated")));
        out.append("#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]\n");
        out.append("pub enum ").append(name).append(" {\n");
        for (final EnumValueDefinition value : values) {
            out.append(RustDoc.render(INDENT, List.of(value.documentation()), value.hasAnnotation("deprecated")));
            out.append(INDENT).append(RustNames.variant(value.name())).append(",\n");
        }
        out.append("}\n");

        final Map<MethodDefinition, String> names = inherentNames(type, context);
        final String nameMethod = "name";
        final String valuesMethod = "values";
        final String list = values.stream().map(v -> name + "::" + RustNames.variant(v.name()))
                .collect(Collectors.joining(", "));
        out.append("\n#[allow(unused_variables)]\nimpl ").append(name).append(" {\n");
        out.append(INDENT).append("/// Returns all constants, in the order of the specs.\n");
        out.append(INDENT).append("pub fn ").append(valuesMethod).append("() -> &'static [").append(name)
                .append("] {\n").append(INDENT).append(INDENT).append("&[").append(list).append("]\n").append(INDENT)
                .append("}\n\n");
        out.append(INDENT).append("/// Returns the name of the constant in the specs.\n");
        out.append(INDENT).append("pub const fn ").append(nameMethod).append("(&self) -> &'static str {\n");
        out.append(match(name, values, v -> RustLiterals.quote(v.name()), INDENT + INDENT));
        out.append(INDENT).append("}\n");
        for (int i = 0; i < type.attributes().size(); i++) {
            final ParameterDefinition attribute = type.attributes().get(i);
            final int index = i;
            final RustType rustType = context.rustType(attribute.type(), Map.of());
            final boolean text = rustType instanceof RustType.Text;
            final boolean constant = text || RustType.isConstant(rustType) || rustType instanceof RustType.Enum;
            out.append('\n').append(INDENT).append("/// Returns the `").append(attribute.name()).append("`.\n");
            out.append(INDENT).append(constant ? "pub const fn " : "pub fn ").append(RustNames.member(attribute.name()))
                    .append("(&self) -> ").append(text ? "&'static str" : rustType.render(imports)).append(" {\n");
            out.append(match(name, values, v -> {
                final Literal literal = v.arguments().get(index); // the validator checks the number of arguments
                return constant ? RustLiterals.constant(literal, attribute.type(), imports)
                        : RustLiterals.expression(literal, attribute.type(), false, imports);
            }, INDENT + INDENT));
            out.append(INDENT).append("}\n");
        }
        for (final Map.Entry<MethodDefinition, String> method : names.entrySet()) {
            out.append('\n').append(RustMembers.render(method.getKey(), RustMembers.signature(method.getKey(),
                    method.getValue(), Map.of(), RustMembers.Kind.INHERENT, imports), RustMembers.Kind.INHERENT,
                    RustMembers.stub(name, method.getKey()), INDENT));
        }
        out.append("}\n");

        // Display, FromStr
        final Optional<String> display = display(type, "", imports);
        if (display.isPresent()) {
            out.append('\n').append(display.get());
        } else {
            out.append("\nimpl ").append(imports.external("std::fmt::Display")).append(" for ").append(name)
                    .append(" {\n").append(INDENT)
                    .append("fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {\n")
                    .append(INDENT).append(INDENT).append("f.write_str(self.").append(nameMethod).append("())\n")
                    .append(INDENT).append("}\n}\n");
        }
        final String error = imports.support("InvalidArgumentError");
        out.append("\nimpl ").append(imports.external("std::str::FromStr")).append(" for ").append(name).append(" {\n")
                .append(INDENT).append("type Err = ").append(error).append(";\n\n")
                .append(INDENT).append("fn from_str(value: &str) -> Result<Self, Self::Err> {\n")
                .append(INDENT).append(INDENT).append("match value {\n");
        for (final EnumValueDefinition value : values) {
            out.append(INDENT).append(INDENT).append(INDENT).append(RustLiterals.quote(value.name())).append(" => Ok(")
                    .append(name).append("::").append(RustNames.variant(value.name())).append("),\n");
        }
        out.append(INDENT).append(INDENT).append(INDENT).append("_ => Err(").append(error).append("::new(format!(")
                .append(RustLiterals.quote("unknown " + name + ": {value}")).append("))),\n")
                .append(INDENT).append(INDENT).append("}\n").append(INDENT).append("}\n}\n");
        for (final Type.DeclaredType trait : context.traits(type)) {
            if (context.isGenerated(trait.name())) {
                out.append('\n').append(traitImpl(type, trait, Map.of(), names, imports));
            }
        }
        return out.toString();
    }

    private static String match(final String name, final List<EnumValueDefinition> values,
                                final java.util.function.Function<EnumValueDefinition, String> arm,
                                final String indent) {
        if (values.isEmpty()) {
            return indent + "match *self {}\n";
        }
        final StringBuilder out = new StringBuilder(indent).append("match self {\n");
        for (final EnumValueDefinition value : values) {
            out.append(indent).append(INDENT).append(name).append("::").append(RustNames.variant(value.name()))
                    .append(" => ").append(arm.apply(value)).append(",\n");
        }
        return out.append(indent).append("}\n").toString();
    }

    // --- generics ------------------------------------------------------------------------------------

    static String generics(final List<TypeParameterDefinition> parameters, final RustContext context) {
        return parameters.isEmpty() ? "" : "<" + parameters.stream().map(p -> context.variableName(p.name()))
                .collect(Collectors.joining(", ")) + ">";
    }

    private static String boundedGenerics(final TypeDefinition type, final RustContext context,
                                          final RustImports imports, final String bound) {
        final List<TypeParameterDefinition> parameters = context.keptParameters(type);
        if (parameters.isEmpty()) {
            return "";
        }
        final String rendered = bound == null ? null : bound.replace("std::fmt::Debug",
                imports.external("std::fmt::Debug"));
        return "<" + parameters.stream().map(p -> context.variableName(p.name())
                + (rendered == null ? "" : ": " + rendered)).collect(Collectors.joining(", ")) + ">";
    }

}
