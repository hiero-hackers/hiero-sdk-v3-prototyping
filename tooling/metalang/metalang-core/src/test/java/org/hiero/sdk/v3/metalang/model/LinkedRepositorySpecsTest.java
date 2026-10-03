package org.hiero.sdk.v3.metalang.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Links the real specs of this repository.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LinkedRepositorySpecsTest {

    private static final Path SPEC_ROOT = Path.of(System.getProperty("spec.root", "../../../spec"));

    private final ValidationReport report = new MetaLang().validate(SPEC_ROOT);
    private final LinkedModel model = LinkedModel.of(report.model());

    private static void collect(final Type type, final List<Type> out) {
        out.add(type);
        switch (type) {
            case Type.BasicType basic -> basic.arguments().forEach(a -> collect(a, out));
            case Type.DeclaredType declared -> declared.arguments().forEach(a -> collect(a, out));
            case Type.WildcardType wildcard -> {
                if (wildcard.upperBound() != null) {
                    collect(wildcard.upperBound(), out);
                }
            }
            case Type.FunctionType function -> {
                collect(function.returnType(), out);
                function.parameters().forEach(p -> collect(p.type(), out));
            }
            default -> {
                // leaf
            }
        }
    }

    private static void collect(final MethodDefinition method, final List<Type> out) {
        collect(method.returnType(), out);
        method.parameters().forEach(p -> collect(p.type(), out));
        method.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> collect(p.bound(), out));
    }

    private List<Type> allTypes() {
        final List<Type> types = new ArrayList<>();
        for (final TypeDefinition definition : model.types()) {
            definition.supertypes().forEach(t -> collect(t, types));
            definition.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> collect(p.bound(), types));
            definition.fields().forEach(f -> collect(f.type(), types));
            definition.methods().forEach(m -> collect(m, types));
        }
        model.functions().forEach(f -> collect(f.method(), types));
        model.constants().forEach(c -> collect(c.type(), types));
        return types;
    }

    @Test
    void everyReferenceShouldBeResolvedAndEveryDeclaredTypeShouldExist() {
        // GIVEN the specs have no unresolved-type findings
        assertThat(report.byRule("type.unknown")).isEmpty();
        assertThat(report.byRule("generic.undeclared")).isEmpty();

        // WHEN
        final List<Type> types = allTypes();

        // THEN
        assertThat(types).hasSizeGreaterThan(1000);
        assertThat(types).noneMatch(t -> t instanceof Type.UnresolvedType);
        assertThat(types).filteredOn(t -> t instanceof Type.DeclaredType)
                .allSatisfy(t -> assertThat(model.type(((Type.DeclaredType) t).name())).isPresent());
        assertThat(model.types()).hasSize(report.model().files().stream().mapToInt(f -> f.types().size()).sum());
    }

    @Test
    void transactionSubtypesShouldSeeTheirSelfTypeInInheritedMethods() {
        // WHEN
        final TypeDefinition accountCreate = model.type("consensusnode.transactions.accounts",
                "AccountCreateTransaction").orElseThrow();

        // THEN $$Receipt and $$Self of Transaction are substituted
        assertThat(accountCreate.methods("pack")).singleElement().satisfies(m -> assertThat(m.returnType().text())
                .isEqualTo("consensusnode.transactions.PackedTransaction<"
                        + "consensusnode.transactions.accounts.AccountCreateReceipt, "
                        + "consensusnode.transactions.accounts.AccountCreateTransaction>"));
        assertThat(accountCreate.field("memo").orElseThrow().declaringType())
                .isEqualTo(new QualifiedName("consensusnode.transactions", "Transaction"));
    }

    @Test
    void hbarShouldSeeItsConcreteTypesInInheritedNativeTokenMembers() {
        final TypeDefinition hbar = model.type("hedera", "Hbar").orElseThrow();
        assertThat(hbar.methods("to")).singleElement().satisfies(m -> {
            assertThat(m.returnType().text()).isEqualTo("hedera.Hbar");
            assertThat(m.signature()).isEqualTo("to(hedera.HbarUnit)");
        });
        assertThat(hbar.field("unit").orElseThrow().type().text()).isEqualTo("hedera.HbarUnit");
    }

    @Test
    void linkingShouldBeDeterministic() {
        final LinkedModel second = LinkedModel.of(new MetaLang().validate(SPEC_ROOT).model());
        assertThat(second.types()).isEqualTo(model.types());
        assertThat(second.functions()).isEqualTo(model.functions());
        assertThat(second.constants()).isEqualTo(model.constants());
    }
}
