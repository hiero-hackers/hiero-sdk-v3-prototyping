package org.hiero.sdk.v3.codegen.mapping;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.codegen.config.CodegenConfiguration;
import org.hiero.sdk.v3.codegen.manifest.DeferredEnforcement;
import org.hiero.sdk.v3.codegen.model.MappingDecision;
import org.hiero.sdk.v3.codegen.model.ResolvedSchema;
import org.hiero.sdk.v3.codegen.model.SchemaElements;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class BaseApiMapper {

    public ApiGeneration map(ResolvedSchema schema, CodegenConfiguration configuration) {
        var sources = new ArrayList<JavaSourceFile>();
        sources.add(moduleInfo(configuration));
        for (var namespace : schema.documents().stream().map(document -> document.namespace()).sorted().toList()) {
            sources.add(MappingSources.packageInfo(namespace));
        }
        sources.addAll(new CommonMapping().sourceFiles());
        sources.addAll(new ProtocolMapping().sourceFiles());
        sources.addAll(new NativeTokenMapping().sourceFiles());
        sources.addAll(new LedgerMapping().sourceFiles());
        sources.addAll(new NetworkProfileMapping().sourceFiles());
        var keyMapping = new KeyMapping();
        sources.addAll(keyMapping.sourceFiles());
        sources.addAll(new AuthorityMapping().sourceFiles());

        var mappings = SchemaElements.identities(schema).stream().sorted()
                .map(identity -> MappingDecision.direct(
                        identity, javaElement(identity, configuration), rationale(identity), "FR-003"))
                .toList();
        var deferred = new ArrayList<DeferredEnforcement>();
        deferred.addAll(keyMapping.deferredEnforcement());
        deferred.add(new DeferredEnforcement("common#Page.data", "DE-002", "interface collection ownership", "provider TCK"));
        deferred.add(new DeferredEnforcement("common#Page.next():Page<$$T>", "DE-004", "async execution policy", "provider async TCK"));
        return new ApiGeneration(sources, mappings, deferred);
    }

    private static String rationale(String identity) {
        if (!identity.contains("@@throws")) {
            return "BASE_API_MAPPING";
        }
        if (identity.startsWith("common#")) {
            return "mirror-node-error";
        }
        if (identity.startsWith("ledger.config#")) {
            return "not-found-error";
        }
        return "illegal-format";
    }

    private static JavaSourceFile moduleInfo(CodegenConfiguration configuration) {
        var exports = List.of("authority", "common", "grpc", "hedera", "keys", "ledger", "ledger.config",
                "nativetoken", "proto", "solo", "token");
        var body = new StringBuilder("/** Generated Hiero SDK V3 base contracts. */\nmodule ")
                .append(configuration.moduleName()).append(" {\n    requires static org.jspecify;\n");
        exports.forEach(value -> body.append("    exports ").append(configuration.packageRoot()).append('.').append(value).append(";\n"));
        body.append("}\n");
        return new JavaSourceFile(Path.of("module-info.java"), "codegen/java-base.yml", "module", "", List.of(), body.toString());
    }

    private static String javaElement(String identity, CodegenConfiguration configuration) {
        var separator = identity.indexOf('#');
        var namespace = identity.substring(0, separator).toLowerCase(java.util.Locale.ROOT);
        var element = identity.substring(separator + 1);
        var declarationEnd = element.length();
        for (var marker : List.of(".", "@@", "(")) {
            var index = element.indexOf(marker);
            if (index >= 0) declarationEnd = Math.min(declarationEnd, index);
        }
        var declaration = element.substring(0, declarationEnd).replace("namespace", "Operations");
        return configuration.packageRoot() + "." + namespace + "." + declaration;
    }
}
