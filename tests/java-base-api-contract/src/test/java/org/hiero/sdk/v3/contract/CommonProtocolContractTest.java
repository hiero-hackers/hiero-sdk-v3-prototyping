package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.concurrent.CompletionStage;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.common.Page;
import org.hiero.sdk.v3.grpc.MethodDescriptor;
import org.junit.jupiter.api.Test;

class CommonProtocolContractTest {

    @Test
    void pageUsesCompletionStageAndDeclaresMirrorNodeFailures() throws Exception {
        assertEquals(CompletionStage.class, Page.class.getMethod("next").getReturnType());
        assertEquals(CompletionStage.class, Page.class.getMethod("first").getReturnType());
        var source = Files.readString(ContractTestSupport.repositoryRoot()
                .resolve("java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java"));
        assertTrue(source.contains("mirror-node failures complete exceptionally"));
    }

    @Test
    void protocolDescriptorIsImmutableAndProtoHasNoPublicType() throws Exception {
        assertTrue(MethodDescriptor.class.isRecord());
        var descriptor = new MethodDescriptor("service", "method");
        assertEquals("service", descriptor.serviceName());
        assertEquals("method", descriptor.methodName());
        assertFalse(java.lang.reflect.Modifier.isPublic(
                Class.forName("org.hiero.sdk.v3.proto.ProtoPackageAnchor").getModifiers()));
    }

    @Test
    void consumersCompileWithoutTransportOrCodecDependencies() throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var root = ContractTestSupport.repositoryRoot();
        var output = root.resolve("tests/java-base-api-contract/target/protocol-fixtures");
        Files.createDirectories(output);
        for (var name : new String[] {"PageConsumer.java", "ProtocolConsumer.java"}) {
            var source = root.resolve("tests/java-base-api-contract/src/test/resources/fixtures/positive/" + name);
            assertEquals(0, compiler.run(null, null, null, "--release", "21", "-proc:none", "-d", output.toString(), source.toString()));
        }
        var apiPom = Files.readString(root.resolve("java/hiero-sdk-base-api/pom.xml"));
        assertFalse(apiPom.contains("io.grpc"));
        assertFalse(apiPom.contains("protobuf"));
        assertFalse(apiPom.contains("httpclient"));
    }
}
