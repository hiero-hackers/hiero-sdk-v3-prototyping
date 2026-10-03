package org.hiero.sdk.v3.metalang.check.java;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * The public API declared by a set of Java sources, normalized for a structural comparison: types, their members and
 * the module declarations — without method bodies, private members, formatting, imports and documentation. Type names
 * are resolved to qualified names through the imports and the package, so {@code List} and {@code java.util.List} are
 * the same type. The sources are only parsed (JDK compiler tree API), not compiled, so an implementation can be
 * checked without its dependencies.
 *
 * @param types    the types by qualified name (nested types as {@code Outer.Inner})
 * @param modules  the module declarations by module name
 * @param problems the sources that could not be parsed
 */
public record JavaApi(SortedMap<String, ApiType> types, SortedMap<String, ApiModule> modules,
                      List<ApiDifference> problems) {

    /** The modifiers in the order of the Java Language Specification ({@code public static final}). */
    private static final Comparator<String> MODIFIER_ORDER = Comparator.comparing(
            m -> Modifier.valueOf(m.toUpperCase(Locale.ROOT).replace('-', '_')));

    /** Annotations that are no API contract (compiler hints, serialization). */
    private static final Set<String> IGNORED_ANNOTATIONS = Set.of("Override", "SuppressWarnings", "Serial",
            "SafeVarargs");

    /** Modifiers that belong to the API of a type or member; others (synchronized, native, ...) are implementation. */
    private static final Set<Modifier> API_MODIFIERS = Set.of(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.STATIC,
            Modifier.FINAL, Modifier.ABSTRACT, Modifier.SEALED, Modifier.NON_SEALED);

    /**
     * A type of the API.
     *
     * @param name             the qualified name
     * @param kind             {@code class}, {@code interface}, {@code enum}, {@code record} or {@code annotation}
     * @param modifiers        the API modifiers (implicit ones of the kind removed)
     * @param typeParameters   the type parameters, e.g. {@code <T extends java.lang.Comparable<T>>}
     * @param superclass       the superclass, empty if none
     * @param interfaces       the implemented (or, for interfaces, extended) interfaces
     * @param permits          the permitted subtypes of a sealed type
     * @param recordComponents the components of a record, in order
     * @param enumConstants    the constants of an enum
     * @param annotations      the annotations of the type
     * @param members          the non-private members by key (e.g. {@code name(java.lang.String)})
     * @param file             the source file
     * @param line             the line of the declaration
     */
    public record ApiType(String name, String kind, Set<String> modifiers, String typeParameters, String superclass,
                          Set<String> interfaces, Set<String> permits, List<String> recordComponents,
                          Set<String> enumConstants, Set<String> annotations, SortedMap<String, ApiMember> members,
                          String file, long line) {
    }

    /**
     * A member of a type: method, constructor or field.
     *
     * @param key         the key: name and parameter types, or the field name
     * @param kind        {@code method}, {@code constructor} or {@code field}
     * @param signature   everything that must not change: modifiers, type parameters, result type, parameter types
     *                    (with their nullness), exceptions, the value of a constant
     * @param annotations the annotations of the member
     * @param file        the source file
     * @param line        the line of the declaration
     */
    public record ApiMember(String key, String kind, String signature, Set<String> annotations, String file,
                            long line) {
    }

    /**
     * A module declaration.
     *
     * @param name        the module name
     * @param requires    the required modules with their flags ({@code transitive}, {@code static})
     * @param exports     the exported packages
     * @param annotations the annotations of the module
     * @param file        the source file
     * @param line        the line of the declaration
     */
    public record ApiModule(String name, SortedMap<String, String> requires, Set<String> exports,
                            Set<String> annotations, String file, long line) {
    }

    /**
     * Reads the API of in-memory sources (e.g. generated files).
     *
     * @param sources the sources by path
     * @return the API
     */
    public static JavaApi of(final Map<String, String> sources) {
        final List<JavaFileObject> objects = new ArrayList<>();
        sources.forEach((path, content) -> objects.add(new StringSource(path, content)));
        return parse(objects, null, Set.of());
    }

    /**
     * Reads the API of all {@code .java} files below a directory, except build output ({@code target} directories).
     *
     * @param directory  the directory
     * @param knownTypes qualified names of types that are resolved even if the directory does not declare them
     *                   (the expected types: a missing type must not change how the references to it are read)
     * @return the API
     * @throws IOException if the directory cannot be read
     */
    public static JavaApi of(final Path directory, final Set<String> knownTypes) throws IOException {
        Objects.requireNonNull(directory, "directory must not be null");
        Objects.requireNonNull(knownTypes, "knownTypes must not be null");
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ROOT,
                StandardCharsets.UTF_8); Stream<Path> files = Files.walk(directory)) {
            final List<Path> sources = files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .filter(p -> !isBuildOutput(directory.relativize(p)))
                    .sorted().toList();
            final List<JavaFileObject> objects = new ArrayList<>();
            fileManager.getJavaFileObjectsFromPaths(sources).forEach(objects::add);
            return parse(objects, directory, knownTypes);
        }
    }

    private static boolean isBuildOutput(final Path relative) {
        for (final Path part : relative) {
            if (part.toString().equals("target")) {
                return true;
            }
        }
        return false;
    }

    private static JavaApi parse(final List<JavaFileObject> sources, final Path base, final Set<String> knownTypes) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final JavacTask task = (JavacTask) compiler.getTask(null, null, diagnostics, List.of("-proc:none"), null,
                sources);
        final List<CompilationUnitTree> units = new ArrayList<>();
        final List<ApiDifference> problems = new ArrayList<>();
        try {
            task.parse().forEach(units::add);
        } catch (final IOException e) {
            problems.add(new ApiDifference("", 0, "Cannot read the sources: " + e.getMessage()));
        }
        for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                problems.add(new ApiDifference(path(diagnostic.getSource(), base), diagnostic.getLineNumber(),
                        "Cannot parse: " + diagnostic.getMessage(Locale.ROOT)));
            }
        }
        // all declared types, to resolve simple names of the same package
        final Set<String> index = new HashSet<>(knownTypes);
        for (final CompilationUnitTree unit : units) {
            final String packageName = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
            unit.getTypeDecls().stream().filter(ClassTree.class::isInstance).map(ClassTree.class::cast)
                    .forEach(type -> indexType(type, packageName.isEmpty() ? "" : packageName + ".", index));
        }
        final SourcePositions positions = Trees.instance(task).getSourcePositions();
        final SortedMap<String, ApiType> types = new TreeMap<>();
        final SortedMap<String, ApiModule> modules = new TreeMap<>();
        for (final CompilationUnitTree unit : units) {
            final Reader reader = new Reader(unit, index, positions, path(unit.getSourceFile(), base));
            if (unit.getModule() != null) {
                final ApiModule module = reader.module(unit.getModule());
                modules.put(module.name(), module);
            }
            for (final Tree declaration : unit.getTypeDecls()) {
                if (declaration instanceof ClassTree type) {
                    reader.type(type, reader.packagePrefix(), false, List.of(), types, problems);
                }
            }
        }
        return new JavaApi(types, modules, List.copyOf(problems));
    }

    private static void indexType(final ClassTree type, final String prefix, final Set<String> index) {
        final String name = prefix + type.getSimpleName();
        index.add(name);
        type.getMembers().stream().filter(ClassTree.class::isInstance).map(ClassTree.class::cast)
                .forEach(nested -> indexType(nested, name + ".", index));
    }

    private static String path(final JavaFileObject source, final Path base) {
        if (source == null) {
            return "";
        }
        final URI uri = source.toUri();
        if (uri.getScheme().equals(StringSource.SCHEME)) {
            return uri.getPath().substring(1);
        }
        final Path path = Path.of(uri);
        return (base == null ? path : base.toAbsolutePath().normalize().relativize(path.toAbsolutePath().normalize()))
                .toString().replace('\\', '/');
    }

    /** Reads the declarations of one compilation unit. */
    private static final class Reader {

        private final CompilationUnitTree unit;
        private final Set<String> index;
        private final SourcePositions positions;
        private final String file;
        private final String packageName;
        private final Map<String, String> singleImports = new HashMap<>();
        private final List<String> starImports = new ArrayList<>();
        private final Map<String, Boolean> loadable = new HashMap<>();
        /** The qualified names of the enclosing types, innermost last. */
        private final List<String> enclosing = new ArrayList<>();

        private Reader(final CompilationUnitTree unit, final Set<String> index, final SourcePositions positions,
                       final String file) {
            this.unit = unit;
            this.index = index;
            this.positions = positions;
            this.file = file;
            this.packageName = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
            for (final ImportTree imported : unit.getImports()) {
                if (imported.isStatic()) {
                    continue;
                }
                final String name = imported.getQualifiedIdentifier().toString();
                if (name.endsWith(".*")) {
                    starImports.add(name.substring(0, name.length() - 2));
                } else {
                    singleImports.put(name.substring(name.lastIndexOf('.') + 1), name);
                }
            }
        }

        private String packagePrefix() {
            return packageName.isEmpty() ? "" : packageName + ".";
        }

        private long line(final Tree tree) {
            final long position = positions.getStartPosition(unit, tree);
            return position < 0 ? 0 : unit.getLineMap().getLineNumber(position);
        }

        private ApiModule module(final ModuleTree module) {
            final SortedMap<String, String> requires = new TreeMap<>();
            final Set<String> exports = new TreeSet<>();
            for (final DirectiveTree directive : module.getDirectives()) {
                if (directive instanceof RequiresTree required) {
                    requires.put(required.getModuleName().toString(),
                            ((required.isTransitive() ? "transitive " : "") + (required.isStatic() ? "static" : ""))
                                    .strip());
                } else if (directive instanceof ExportsTree exported) {
                    exports.add(exported.getPackageName().toString());
                }
            }
            return new ApiModule(module.getName().toString(), requires, exports,
                    annotations(module.getAnnotations(), List.of()), file, line(module));
        }

        private void type(final ClassTree type, final String prefix, final boolean inInterface,
                          final List<String> outerVariables, final SortedMap<String, ApiType> types,
                          final List<ApiDifference> problems) {
            if (type.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                return;
            }
            final String name = prefix + type.getSimpleName();
            enclosing.add(name);
            try {
                type(type, name, prefix, inInterface, outerVariables, types, problems);
            } finally {
                enclosing.removeLast();
            }
        }

        private void type(final ClassTree type, final String name, final String prefix, final boolean inInterface,
                          final List<String> outerVariables, final SortedMap<String, ApiType> types,
                          final List<ApiDifference> problems) {
            final String kind = switch (type.getKind()) {
                case INTERFACE -> "interface";
                case ENUM -> "enum";
                case RECORD -> "record";
                case ANNOTATION_TYPE -> "annotation";
                default -> "class";
            };
            final List<String> variables = new ArrayList<>(outerVariables);
            type.getTypeParameters().forEach(p -> variables.add(p.getName().toString()));
            final Set<String> modifiers = modifiers(type.getModifiers());
            switch (kind) {
                case "interface", "annotation" -> modifiers.remove("abstract");
                case "record", "enum" -> modifiers.remove("final");
                default -> {
                }
            }
            if (!prefix.equals(packagePrefix()) && !kind.equals("class")) {
                modifiers.remove("static"); // nested interfaces, enums and records are implicitly static
            }
            if (inInterface) {
                modifiers.addAll(List.of("public", "static")); // types in interfaces are implicitly public static
                if (!kind.equals("class")) {
                    modifiers.remove("static");
                }
            }
            final boolean isInterface = kind.equals("interface") || kind.equals("annotation");
            final SortedMap<String, ApiMember> members = new TreeMap<>();
            final List<String> components = new ArrayList<>();
            final Set<String> constants = new TreeSet<>();
            for (final Tree member : type.getMembers()) {
                switch (member) {
                    case ClassTree nested -> type(nested, name + ".", isInterface, variables, types, problems);
                    case MethodTree method -> method(method, type, isInterface, kind, components, variables, members);
                    case VariableTree field -> {
                        if (kind.equals("enum") && field.getInitializer() instanceof NewClassTree created
                                && created.getIdentifier().toString().equals(type.getSimpleName().toString())) {
                            constants.add(field.getName().toString());
                        } else if (kind.equals("record") && !field.getModifiers().getFlags().contains(Modifier.STATIC)) {
                            // records have no instance fields besides their components
                            components.add(nullness(field.getModifiers(), variables) + type(field.getType(), variables)
                                    + " " + field.getName());
                        } else {
                            field(field, isInterface, variables, members);
                        }
                    }
                    default -> {
                    }
                }
            }
            final String superclass = isInterface || type.getExtendsClause() == null ? ""
                    : type(type.getExtendsClause(), variables);
            final Set<String> interfaces = type.getImplementsClause().stream().map(t -> type(t, variables))
                    .collect(Collectors.toCollection(TreeSet::new));
            if (isInterface && type.getExtendsClause() != null) {
                interfaces.add(type(type.getExtendsClause(), variables));
            }
            final Set<String> permits = type.getPermitsClause().stream().map(t -> type(t, variables))
                    .collect(Collectors.toCollection(TreeSet::new));
            if (types.containsKey(name)) {
                problems.add(new ApiDifference(file, line(type), "Type " + name + " is also declared in "
                        + types.get(name).file()));
            }
            types.put(name, new ApiType(name, kind, modifiers, typeParameters(type.getTypeParameters(), variables),
                    superclass, interfaces, permits, List.copyOf(components), constants,
                    annotations(type.getModifiers().getAnnotations(), variables), members, file, line(type)));
        }

        private void method(final MethodTree method, final ClassTree owner, final boolean isInterface,
                            final String ownerKind, final List<String> components, final List<String> outerVariables,
                            final SortedMap<String, ApiMember> members) {
            final Set<Modifier> flags = method.getModifiers().getFlags();
            if (flags.contains(Modifier.PRIVATE)) {
                return;
            }
            final List<String> variables = new ArrayList<>(outerVariables);
            method.getTypeParameters().forEach(p -> variables.add(p.getName().toString()));
            final boolean constructor = method.getName().contentEquals("<init>");
            final List<String> parameterTypes = method.getParameters().stream()
                    .map(p -> erase(type(p.getType(), variables))).toList();
            if (constructor && ownerKind.equals("record") && (method.getParameters().isEmpty() && !components.isEmpty()
                    || parameterTypes.equals(components.stream().map(c -> erase(c.substring(0, c.lastIndexOf(' '))))
                    .toList()))) {
                return; // the canonical or compact constructor of a record is implied by its components
            }
            if (constructor && ownerKind.equals("enum")) {
                return; // enum constructors are always private
            }
            final Set<String> modifiers = modifiers(method.getModifiers());
            // abstract, default or implemented is the implementation's choice
            modifiers.remove("abstract");
            if (isInterface) {
                modifiers.add("public");
            }
            final String name = constructor ? owner.getSimpleName().toString() : method.getName().toString();
            final String key = name + "(" + String.join(", ", parameterTypes) + ")";
            final String parameters = method.getParameters().stream()
                    .map(p -> nullness(p.getModifiers(), variables) + type(p.getType(), variables))
                    .collect(Collectors.joining(", "));
            final String result = constructor ? ""
                    : nullness(method.getModifiers(), variables) + type(method.getReturnType(), variables) + " ";
            final String exceptions = method.getThrows().isEmpty() ? "" : " throws " + method.getThrows().stream()
                    .map(t -> type(t, variables)).sorted().collect(Collectors.joining(", "));
            final String signature = String.join(" ", modifiers) + " "
                    + typeParameters(method.getTypeParameters(), variables) + result + name + "(" + parameters + ")"
                    + exceptions;
            members.put(key, new ApiMember(key, constructor ? "constructor" : "method", signature.strip(),
                    annotations(method.getModifiers().getAnnotations(), variables), file, line(method)));
        }

        private void field(final VariableTree field, final boolean isInterface, final List<String> variables,
                           final SortedMap<String, ApiMember> members) {
            if (field.getModifiers().getFlags().contains(Modifier.PRIVATE)
                    || !isInterface && !field.getModifiers().getFlags().contains(Modifier.PUBLIC)
                    && !field.getModifiers().getFlags().contains(Modifier.PROTECTED)) {
                return;
            }
            final Set<String> modifiers = modifiers(field.getModifiers());
            if (isInterface) {
                modifiers.addAll(List.of("public", "static", "final"));
            }
            final String value = field.getInitializer() == null ? "" : " = " + field.getInitializer();
            final String signature = String.join(" ", modifiers) + " "
                    + nullness(field.getModifiers(), variables) + type(field.getType(), variables) + " "
                    + field.getName() + value;
            members.put(field.getName().toString(), new ApiMember(field.getName().toString(), "field",
                    signature.strip(), annotations(field.getModifiers().getAnnotations(), variables), file,
                    line(field)));
        }

        private Set<String> modifiers(final ModifiersTree modifiers) {
            return modifiers.getFlags().stream().filter(API_MODIFIERS::contains)
                    .map(m -> m.toString().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(MODIFIER_ORDER)));
        }

        private String typeParameters(final List<? extends TypeParameterTree> parameters,
                                      final List<String> variables) {
            if (parameters.isEmpty()) {
                return "";
            }
            return "<" + parameters.stream().map(p -> p.getName() + (p.getBounds().isEmpty() ? "" : " extends "
                    + p.getBounds().stream().map(b -> type(b, variables)).collect(Collectors.joining(" & "))))
                    .collect(Collectors.joining(", ")) + "> ";
        }

        /** The nullness annotations of a declaration ({@code @Nullable} written in front of the type). */
        private String nullness(final ModifiersTree modifiers, final List<String> variables) {
            return modifiers.getAnnotations().stream().filter(a -> isNullness(simpleName(a)))
                    .map(a -> annotation(a, variables) + " ").sorted().collect(Collectors.joining());
        }

        private Set<String> annotations(final List<? extends AnnotationTree> annotations,
                                        final List<String> variables) {
            return annotations.stream().filter(a -> !IGNORED_ANNOTATIONS.contains(simpleName(a)))
                    .map(a -> annotation(a, variables)).collect(Collectors.toCollection(TreeSet::new));
        }

        private String annotation(final AnnotationTree annotation, final List<String> variables) {
            final String arguments = annotation.getArguments().isEmpty() ? "" : "(" + annotation.getArguments()
                    .stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
            return "@" + type(annotation.getAnnotationType(), variables) + arguments;
        }

        private static String simpleName(final AnnotationTree annotation) {
            final String name = annotation.getAnnotationType().toString();
            return name.substring(name.lastIndexOf('.') + 1);
        }

        /** The normalized type with all names qualified. */
        private String type(final Tree tree, final List<String> variables) {
            return switch (tree) {
                case null -> "";
                case PrimitiveTypeTree primitive -> primitive.getPrimitiveTypeKind().toString()
                        .toLowerCase(Locale.ROOT);
                case IdentifierTree identifier -> resolve(identifier.getName().toString(), variables);
                case MemberSelectTree select -> qualified(select.toString(), variables);
                case ParameterizedTypeTree parameterized -> type(parameterized.getType(), variables) + "<"
                        + parameterized.getTypeArguments().stream().map(a -> type(a, variables))
                        .collect(Collectors.joining(", ")) + ">";
                case ArrayTypeTree array -> type(array.getType(), variables) + "[]";
                case WildcardTree wildcard -> switch (wildcard.getKind()) {
                    case EXTENDS_WILDCARD -> "? extends " + type(wildcard.getBound(), variables);
                    case SUPER_WILDCARD -> "? super " + type(wildcard.getBound(), variables);
                    default -> "?";
                };
                // String @Nullable [] (nullable array) differs from @Nullable String[] (nullable elements)
                case AnnotatedTypeTree annotated when annotated.getUnderlyingType() instanceof ArrayTypeTree array ->
                        type(array.getType(), variables) + " " + annotations(annotated, variables) + "[]";
                case AnnotatedTypeTree annotated -> annotations(annotated, variables)
                        + type(annotated.getUnderlyingType(), variables);
                case IntersectionTypeTree intersection -> intersection.getBounds().stream()
                        .map(b -> type(b, variables)).collect(Collectors.joining(" & "));
                default -> tree.toString();
            };
        }

        private String annotations(final AnnotatedTypeTree annotated, final List<String> variables) {
            return annotated.getAnnotations().stream().map(a -> annotation(a, variables) + " ").sorted()
                    .collect(Collectors.joining());
        }

        /** A dotted name: the first segment is resolved if it is a type ({@code StreamItem.Success}). */
        private String qualified(final String dotted, final List<String> variables) {
            final int dot = dotted.indexOf('.');
            final String first = dot < 0 ? dotted : dotted.substring(0, dot);
            final String resolved = resolve(first, variables);
            return resolved.equals(first) ? dotted : resolved + dotted.substring(first.length());
        }

        private String resolve(final String simple, final List<String> variables) {
            if (variables.contains(simple)) {
                return simple;
            }
            for (int i = enclosing.size() - 1; i >= 0; i--) {
                if (enclosing.get(i).endsWith("." + simple) || enclosing.get(i).equals(simple)) {
                    return enclosing.get(i);
                }
                if (index.contains(enclosing.get(i) + "." + simple)) {
                    return enclosing.get(i) + "." + simple;
                }
            }
            final String imported = singleImports.get(simple);
            if (imported != null) {
                return imported;
            }
            if (index.contains(packagePrefix() + simple)) {
                return packagePrefix() + simple;
            }
            for (final String star : starImports) {
                if (index.contains(star + "." + simple) || isLoadable(star + "." + simple)) {
                    return star + "." + simple;
                }
            }
            if (isLoadable("java.lang." + simple)) {
                return "java.lang." + simple;
            }
            return simple;
        }

        private boolean isLoadable(final String name) {
            return loadable.computeIfAbsent(name, n -> {
                try {
                    Class.forName(n, false, JavaApi.class.getClassLoader());
                    return true;
                } catch (final ClassNotFoundException | LinkageError e) {
                    return false;
                }
            });
        }
    }

    /** The parameter type without annotations and type arguments, as used in member keys. */
    private static String erase(final String type) {
        final StringBuilder plain = new StringBuilder();
        int depth = 0;
        for (final char c : type.replaceAll("@[\\w.]+(\\([^)]*\\))?\\s*", "").toCharArray()) {
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (depth == 0) {
                plain.append(c);
            }
        }
        return plain.toString().replace(" [", "[").strip();
    }

    private static boolean isNullness(final String annotation) {
        return annotation.equals("Nullable") || annotation.equals("NonNull");
    }

    /**
     * Whether a rendered annotation (e.g. {@code @org.jspecify.annotations.Nullable}) is a nullness annotation; those
     * are part of the signature of a member.
     *
     * @param annotation the annotation
     * @return {@code true} for {@code @Nullable} and {@code @NonNull}
     */
    static boolean isNullnessAnnotation(final String annotation) {
        final String name = annotation.replaceAll("\\(.*", "");
        return isNullness(name.substring(name.lastIndexOf('.') + 1));
    }

    /** A source given as string. */
    private static final class StringSource extends SimpleJavaFileObject {

        private static final String SCHEME = "string";

        private final String content;

        private StringSource(final String path, final String content) {
            super(URI.create(SCHEME + ":///" + path.replace('\\', '/')), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return content;
        }
    }

}
