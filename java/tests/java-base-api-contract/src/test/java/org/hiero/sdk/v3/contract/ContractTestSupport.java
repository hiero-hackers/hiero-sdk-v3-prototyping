package org.hiero.sdk.v3.contract;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

final class ContractTestSupport {
    static final Path JAVA_ROOT = Path.of("..", "..").toAbsolutePath().normalize();
    static final Path REPOSITORY_ROOT = JAVA_ROOT.getParent();
    static final Path API_SOURCES = JAVA_ROOT.resolve("hiero-sdk-base-api/src/main/java");
    static final Path API_CLASSES = JAVA_ROOT.resolve("hiero-sdk-base-api/target/classes");
    static final Path DOCS = JAVA_ROOT.resolve("docs/base-api");
    static final Path BASE_SPEC = REPOSITORY_ROOT.resolve("spec/base");

    private ContractTestSupport() {}

    static List<Path> javaSources() throws IOException {
        try (Stream<Path> paths = Files.walk(API_SOURCES)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }

    static List<Class<?>> apiTypes() throws IOException, ClassNotFoundException {
        final List<Class<?>> types = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(API_CLASSES)) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".class")).sorted().toList()) {
                final String relative = API_CLASSES.relativize(path).toString();
                if (relative.equals("module-info.class") || relative.contains("$")) {
                    continue;
                }
                final String className = relative.substring(0, relative.length() - ".class".length())
                        .replace(path.getFileSystem().getSeparator(), ".");
                types.add(Class.forName(className));
            }
        }
        return List.copyOf(types);
    }

    static String publicSignatureHash() throws IOException, ClassNotFoundException {
        final List<String> signatures = new ArrayList<>();
        for (Class<?> type : apiTypes()) {
            signatures.add("TYPE " + Modifier.toString(type.getModifiers()) + " " + type.getName()
                    + " extends " + type.getGenericSuperclass()
                    + " implements " + List.of(type.getGenericInterfaces()));
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (!constructor.isSynthetic() && isPublicOrProtected(constructor.getModifiers())) {
                    signatures.add("CTOR " + constructor.toGenericString());
                }
            }
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isSynthetic() && !method.isBridge() && isPublicOrProtected(method.getModifiers())) {
                    signatures.add("METHOD " + method.toGenericString());
                }
            }
            for (Field field : type.getDeclaredFields()) {
                if (!field.isSynthetic() && isPublicOrProtected(field.getModifiers())) {
                    signatures.add("FIELD " + field.toGenericString());
                }
            }
            if (type.isSealed()) {
                signatures.add("PERMITS " + type.getName() + " " + List.of(type.getPermittedSubclasses()));
            }
        }
        signatures.sort(Comparator.naturalOrder());
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(String.join("\n", signatures).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }

    private static boolean isPublicOrProtected(final int modifiers) {
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
    }
}
