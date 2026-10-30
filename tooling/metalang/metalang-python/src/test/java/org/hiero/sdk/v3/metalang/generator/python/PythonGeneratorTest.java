package org.hiero.sdk.v3.metalang.generator.python;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PythonGeneratorTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec"));

    /** The Python interpreter for the import and type checks; {@code -Dpython.executable=...} selects another one. */
    private static final String PYTHON = System.getProperty("python.executable", "python3");

    @TempDir
    Path temp;

    private static LinkedModel model(final Map<String, String> schemas, final boolean strict) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, schema.startsWith("# ") ? schema
                : TestSpecs.markdown(schema)));
        final ValidationReport report = new MetaLang().validate(documents);
        if (strict) {
            assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        }
        return LinkedModel.of(report.model());
    }

    private static LinkedModel model(final String schema) {
        return model(Map.of("f/a.md", schema), true);
    }

    static String file(final List<GeneratedFile> files, final String path) {
        return files.stream().filter(f -> f.path().equals(path)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + path + " in " + files.stream()
                        .map(GeneratedFile::path).toList())).content();
    }

    private static String module(final List<GeneratedFile> files, final String suffix) {
        return files.stream().filter(f -> f.path().endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + suffix)).content();
    }

    @Nested
    class Layout {

        @Test
        void shouldCreateOneDistributionWithOnePackagePerNamespace() {
            // GIVEN
            final LinkedModel model = model(Map.of(
                    "base/b.md", "namespace ledger\nPoint { @@immutable x: int32 }\n",
                    "base/c.md", "namespace ledger.config\nSetting { @@immutable name: string }\n",
                    "client/d.md", "namespace nativeToken.units\nUnit { @@immutable symbol: string }\n"), true);

            // WHEN
            final List<GeneratedFile> files = new PythonGenerator().generate(model);

            // THEN
            assertThat(files).extracting(GeneratedFile::path).containsExactly(
                    ".gitignore", "pyproject.toml",
                    "src/hiero_sdk/__init__.py",
                    "src/hiero_sdk/ledger/__init__.py", "src/hiero_sdk/ledger/_point.py",
                    "src/hiero_sdk/ledger/config/__init__.py", "src/hiero_sdk/ledger/config/_setting.py",
                    "src/hiero_sdk/native_token/__init__.py",
                    "src/hiero_sdk/native_token/units/__init__.py", "src/hiero_sdk/native_token/units/_unit.py",
                    "src/hiero_sdk/py.typed");
            assertThat(files).allMatch(f -> f.content().startsWith(PythonGenerator.HEADER));
            assertThat(file(files, "src/hiero_sdk/ledger/__init__.py")).endsWith("""
                    from hiero_sdk.ledger._point import Point

                    __all__ = [
                        "Point",
                    ]
                    """);
            assertThat(file(files, "src/hiero_sdk/native_token/__init__.py")).isEqualTo(PythonGenerator.HEADER);
            assertThat(file(files, "src/hiero_sdk/__init__.py")).contains("\"\"\"The Python API of the Hiero SDK.");
            assertThat(file(files, "pyproject.toml")).contains("name = \"hiero-sdk\"")
                    .contains("requires-python = \">=3.12\"").contains("packages = [\"src/hiero_sdk\"]")
                    .contains("strict = true");
        }

        @Test
        void shouldUseTheConfiguration() {
            final PythonGenerator generator = new PythonGenerator(
                    new PythonGeneratorConfig("hiero-acme", "acme", "2.0.0"));
            final List<GeneratedFile> files = generator.generate(model("namespace a\nX { @@immutable n: int32 }\n"));
            assertThat(file(files, "pyproject.toml")).contains("name = \"hiero-acme\"").contains("version = \"2.0.0\"");
            assertThat(files).extracting(GeneratedFile::path).contains("src/acme/a/_x.py");
            assertThat(generator.deferred()).isEmpty();
        }

        @Test
        void theDescriptionShouldBecomeThePackageDocstring() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model(Map.of("f/a.md", """
                    # A

                    ## Description

                    The things of a.

                    ## API Schema

                    ```
                    namespace a
                    X { @@immutable n: int32 }
                    ```

                    ## Questions & Comments
                    """), true));
            assertThat(file(files, "src/hiero_sdk/a/__init__.py")).contains("\"\"\"The things of a.\"\"\"");
        }
    }

    @Nested
    class Types {

        @Test
        void aConcreteTypeShouldBeAFrozenDataclass() {
            final String py = module(new PythonGenerator().generate(model("""
                    namespace a
                    enum Kind { ONE, TWO }
                    // An account.
                    Account {
                        // the shard
                        @@immutable shard: int64
                        @@immutable @@nullable alias: bytes
                        @@immutable @@default([]) tags: list<string>
                        @@immutable @@default([]) codes: set<int32>
                        @@immutable @@default([]) extra: map<string, int32>
                        @@immutable @@default([]) payload: bytes
                        @@immutable @@default(Kind.TWO) kind: Kind
                        @@immutable @@default(true) active: bool
                        @@immutable @@default("x") label: string
                        @@immutable @@default(1.5) rate: decimal
                        @@immutable from: string
                        @@immutable created: zonedDateTime
                        @@immutable period: duration
                        @@immutable id: uuid
                        @@immutable callback: function<int32 apply(text: string)>
                        @@immutable anything: ANY
                    }
                    """)), "/_account.py");
            assertThat(py)
                    .contains("@dataclass(frozen=True, slots=True, kw_only=True)\nclass Account:\n")
                    .contains("    \"\"\"An account.\"\"\"\n")
                    .contains("    shard: int\n    \"\"\"the shard\"\"\"\n")
                    .contains("    alias: bytes | None = None\n")
                    .contains("    tags: tuple[str, ...] = ()\n")
                    .contains("    codes: frozenset[int] = frozenset()\n")
                    .contains("    extra: Mapping[str, int] = field(default_factory=dict)\n")
                    .contains("    payload: bytes = b\"\"\n")
                    .contains("    kind: Kind = Kind.TWO\n")
                    .contains("    active: bool = True\n")
                    .contains("    label: str = \"x\"\n")
                    .contains("    rate: _decimal.Decimal = _decimal.Decimal(\"1.5\")\n")
                    .contains("    from_: str\n")
                    .contains("    created: _dt.datetime\n")
                    .contains("    period: _dt.timedelta\n")
                    .contains("    id: _uuid.UUID\n")
                    .contains("    callback: Callable[[str], int]\n")
                    .contains("    anything: object\n")
                    .contains("from dataclasses import dataclass, field\n")
                    .contains("from hiero_sdk.a._kind import Kind\n");
        }

        @Test
        void aMutableAttributeShouldMakeTheInheritanceTreeNonFrozen() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    Base { @@immutable @@nullable id: string }
                    Child extends Base {
                        @@nullable note: string
                        @@immutable @@override id: string
                    }
                    Alone { @@immutable n: int32 }
                    """));
            assertThat(module(files, "/_base.py")).contains("@dataclass(slots=True, kw_only=True)\nclass Base:");
            assertThat(module(files, "/_child.py")).contains("class Child(Base):\n")
                    .contains("from hiero_sdk.a._base import Base\n")
                    .contains("    note: str | None = None\n").contains("    id: str\n");
            assertThat(module(files, "/_alone.py")).contains("@dataclass(frozen=True, slots=True, kw_only=True)");
        }

        @Test
        void anAbstractionShouldBeAProtocol() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    abstraction Named {
                        // the name
                        @@immutable name: string
                    }
                    abstraction Tagged extends Named {
                        @@nullable tag: string
                    }
                    abstraction Empty {}
                    Thing extends Tagged {}
                    """));
            assertThat(module(files, "/_named.py")).contains("class Named(Protocol):\n")
                    .contains("    @property\n    def name(self) -> str:\n        \"\"\"the name\"\"\"\n        ...\n");
            assertThat(module(files, "/_tagged.py")).contains("class Tagged(Named, Protocol):\n")
                    .contains("    tag: str | None\n").contains("from hiero_sdk.a._named import Named\n");
            assertThat(module(files, "/_empty.py")).contains("class Empty(Protocol):\n    pass\n");
            // structural: the concrete type does not name the protocol
            assertThat(module(files, "/_thing.py")).contains("class Thing:\n")
                    .contains("\"\"\"Implements ``Tagged``.\"\"\"");
        }

        @Test
        void aSealedAbstractionShouldBeAUnionAlias() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A shape.
                    @@sealed(Circle, Square)
                    abstraction Shape {}
                    Circle extends Shape { @@immutable radius: double }
                    Square extends Shape { @@immutable side: double }
                    """));
            assertThat(module(files, "/_shape.py"))
                    .contains("import Square\n\ntype Shape = Circle | Square\n\"\"\"A shape.\"\"\"\n")
                    .contains("from hiero_sdk.a._circle import Circle\n");
        }

        @Test
        void aGenericSealedAbstractionShouldBeAUnionAliasWithoutParameters() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A receipt.
                    Receipt { @@immutable id: string }
                    // A box.
                    @@sealed(SmallBox, LargeBox)
                    abstraction Box<$$Self extends Box<$$Self>> { @@immutable label: string }
                    SmallBox extends Box<SmallBox> { @@immutable width: int32 }
                    LargeBox extends Box<LargeBox> { @@immutable height: int32 }
                    """));
            // the subtypes bind every parameter, so the alias needs none of its own
            assertThat(module(files, "/_box.py")).contains("type Box = SmallBox | LargeBox\n");
        }

        @Test
        void genericsShouldUsePep695() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    abstraction Receipt { @@immutable status: string }
                    abstraction Transaction<$$R extends Receipt, $$Self extends Transaction<$$R, $$Self>> {
                        @@nullable memo: string
                        @@nullable last: $$R
                    }
                    Page<$$T> {
                        @@immutable @@default([]) items: list<$$T>
                        @@immutable owner: Transaction<ANY, ANY>
                    }
                    Response<$$Receipt extends Receipt, $$Q extends Page<$$Receipt>> {
                        @@immutable receipt: $$Receipt
                        @@immutable raw: Page<ANY>
                    }
                    """));
            assertThat(module(files, "/_transaction.py")).contains("class Transaction[R: Receipt](Protocol):\n")
                    .contains("    last: R | None\n");
            assertThat(module(files, "/_page.py")).contains("class Page[T]:\n")
                    .contains("    items: tuple[T, ...] = ()\n").contains("    owner: Transaction[Any]\n");
            assertThat(module(files, "/_response.py"))
                    .contains("class Response[ReceiptT: Receipt, Q: Page[Any]]:\n")
                    .contains("The bound of the type parameter ``Q`` (a.Page<$$Receipt>) is widened")
                    .contains("    receipt: ReceiptT\n").contains("    raw: Page[Any]\n");
        }

        @Test
        void enumerationsShouldUseBothShapes() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // The container.
                    enum Container {
                        // the first one
                        PKCS8
                        SPKI
                    }
                    enum Format(container: Container, code: int32, label: string, fast: bool) {
                        PKCS8_WITH_DER(Container.PKCS8, 1, "der", true)
                        SPKI_WITH_DER(SPKI, 2, "spki", false)
                    }
                    """));
            assertThat(module(files, "/_container.py")).contains("class Container(Enum):\n")
                    .contains("    \"\"\"The container.\"\"\"\n")
                    .contains("    PKCS8 = \"PKCS8\"\n    \"\"\"the first one\"\"\"\n    SPKI = \"SPKI\"\n");
            assertThat(module(files, "/_format.py"))
                    .contains("    PKCS8_WITH_DER = (\"PKCS8_WITH_DER\", Container.PKCS8, 1, \"der\", True)\n")
                    .contains("    SPKI_WITH_DER = (\"SPKI_WITH_DER\", Container.SPKI, 2, \"spki\", False)\n")
                    .contains("    def __init__(self, value_name: str, container: Container, code: int, label: str, "
                            + "fast: bool) -> None:\n")
                    .contains("        self._code = code\n")
                    .contains("    @property\n    def code(self) -> int:\n        \"\"\"The code of the value.\"\"\"\n"
                            + "        return self._code\n")
                    .contains("from hiero_sdk.a._container import Container\n");
        }

        @Test
        void annotationsShouldBecomeNotes() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    @@threadSafe
                    @@deprecated
                    Old { @@immutable n: int32 }
                    """));
            assertThat(module(files, "/_old.py")).contains("Instances are safe to use from several threads.")
                    .contains("Deprecated: this declaration should no longer be used.");
        }
    }

    @Nested
    class Methods {

        @Test
        void aMethodShouldRaiseNotImplementedAndAProtocolShouldDeclareIt() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A repository.
                    abstraction Repository {
                        // Finds one.
                        string find(key: string)
                    }
                    // An account.
                    Account {
                        @@immutable id: string
                        // Renames it.
                        void rename(name: string)
                    }
                    """));
            assertThat(module(files, "/_repository.py"))
                    .contains("    def find(self, key: str) -> str:\n        \"\"\"Finds one.\"\"\"\n        ...\n");
            assertThat(module(files, "/_account.py"))
                    .contains("    def rename(self, name: str) -> None:\n        \"\"\"Renames it.\"\"\"\n"
                            + "        raise NotImplementedError(\"Not implemented yet: Account.rename\")\n");
        }

        @Test
        void asyncShouldBeACoroutineAndStreamingAnAsyncIterator() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A client.
                    Client {
                        @@immutable id: string
                        // Loads it.
                        @@async string load(key: string)
                        // Follows it.
                        @@streaming string follow()
                    }
                    """));
            assertThat(module(files, "/_client.py"))
                    .contains("    async def load(self, key: str) -> str:")
                    .contains("    def follow(self) -> AsyncIterator[str]:");
        }

        @Test
        void overloadsShouldStayOneNameWithTypingOverload() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // Keys.
                    Keys {
                        @@immutable id: string
                        // Creates one.
                        string create(value: string)
                        // Creates one from bytes.
                        bytes create(raw: bytes)
                    }
                    """));
            // the implementation accepts every overload and returns what every overload can produce
            assertThat(module(files, "/_keys.py"))
                    .contains("    @overload\n    def create(self, value: str) -> str: ...\n")
                    .contains("    @overload\n    def create(self, raw: bytes) -> bytes: ...\n")
                    .contains("    def create(self, *args: Any, **kwargs: Any) -> str | bytes:");
        }

        @Test
        void staticOverloadsShouldHaveNoSelf() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // Keys.
                    Keys {
                        @@immutable id: string
                        // Creates the default.
                        @@static Keys create()
                        // Creates one with the id.
                        @@static Keys create(id: string)
                    }
                    """));
            // a static method has no instance, neither in the overloads nor in the implementation
            assertThat(module(files, "/_keys.py"))
                    .contains("    @overload\n    @staticmethod\n    def create() -> Keys: ...\n")
                    .contains("    @staticmethod\n    def create(*args: Any, **kwargs: Any) -> Keys:");
        }

        @Test
        void aMemberNamedLikeABuiltinShouldQualifyTheBuiltinInAnnotations() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A key.
                    Key {
                        @@immutable bytes: bytes
                        // Encodes it.
                        bytes encode()
                    }
                    """));
            // the attribute shadows the builtin inside the class body, so the annotation names the module
            assertThat(module(files, "/_key.py")).contains("    def encode(self) -> _builtins.bytes:")
                    .contains("import builtins as _builtins");
        }

        @Test
        void anAttributeNamedLikeABuiltinShouldQualifyTheBuiltinInTheOtherAttributes() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A transaction.
                    @@finalType
                    Transaction {
                        @@immutable @@nullable bytes: bytes
                        @@immutable memo: bytes
                    }
                    """));
            // the attribute `bytes` shadows the builtin for the attributes declared after it
            assertThat(module(files, "/_transaction.py")).contains("    bytes: _builtins.bytes | None")
                    .contains("    memo: _builtins.bytes");
        }

        @Test
        void aStaticMethodShouldBeAStaticmethod() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // An address.
                    Address {
                        @@immutable id: string
                        // Parses one.
                        @@static Address parse(text: string)
                    }
                    """));
            assertThat(module(files, "/_address.py"))
                    .contains("    @staticmethod\n    def parse(text: str) -> Address:");
        }

        @Test
        void aNamespaceFunctionShouldBeAModuleLevelFunction() {
            final List<GeneratedFile> files = new PythonGenerator().generate(model("""
                    namespace a
                    // A client.
                    Client {
                        @@immutable id: string
                    }
                    // Creates a client.
                    @@static Client createClient(url: string)
                    """));
            assertThat(module(files, "/_functions.py"))
                    .contains("def create_client(url: str) -> Client:")
                    .contains("raise NotImplementedError(\"Not implemented yet: a.create_client\")")
                    .contains("__all__ = [\n    \"create_client\",\n]\n");
            // the package re-exports it, as it re-exports every type
            assertThat(module(files, "/a/__init__.py")).contains("from hiero_sdk.a._functions import create_client");
        }
    }

    @Nested
    class Gaps {

        @Test
        void declarationsWithoutPythonFormShouldBeDeferred() {
            // GIVEN
            final PythonGenerator generator = new PythonGenerator();

            // WHEN
            final List<GeneratedFile> files = generator.generate(model(Map.of("f/a.md", """
                    namespace a
                    Streamed { @@immutable items: streamResult<int32> }
                    UsesStreamed { @@immutable s: Streamed }
                    Base { @@immutable n: int32 }
                    Other { @@immutable m: int32 }
                    Twice extends Base, Other { @@immutable o: int32 }
                    abstraction FromClass extends Base {}
                    enum Named(name: string) { ONE("one") }
                    enum Short(code: int32, label: string) { ONE(1) }
                    Defaulted { @@immutable @@default(3) when: duration }
                    Unknown { @@immutable u: DoesNotExist }
                    @@sealed(Missing)
                    abstraction Closed {}
                    """), false));

            // THEN
            assertThat(generator.deferred()).containsOnlyKeys(
                    q("Streamed"), q("UsesStreamed"), q("Twice"), q("FromClass"), q("Named"), q("Short"),
                    q("Defaulted"), q("Unknown"), q("Closed"));
            assertThat(generator.deferred().get(q("UsesStreamed"))).contains("it references a.Streamed");
            assertThat(generator.deferred().get(q("Twice"))).contains("more than one concrete type");
            assertThat(generator.deferred().get(q("FromClass"))).contains("protocol cannot");
            assertThat(generator.deferred().get(q("Named"))).contains("clashes with Enum.name");
            assertThat(generator.deferred().get(q("Short"))).contains("gives 1 argument(s) for 2");
            assertThat(generator.deferred().get(q("Defaulted"))).contains("has no Python form");
            assertThat(generator.deferred().get(q("Closed"))).contains("Missing is not declared");
            assertThat(files).extracting(GeneratedFile::path).contains("src/hiero_sdk/a/_base.py")
                    .noneMatch(p -> p.endsWith("/_streamed.py") || p.endsWith("/_twice.py"));
            assertThat(file(files, "src/hiero_sdk/a/__init__.py")).doesNotContain("Streamed");
        }

        private static org.hiero.sdk.v3.metalang.model.QualifiedName q(final String name) {
            return new org.hiero.sdk.v3.metalang.model.QualifiedName("a", name);
        }
    }

    /**
     * Generates the API of the real specs and checks it with the interpreter: every package imports, and — when mypy
     * is installed for the interpreter — {@code mypy --strict} reports nothing.
     */
    @Nested
    class RealSpecs {

        @Test
        void theApiOfTheSpecsShouldImportAndTypeCheck() throws Exception {
            // GIVEN
            final PythonGenerator generator = new PythonGenerator();
            final List<GeneratedFile> files = generator.generate(LinkedModel.of(new MetaLang().validate(SPECS)
                    .model()));
            assertThat(generator.deferred()).isEmpty();
            assumeThat(run(temp, PYTHON, "-c", "import sys; assert sys.version_info >= (3, 12)").exit())
                    .as("Python 3.12 or newer as " + PYTHON).isZero();
            for (final GeneratedFile file : files) {
                final Path target = temp.resolve(file.path());
                Files.createDirectories(target.getParent());
                Files.writeString(target, file.content(), StandardCharsets.UTF_8);
            }

            // WHEN every package is imported
            final Result imported = run(temp, PYTHON, "-c", """
                    import importlib, pathlib, sys
                    sys.path.insert(0, "src")
                    for init in sorted(pathlib.Path("src").rglob("__init__.py")):
                        importlib.import_module(".".join(init.parent.relative_to("src").parts))
                    """);

            // THEN
            assertThat(imported.exit()).as(imported.output()).isZero();
            final Result mypy = run(temp, PYTHON, "-m", "mypy", "--version");
            if (mypy.exit() == 0) {
                final Result checked = run(temp, PYTHON, "-m", "mypy", "--strict", "src");
                assertThat(checked.exit()).as(checked.output()).isZero();
            }
        }
    }

    private record Result(int exit, String output) {
    }

    private static Result run(final Path directory, final String... command) throws IOException,
            InterruptedException {
        final Process process;
        try {
            process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        } catch (final IOException notInstalled) {
            return new Result(127, notInstalled.getMessage());
        }
        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(10, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            return new Result(124, output);
        }
        return new Result(process.exitValue(), output);
    }
}
