package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.junit.jupiter.api.Test;

class BehaviorRulesTest {

    private static final SourceLocation LOCATION = new SourceLocation("a.md", 1, 1);

    private static Annotation annotation(final String name, final String... arguments) {
        return new Annotation(name, List.of(arguments).stream()
                .map(a -> (Literal) new Literal.StringLiteral(a, LOCATION)).toList(), true, LOCATION);
    }

    @Test
    void shouldAcceptAnIdAndAnIdQualifiedByANamespace() {
        assertThat(BehaviorRules.isReference("node-choice")).isTrue();
        assertThat(BehaviorRules.isReference("rule1")).isTrue();
        assertThat(BehaviorRules.isReference("consensusnode.client/node-choice")).isTrue();
        assertThat(BehaviorRules.isReference("nativeToken/conversion")).isTrue();
        assertThat(BehaviorRules.isReference("Node-Choice")).isFalse();
        assertThat(BehaviorRules.isReference("node_choice")).isFalse();
        assertThat(BehaviorRules.isReference("rule:node-choice")).isFalse();
        assertThat(BehaviorRules.isReference("consensusnode.client/")).isFalse();
        assertThat(BehaviorRules.isReference("")).isFalse();
    }

    @Test
    void shouldQualifyAnIdWithTheNamespaceOfTheTest() {
        // GIVEN
        final List<Annotation> annotations = List.of(annotation("testId", "a-test"),
                annotation("rule", "node-choice", "ledger/checksum", "node-choice"), annotation("rule", "Invalid"));

        // WHEN
        final List<String> rules = BehaviorRules.of("consensusnode.client", annotations);

        // THEN the order of the annotations, without duplicates and without the invalid reference
        assertThat(rules).containsExactly("consensusnode.client/node-choice", "ledger/checksum");
        assertThat(BehaviorRules.tag(rules.getFirst())).isEqualTo("rule:consensusnode.client/node-choice");
    }
}
