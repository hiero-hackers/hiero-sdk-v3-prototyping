package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.NamespaceDefinition;
import org.junit.jupiter.api.Test;

class SpecFoldersTest {

    private static LinkedModel model(final Map<String, String> schemas) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        return LinkedModel.of(new MetaLang().validate(documents).model());
    }

    @Test
    void shouldGroupNamespacesByFolderWithTheRequiredFolders() {
        // WHEN
        final List<SpecFolders.Folder> folders = SpecFolders.of(model(Map.of(
                "base/a.md", "namespace a\nA { @@immutable x: int32 }\n",
                "base/b.md", "namespace b\n",
                "client/c.md", "namespace c\nrequires {A} from a\nC { @@immutable a: A }\n",
                "app/d.md", "namespace d\nrequires {C} from c\nD { @@immutable c: C }\n")), "package");

        // THEN
        assertThat(folders).extracting(SpecFolders.Folder::name).containsExactly("app", "base", "client");
        assertThat(folders.get(1).namespaces()).extracting(NamespaceDefinition::name).containsExactly("a", "b");
        assertThat(folders.get(0).requires()).containsExactly("client");
        assertThat(SpecFolders.required("app", folders)).containsExactly("base", "client");
        assertThat(SpecFolders.required("base", folders)).isEmpty();
        assertThat(SpecFolders.common(Set.of("app", "base", "client"), folders)).contains("base");
        assertThat(SpecFolders.common(Set.of("app"), folders)).contains("app");
    }

    @Test
    void shouldRejectFoldersThatCannotBeUnits() {
        assertThatThrownBy(() -> SpecFolders.of(model(Map.of("a.md", "namespace a\n")), "module"))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                        "Spec file 'a.md' of namespace 'a' is not inside a folder; the folder defines the module"));
        assertThatThrownBy(() -> SpecFolders.of(model(Map.of("x/a.md", "namespace a\n", "y/a.md", "namespace a\n")),
                "module")).isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                .containsExactly("Namespace 'a' is spread over the spec folders [x, y]; it must belong to exactly "
                        + "one module"));
        assertThatThrownBy(() -> SpecFolders.of(model(Map.of(
                "x/a.md", "namespace a\nrequires {B} from b\nA { @@immutable b: B }\n",
                "y/b.md", "namespace b\nrequires {A} from a\nB { @@immutable a: A }\n")), "module"))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                        "Dependency cycle between the spec folders (modules): x -> y -> x"));
        assertThat(SpecFolders.common(Set.of("x", "y"), SpecFolders.of(model(Map.of("x/a.md", "namespace a\n",
                "y/b.md", "namespace b\n")), "module"))).isEmpty();
    }
}
