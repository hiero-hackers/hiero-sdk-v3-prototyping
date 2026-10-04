package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.SpecFolders;

/**
 * Generates the npm workspace: a root {@code package.json} with the build ({@code tsc --build}) and the tests
 * ({@code node --test}), a {@code tsconfig.base.json} with the compiler options (strict, ES2022 modules), and per spec
 * folder a package with a {@code package.json} (namespace subpaths as {@code exports}, the required packages as
 * dependencies) and a {@code tsconfig.json} (project references to the required packages). The tool versions are
 * pinned.
 */
final class TsProjectGenerator {

    /** The version of TypeScript (the last major version with the JavaScript compiler API). */
    static final String TYPESCRIPT_VERSION = "6.0.3";

    /** The version of the Node.js type definitions (Node.js 22, the active LTS). */
    static final String NODE_TYPES_VERSION = "22.20.5";

    private TsProjectGenerator() {
    }

    static List<GeneratedFile> root(final List<SpecFolders.Folder> folders, final TsGeneratorConfig config,
                                    final boolean support) {
        // the hand-written support package is linked into the workspace and built first
        final List<String> workspaces = new java.util.ArrayList<>();
        if (support) {
            workspaces.add(TsNames.supportDirectory(config, ""));
        }
        folders.forEach(f -> workspaces.add(TsNames.packageDirectory(f.name())));
        final List<String> references = new java.util.ArrayList<>();
        if (support) {
            references.add(TsNames.supportDirectory(config, ""));
        }
        folders.forEach(f -> references.add("./" + TsNames.packageDirectory(f.name())));
        // the marker in the first line: JSON has no comments
        final String packageJson = "{ \"//\": \"" + TsGenerator.MARKER + "\",\n"
                + "  \"name\": \"hiero-sdk\",\n"
                + "  \"version\": \"" + config.version() + "\",\n"
                + "  \"private\": true,\n"
                + "  \"description\": \"The TypeScript API of the Hiero SDK.\",\n"
                + "  \"type\": \"module\",\n"
                + "  \"workspaces\": [\n"
                + workspaces.stream().map(w -> "    \"" + w + "\"").collect(Collectors.joining(",\n")) + "\n"
                + "  ],\n"
                + "  \"scripts\": {\n"
                + "    \"build\": \"tsc --build\",\n"
                + "    \"test\": \"tsc --build && node --test \\\"packages/*/dist/**/*.test.js\\\"\"\n"
                + "  },\n"
                + "  \"devDependencies\": {\n"
                + "    \"@types/node\": \"" + NODE_TYPES_VERSION + "\",\n"
                + "    \"typescript\": \"" + TYPESCRIPT_VERSION + "\"\n"
                + "  }\n"
                + "}\n";
        final String base = "// " + TsGenerator.MARKER + "\n"
                + "{\n"
                + "  \"compilerOptions\": {\n"
                + "    \"target\": \"ES2022\",\n"
                + "    \"lib\": [\"ES2023\"],\n"
                + "    \"module\": \"NodeNext\",\n"
                + "    \"moduleResolution\": \"NodeNext\",\n"
                + "    \"types\": [\"node\"],\n"
                + "    \"strict\": true,\n"
                + "    \"exactOptionalPropertyTypes\": true,\n"
                + "    \"noUncheckedIndexedAccess\": true,\n"
                + "    \"noImplicitReturns\": true,\n"
                + "    \"noUnusedLocals\": true,\n"
                + "    \"noFallthroughCasesInSwitch\": true,\n"
                + "    \"verbatimModuleSyntax\": true,\n"
                + "    \"isolatedModules\": true,\n"
                + "    \"forceConsistentCasingInFileNames\": true,\n"
                + "    \"declaration\": true,\n"
                + "    \"composite\": true\n"
                + "  }\n"
                + "}\n";
        final String tsconfig = "// " + TsGenerator.MARKER + "\n"
                + "{\n"
                + "  \"files\": [],\n"
                + "  \"references\": [\n"
                + references.stream().map(r -> "    { \"path\": \"" + r + "\" }").collect(Collectors.joining(",\n"))
                + "\n"
                + "  ]\n"
                + "}\n";
        final String gitignore = "# " + TsGenerator.MARKER + "\n"
                + "node_modules/\n"
                + "dist/\n"
                + "*.tsbuildinfo\n"
                + "package-lock.json\n";
        return List.of(new GeneratedFile("package.json", packageJson),
                new GeneratedFile("tsconfig.base.json", base),
                new GeneratedFile("tsconfig.json", tsconfig),
                new GeneratedFile(".gitignore", gitignore));
    }

    static List<GeneratedFile> folder(final SpecFolders.Folder folder, final List<String> subpaths,
                                      final TsGeneratorConfig config, final boolean support) {
        final String directory = TsNames.packageDirectory(folder.name());
        final String exports = subpaths.stream().sorted().map(s -> "    \"./" + s + "\": {\n"
                        + "      \"types\": \"./dist/" + s + "/index.d.ts\",\n"
                        + "      \"default\": \"./dist/" + s + "/index.js\"\n"
                        + "    }")
                .collect(Collectors.joining(",\n"));
        final List<String> required = new java.util.ArrayList<>();
        if (support) {
            required.add(TsNames.supportPackage(config));
        }
        folder.requires().forEach(r -> required.add(TsNames.packageName(config, r)));
        final String dependencies = required.stream()
                .map(r -> "    \"" + r + "\": \"" + config.version() + "\"")
                .collect(Collectors.joining(",\n"));
        final List<String> references = new java.util.ArrayList<>();
        if (support) {
            references.add(TsNames.supportDirectory(config, "../../"));
        }
        folder.requires().forEach(r -> references.add("../" + r));
        final String packageJson = "{ \"//\": \"" + TsGenerator.MARKER + "\",\n"
                + "  \"name\": \"" + TsNames.packageName(config, folder.name()) + "\",\n"
                + "  \"version\": \"" + config.version() + "\",\n"
                + "  \"description\": \"Package " + folder.name() + " of the Hiero SDK.\",\n"
                + "  \"type\": \"module\",\n"
                + "  \"exports\": {\n" + exports + "\n  },\n"
                + "  \"files\": [\"dist\"]" + (dependencies.isEmpty() ? "\n" : ",\n  \"dependencies\": {\n"
                + dependencies + "\n  }\n")
                + "}\n";
        final String tsconfig = "// " + TsGenerator.MARKER + "\n"
                + "{\n"
                + "  \"extends\": \"../../tsconfig.base.json\",\n"
                + "  \"compilerOptions\": {\n"
                + "    \"rootDir\": \"src\",\n"
                + "    \"outDir\": \"dist\"\n"
                + "  },\n"
                + "  \"include\": [\"src\"],\n"
                + "  \"references\": [" + (references.isEmpty() ? "" : "\n" + references.stream()
                .map(r -> "    { \"path\": \"" + r + "\" }").collect(Collectors.joining(",\n")) + "\n  ")
                + "]\n"
                + "}\n";
        return List.of(new GeneratedFile(directory + "/package.json", packageJson),
                new GeneratedFile(directory + "/tsconfig.json", tsconfig));
    }
}
