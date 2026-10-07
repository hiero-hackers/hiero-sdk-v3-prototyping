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

    /** The protobuf dependencies: crate name -> version, added to the workspace when a crate needs them. */
    static final Map<String, String> PROTOBUF_LIBRARIES = new LinkedHashMap<>();

    static {
        PROTOBUF_LIBRARIES.put("prost", "\"0.14\"");
        PROTOBUF_LIBRARIES.put("prost-build", "\"0.14\"");
        // a protoc binary for the build script, so that building needs no protobuf installation
        PROTOBUF_LIBRARIES.put("protoc-bin-vendored", "\"3\"");
    }

    /** The private module of a crate that holds the compiled protobuf messages. */
    static final String PROTOBUF_MODULE = "proto";

    /** The file prost-build writes the module tree of all protobuf packages into. */
    static final String PROTOBUF_INCLUDE = "_protobuf.rs";

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
        if (!config.protobuf().isEmpty()) {
            PROTOBUF_LIBRARIES.forEach((name, version) -> toml.append(name).append(" = ").append(version)
                    .append('\n'));
        }
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
        final boolean protobuf = config.protobuf().contains(folder.name());
        if (protobuf) {
            dependencies.add("prost");
        }
        if (!dependencies.isEmpty()) {
            toml.append("\n[dependencies]\n");
            dependencies.forEach(d -> toml.append(d).append(".workspace = true\n"));
        }
        if (protobuf) {
            toml.append("\n[build-dependencies]\nprost-build.workspace = true\n")
                    .append("protoc-bin-vendored.workspace = true\n");
        }
        return new GeneratedFile(RustNames.crateDirectory(folder.name()) + "/Cargo.toml", toml.toString());
    }

    /**
     * The build script that compiles the vendored protobuf definitions into {@code OUT_DIR}.
     *
     * @param folder  the spec folder of the crate
     * @param context the generator context
     * @return the {@code build.rs} of the crate
     */
    static GeneratedFile buildScript(final SpecFolders.Folder folder, final RustContext context) {
        final String rs = RustGenerator.HEADER
                + "//! Compiles the vendored protobuf definitions into `OUT_DIR`.\n"
                + "//!\n"
                + "//! The result is included by `src/proto.rs` through a private module, so it never leaves the\n"
                + "//! crate. `protoc` comes from `protoc-bin-vendored`: building needs no protobuf installation.\n"
                + "\n"
                + "use std::io::Result;\n"
                + "use std::path::{Path, PathBuf};\n"
                + "\n"
                + "fn main() -> Result<()> {\n"
                + "    let root = Path::new(env!(\"CARGO_MANIFEST_DIR\"))\n"
                + "        .join(\"" + context.config().protobufRoot() + "\")\n"
                + "        .canonicalize()?;\n"
                + "\n"
                + "    let mut files: Vec<PathBuf> = Vec::new();\n"
                + "    collect(&root, &mut files)?;\n"
                + "    files.sort();\n"
                + "    for file in &files {\n"
                + "        println!(\"cargo:rerun-if-changed={}\", file.display());\n"
                + "    }\n"
                + "\n"
                + "    // SAFETY: a build script is single-threaded; this runs before prost-build reads PROTOC.\n"
                + "    unsafe {\n"
                + "        std::env::set_var(\n"
                + "            \"PROTOC\",\n"
                + "            protoc_bin_vendored::protoc_bin_path().expect(\"a vendored protoc\"),\n"
                + "        );\n"
                + "    }\n"
                + "\n"
                + "    prost_build::Config::new()\n"
                + "        .include_file(\"" + PROTOBUF_INCLUDE + "\")\n"
                + "        .compile_protos(&files, &[root])\n"
                + "}\n"
                + "\n"
                + "fn collect(directory: &Path, files: &mut Vec<PathBuf>) -> Result<()> {\n"
                + "    for entry in std::fs::read_dir(directory)? {\n"
                + "        let path = entry?.path();\n"
                + "        if path.is_dir() {\n"
                + "            collect(&path, files)?;\n"
                + "        } else if path.extension().is_some_and(|extension| extension == \"proto\") {\n"
                + "            files.push(path);\n"
                + "        }\n"
                + "    }\n"
                + "    Ok(())\n"
                + "}\n";
        return new GeneratedFile(RustNames.crateDirectory(folder.name()) + "/build.rs", rs);
    }

    /**
     * The module that includes the compiled protobuf messages.
     *
     * @param folder the spec folder of the crate
     * @return the {@code src/proto.rs} of the crate
     */
    static GeneratedFile protoModule(final SpecFolders.Folder folder) {
        final String rs = RustGenerator.HEADER
                + "//! The protobuf messages of the Hiero node types, compiled by `build.rs`.\n"
                + "//!\n"
                + "//! **Not public API.** `lib.rs` declares this module without `pub`, so it is reachable inside\n"
                + "//! the crate only: it is absent from the crate documentation and from the semver surface, and a\n"
                + "//! change of the wire format is no breaking change of the SDK.\n"
                + "#![allow(dead_code, clippy::all)]\n"
                + "\n"
                + "include!(concat!(env!(\"OUT_DIR\"), \"/" + PROTOBUF_INCLUDE + "\"));\n";
        return new GeneratedFile(RustNames.crateDirectory(folder.name()) + "/src/" + PROTOBUF_MODULE + ".rs", rs);
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
        if (context.config().protobuf().contains(folder.name())) {
            // no `pub`: the wire format is an implementation detail, see src/proto.rs
            rs.append("\nmod ").append(PROTOBUF_MODULE).append(";\n");
        }
        return new GeneratedFile(RustNames.crateDirectory(folder.name()) + "/src/lib.rs", rs.toString());
    }
}
