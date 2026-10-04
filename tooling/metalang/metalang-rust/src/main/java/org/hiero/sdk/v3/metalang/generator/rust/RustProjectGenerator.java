package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.SpecFolders;

/**
 * Generates the Cargo workspace: the workspace manifest with the versions of all dependencies, and per crate its
 * manifest (dependencies on the crates of the required folders and on the libraries its code uses) and
 * {@code lib.rs}.
 */
final class RustProjectGenerator {

    /** The libraries the generated code may use: crate name → (path prefix in the code, workspace dependency). */
    static final Map<String, String[]> LIBRARIES = new LinkedHashMap<>();

    static {
        LIBRARIES.put("chrono", new String[] {"chrono::",
                "{ version = \"0.4\", default-features = false, features = [\"std\"] }"});
        LIBRARIES.put("ethnum", new String[] {"ethnum::", "\"1\""});
        LIBRARIES.put("futures-core", new String[] {"futures_core::", "{ version = \"0.3\", default-features = false, "
                + "features = [\"std\"] }"});
        LIBRARIES.put("regex", new String[] {"regex::", "{ version = \"1\", default-features = false, "
                + "features = [\"std\", \"unicode\"] }"});
        LIBRARIES.put("rust_decimal", new String[] {"rust_decimal::",
                "{ version = \"1\", default-features = false, features = [\"std\"] }"});
        LIBRARIES.put("uuid", new String[] {"uuid::", "{ version = \"1\", default-features = false, "
                + "features = [\"std\"] }"});
    }

    private static final String HEADER = "# " + RustGenerator.MARKER + "\n";

    private RustProjectGenerator() {
    }

    static List<GeneratedFile> workspace(final RustContext context) {
        final RustGeneratorConfig config = context.config();
        final StringBuilder toml = new StringBuilder(HEADER).append("\n[workspace]\nresolver = \"3\"\nmembers = [\n");
        context.folders().forEach(f -> toml.append("    \"").append(RustNames.crateDirectory(f.name()))
                .append("\",\n"));
        toml.append("]\n\n[workspace.package]\nversion = \"").append(config.version()).append("\"\n")
                .append("edition = \"2024\"\nrust-version = \"1.85\"\nlicense = \"Apache-2.0\"\n")
                .append("repository = \"https://github.com/hiero-ledger\"\n\n[workspace.dependencies]\n");
        LIBRARIES.forEach((name, library) -> toml.append(name).append(" = ").append(library[1]).append('\n'));
        context.folders().forEach(f -> toml.append(config.crateName(f.name())).append(" = { path = \"")
                .append(RustNames.crateDirectory(f.name())).append("\", version = \"").append(config.version())
                .append("\" }\n"));
        final List<GeneratedFile> files = new ArrayList<>();
        files.add(new GeneratedFile("Cargo.toml", toml.toString()));
        files.add(new GeneratedFile(".gitignore", HEADER + "/target/\n/Cargo.lock\n"));
        return files;
    }

    static GeneratedFile manifest(final SpecFolders.Folder folder, final List<GeneratedFile> files,
                                  final RustContext context) {
        final RustGeneratorConfig config = context.config();
        final String content = files.stream().map(GeneratedFile::content).collect(Collectors.joining("\n"));
        final TreeSet<String> dependencies = new TreeSet<>();
        for (final String required : SpecFolders.required(folder.name(), context.folders())) {
            if (!required.equals(folder.name())) {
                dependencies.add(config.crateName(required));
            }
        }
        LIBRARIES.forEach((name, library) -> {
            if (content.contains(library[0])) {
                dependencies.add(name);
            }
        });
        final StringBuilder toml = new StringBuilder(HEADER).append("\n[package]\nname = \"")
                .append(config.crateName(folder.name())).append("\"\ndescription = \"")
                .append("The ").append(folder.name()).append(" crate of the Hiero SDK (generated API).\"\n")
                .append("version.workspace = true\nedition.workspace = true\nrust-version.workspace = true\n")
                .append("license.workspace = true\nrepository.workspace = true\n");
        if (!dependencies.isEmpty()) {
            toml.append("\n[dependencies]\n");
            dependencies.forEach(d -> toml.append(d).append(".workspace = true\n"));
        }
        return new GeneratedFile(RustNames.crateDirectory(folder.name()) + "/Cargo.toml", toml.toString());
    }

    static GeneratedFile library(final SpecFolders.Folder folder, final boolean support, final RustContext context) {
        final StringBuilder rs = new StringBuilder(RustGenerator.HEADER);
        rs.append("//! The `").append(context.config().crateName(folder.name())).append("` crate of the Hiero SDK: ")
                .append("the namespaces ").append(folder.namespaces().stream().map(n -> "`" + n.name() + "`")
                        .collect(Collectors.joining(", "))).append(".\n");
        // the generated code uses its own deprecated items (e.g. in the implementations of deprecated methods)
        rs.append("#![allow(deprecated)]\n\n");
        RustNamespaceGenerator.roots(folder.name(), context).forEach(m -> rs.append("pub mod ").append(m)
                .append(";\n"));
        if (support) {
            rs.append("pub mod ").append(RustNames.SUPPORT).append(";\n");
        }
        return new GeneratedFile(RustNames.crateDirectory(folder.name()) + "/src/lib.rs", rs.toString());
    }
}
