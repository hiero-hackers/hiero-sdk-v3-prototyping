package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.ConstantDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.NamespaceDefinition;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates the module of a namespace: {@code mod.rs} with the private modules of the types and the re-exports of
 * their items, {@code functions.rs}, {@code constants.rs} and {@code errors.rs}.
 */
final class RustNamespaceGenerator {

    private static final String INDENT = RustTypeGenerator.INDENT;

    private RustNamespaceGenerator() {
    }

    static Optional<GeneratedFile> functions(final String namespace, final RustContext context) {
        final List<FunctionDefinition> functions = context.functions(namespace);
        if (functions.isEmpty()) {
            return Optional.empty();
        }
        final String folder = context.folder(namespace);
        final RustImports imports = new RustImports(context, folder, false);
        functions.forEach(f -> imports.reserve(context.methodName(f.method())));
        final List<String> declarations = new ArrayList<>();
        for (final FunctionDefinition function : functions) {
            final MethodDefinition method = function.method();
            final RustMembers.Signature signature = RustMembers.signature(method, context.methodName(method), Map.of(),
                    RustMembers.Kind.FUNCTION, imports);
            declarations.add(RustMembers.render(method, signature, RustMembers.Kind.FUNCTION,
                    RustMembers.stub(namespace, method), "").replaceFirst("(?m)^pub ", "#[allow(unused_variables)]\npub "));
        }
        return Optional.of(RustGenerator.file(directory(namespace, context) + "/functions.rs", imports,
                String.join("\n", declarations)));
    }

    static Optional<GeneratedFile> constants(final String namespace, final RustContext context) {
        final List<ConstantDefinition> constants = context.constants(namespace);
        if (constants.isEmpty()) {
            return Optional.empty();
        }
        final RustImports imports = new RustImports(context, context.folder(namespace), false);
        final List<String> declarations = new ArrayList<>();
        for (final ConstantDefinition constant : constants) {
            final RustType type = context.rustType(constant.type(), Map.of());
            final String doc = RustDoc.render("", List.of(constant.documentation()),
                    constant.hasAnnotation("deprecated"));
            final String name = constant.name().name();
            if (type instanceof RustType.Text) {
                declarations.add(doc + "pub const " + name + ": &str = " + RustLiterals.constant(constant.value(),
                        constant.type(), imports) + ";\n");
            } else if (RustType.isConstant(type) || type instanceof RustType.Enum) {
                declarations.add(doc + "pub const " + name + ": " + type.render(imports) + " = "
                        + RustLiterals.constant(constant.value(), constant.type(), imports) + ";\n");
            } else {
                final String lazy = imports.external("std::sync::LazyLock");
                declarations.add(doc + "pub static " + name + ": " + lazy + "<" + type.render(imports) + "> = "
                        + lazy + "::new(|| " + RustLiterals.expression(constant.value(), constant.type(), false, imports)
                        + ");\n");
            }
        }
        return Optional.of(RustGenerator.file(directory(namespace, context) + "/constants.rs", imports,
                String.join("\n", declarations)));
    }

    static Optional<GeneratedFile> errors(final String namespace, final RustContext context) {
        final RustImports imports = new RustImports(context, context.folder(namespace), false);
        final List<String> declarations = new ArrayList<>();
        for (final Map.Entry<String, RustContext.ErrorType> error : context.errors().entrySet()) {
            if (namespace.equals(error.getValue().namespace())) {
                imports.declare(new org.hiero.sdk.v3.metalang.model.QualifiedName(namespace, error.getValue().name()));
                declarations.add(errorStruct(error.getKey(), error.getValue().name(), imports));
            }
        }
        for (final RustContext.ErrorEnum errors : context.errorEnums(namespace)) {
            imports.declare(new org.hiero.sdk.v3.metalang.model.QualifiedName(namespace, errors.name()));
            declarations.add(errorEnum(errors, imports));
        }
        if (declarations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(RustGenerator.file(directory(namespace, context) + "/errors.rs", imports,
                String.join("\n", declarations)));
    }

    private static String errorStruct(final String errorId, final String name, final RustImports imports) {
        final String error = "dyn " + imports.external("std::error::Error") + " + Send + Sync";
        return "/// The error `" + errorId + "`.\n"
                + "#[derive(Debug)]\n"
                + "pub struct " + name + " {\n"
                + INDENT + "message: String,\n"
                + INDENT + "source: Option<Box<" + error + ">>,\n"
                + "}\n\n"
                + "impl " + name + " {\n"
                + INDENT + "/// Creates a new `" + name + "`.\n"
                + INDENT + "pub fn new(message: impl Into<String>) -> Self {\n"
                + INDENT + INDENT + "Self { message: message.into(), source: None }\n"
                + INDENT + "}\n\n"
                + INDENT + "/// Creates a new `" + name + "` caused by another error.\n"
                + INDENT + "pub fn with_source(message: impl Into<String>, source: impl Into<Box<" + error
                + ">>) -> Self {\n"
                + INDENT + INDENT + "Self { message: message.into(), source: Some(source.into()) }\n"
                + INDENT + "}\n\n"
                + INDENT + "/// Returns the description of the problem.\n"
                + INDENT + "pub fn message(&self) -> &str {\n"
                + INDENT + INDENT + "&self.message\n"
                + INDENT + "}\n"
                + "}\n\n"
                + "impl " + imports.external("std::fmt::Display") + " for " + name + " {\n"
                + INDENT + "fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {\n"
                + INDENT + INDENT + "f.write_str(&self.message)\n"
                + INDENT + "}\n"
                + "}\n\n"
                + "impl " + imports.external("std::error::Error") + " for " + name + " {\n"
                + INDENT + "fn source(&self) -> Option<&(dyn " + imports.external("std::error::Error")
                + " + 'static)> {\n"
                + INDENT + INDENT + "self.source.as_deref().map(|e| e as &(dyn " + imports.external("std::error::Error")
                + " + 'static))\n"
                + INDENT + "}\n"
                + "}\n";
    }

    private static String errorEnum(final RustContext.ErrorEnum errors, final RustImports imports) {
        final String name = errors.name();
        final StringBuilder out = new StringBuilder("/// The errors of `").append(name, 0, name.length() - 5)
                .append("`.\n#[derive(Debug)]\npub enum ").append(name).append(" {\n");
        final Map<String, String> variants = new TreeMap<>();
        errors.errors().forEach((id, type) -> {
            final String variant = RustNames.errorType(id).replaceFirst("Error$", "");
            final String payload = imports.error(type);
            if (!variants.containsValue(payload)) {
                variants.put(variant, payload);
                out.append(INDENT).append("/// The error `").append(id).append("`.\n").append(INDENT).append(variant)
                        .append('(').append(payload).append("),\n");
            }
        });
        out.append("}\n\n");
        out.append("impl ").append(imports.external("std::fmt::Display")).append(" for ").append(name).append(" {\n")
                .append(INDENT).append("fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {\n")
                .append(INDENT).append(INDENT).append("match self {\n");
        variants.keySet().forEach(v -> out.append(INDENT).append(INDENT).append(INDENT).append(name).append("::")
                .append(v).append("(error) => std::fmt::Display::fmt(error, f),\n"));
        out.append(INDENT).append(INDENT).append("}\n").append(INDENT).append("}\n}\n\n");
        out.append("impl ").append(imports.external("std::error::Error")).append(" for ").append(name).append(" {\n")
                .append(INDENT).append("fn source(&self) -> Option<&(dyn ").append(imports.external("std::error::Error"))
                .append(" + 'static)> {\n").append(INDENT).append(INDENT).append("match self {\n");
        variants.keySet().forEach(v -> out.append(INDENT).append(INDENT).append(INDENT).append(name).append("::")
                .append(v).append("(error) => Some(error),\n"));
        out.append(INDENT).append(INDENT).append("}\n").append(INDENT).append("}\n}\n");
        variants.forEach((variant, payload) -> out.append("\nimpl From<").append(payload).append("> for ").append(name)
                .append(" {\n").append(INDENT).append("fn from(error: ").append(payload).append(") -> Self {\n")
                .append(INDENT).append(INDENT).append(name).append("::").append(variant).append("(error)\n")
                .append(INDENT).append("}\n}\n"));
        return out.toString();
    }

    /**
     * Generates the module files of a crate: {@code mod.rs} of every namespace and of every module that only
     * contains namespaces ({@code consensusnode} for {@code consensusnode.client}).
     *
     * @param folder the spec folder
     * @param files  the files generated so far for the crate
     * @param context the generation context
     * @return the module files and the top-level modules for {@code lib.rs}
     */
    static List<GeneratedFile> modules(final String folder, final List<GeneratedFile> files,
                                       final RustContext context) {
        final Map<String, NamespaceDefinition> namespaces = new TreeMap<>();
        context.folders().stream().filter(f -> f.name().equals(folder)).flatMap(f -> f.namespaces().stream())
                .forEach(n -> namespaces.put(n.name(), n));
        final TreeSet<String> nodes = new TreeSet<>();
        for (final String namespace : namespaces.keySet()) {
            final String[] segments = namespace.split("\\.");
            for (int i = 1; i <= segments.length; i++) {
                nodes.add(String.join(".", java.util.Arrays.copyOf(segments, i)));
            }
        }
        final List<GeneratedFile> result = new ArrayList<>();
        for (final String node : nodes) {
            final StringBuilder rs = new StringBuilder(RustGenerator.HEADER);
            final NamespaceDefinition namespace = namespaces.get(node);
            if (namespace != null) {
                final String description = namespace.sources().stream().map(NamespaceDefinition.Source::description)
                        .filter(d -> !d.isBlank()).reduce((a, b) -> a + "\n\n" + b).orElse("");
                rs.append(RustDoc.module(description.isBlank() ? "The namespace `" + node + "`." : description));
            } else {
                rs.append("//! The namespaces `").append(node).append(".*`.\n");
            }
            rs.append('\n');
            final List<String> modules = new ArrayList<>();
            final List<String> exports = new ArrayList<>();
            if (namespace != null) {
                final String directory = directory(node, context) + "/";
                for (final TypeDefinition type : context.model().types(node)) {
                    if (context.isGenerated(type.name())) {
                        final String module = context.fileModule(type);
                        modules.add("mod " + module + ";");
                        final List<String> items = new ArrayList<>(List.of(type.name().name()));
                        if (context.isTrait(type.name()) && type.declaredMethods().stream()
                                .anyMatch(m -> !m.isStatic() && m.hasAnnotation("finalMethod"))) {
                            items.add(type.name().name() + "Ext");
                        }
                        exports.add("pub use " + module + "::" + braces(items) + ";");
                    }
                }
                for (final String stem : List.of("functions", "constants", "errors")) {
                    files.stream().filter(f -> f.path().equals(directory + stem + ".rs")).findFirst().ifPresent(f -> {
                        modules.add("mod " + stem + ";");
                        exports.add("pub use " + stem + "::" + braces(items(f.content())) + ";");
                    });
                }
            }
            for (final String child : nodes) {
                if (child.startsWith(node + ".") && !child.substring(node.length() + 1).contains(".")) {
                    modules.add("pub mod " + RustNames.module(child.substring(node.length() + 1)) + ";");
                }
            }
            modules.stream().sorted().forEach(m -> rs.append(m).append('\n'));
            if (!exports.isEmpty()) {
                rs.append('\n');
                exports.stream().sorted().forEach(e -> rs.append(e).append('\n'));
            }
            result.add(new GeneratedFile(RustNames.crateDirectory(folder) + "/src/" + RustNames.directory(node)
                    + "/mod.rs", rs.toString()));
        }
        return result;
    }

    /** The top-level modules of a crate. */
    static List<String> roots(final String folder, final RustContext context) {
        final TreeSet<String> roots = new TreeSet<>();
        context.folders().stream().filter(f -> f.name().equals(folder)).flatMap(f -> f.namespaces().stream())
                .forEach(n -> roots.add(RustNames.module(n.name().split("\\.")[0])));
        return List.copyOf(roots);
    }

    /** The public items of a generated file: {@code pub fn x}, {@code pub const X}, {@code pub struct X}, ... */
    static List<String> items(final String content) {
        final List<String> items = new ArrayList<>();
        final java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "(?m)^pub (?:async )?(?:fn|const|static|struct|enum|trait|type) (r#\\w+|\\w+)").matcher(content);
        while (matcher.find()) {
            items.add(matcher.group(1));
        }
        return items;
    }

    private static String braces(final List<String> items) {
        return items.size() == 1 ? items.getFirst() : "{" + String.join(", ", items) + "}";
    }

    static String directory(final String namespace, final RustContext context) {
        return RustNames.crateDirectory(context.folder(namespace)) + "/src/" + RustNames.directory(namespace);
    }
}
