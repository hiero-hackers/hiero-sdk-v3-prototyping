package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.junit.jupiter.api.Test;

class ValidationReportTest {

    @Test
    void shouldCountAndGroupTheDiagnostics() {
        // WHEN a schema with an error, a warning and an info
        final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown("""
                namespace a
                A { @@immutable x: Unknown
                    y: int32 }
                type B { @@immutable z: int32 }
                """)));

        // THEN
        assertThat(report.hasErrors()).isTrue();
        assertThat(report.specCount()).isEqualTo(1);
        assertThat(report.count(Severity.ERROR)).isEqualTo(1);
        assertThat(report.countByRule()).containsEntry("type.unknown", 1L).containsEntry("syntax.type-keyword", 1L)
                .containsEntry("field.mutable", 1L);
        assertThat(report.byRule("type.unknown")).hasSize(1);
        assertThat(report.severities()).containsEntry("type.unknown", Severity.ERROR)
                .containsEntry("field.mutable", Severity.INFO);
        assertThat(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown("namespace a\n"))).hasErrors())
                .isFalse();
    }
}
