package org.hiero.sdk.v3.codegen.mapping;

import java.nio.file.Path;
import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

final class MappingSources {

    private static final String ROOT = "org.hiero.sdk.v3";

    private MappingSources() {}

    static JavaSourceFile type(String namespace, String name, String body, String... imports) {
        var packageName = ROOT + "." + namespace.toLowerCase(java.util.Locale.ROOT);
        return new JavaSourceFile(
                Path.of(packageName.replace('.', '/'), name + ".java"),
                sourcePath(namespace),
                namespace + "#" + name,
                packageName,
                List.of(imports),
                body);
    }

    static JavaSourceFile packageInfo(String namespace) {
        var packageName = ROOT + "." + namespace.toLowerCase(java.util.Locale.ROOT);
        return new JavaSourceFile(
                Path.of(packageName.replace('.', '/'), "package-info.java"),
                sourcePath(namespace),
                namespace + "#package",
                "",
                List.of(),
                "/** Public contracts generated from the {@code " + namespace + "} namespace. */\n"
                        + "@org.jspecify.annotations.NullMarked\npackage " + packageName + ";\n");
    }

    static String sourcePath(String namespace) {
        return switch (namespace) {
            case "ledger.config" -> "spec/base/ledger-config.md";
            case "nativeToken" -> "spec/base/native-token.md";
            default -> "spec/base/" + namespace + ".md";
        };
    }
}
