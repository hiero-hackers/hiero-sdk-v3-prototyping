# scripts

One entry point per task, so that nobody has to assemble the CLI invocations of
[`tooling/metalang/README.md`](../tooling/metalang/README.md) by hand. Every script is run from
anywhere (it resolves the repository itself), builds the `metalang` CLI if the jar is missing, and
is safe to run repeatedly.

| Script | What it does |
|---|---|
| `generate-all.sh` | Generates and builds all five languages; every language is attempted even if an earlier one failed, the summary names what failed |
| `generate-java.sh` | Java API into `sdk-java/generated`, then `support`, `protobuf` and the generated modules into the local Maven repository |
| `generate-ts.sh` | TypeScript API into `sdk-ts/generated`, then `npm install` in the workspace and `tsc --build` |
| `generate-rust.sh` | Rust API into `sdk-rust/generated`, then `cargo build --all-targets` |
| `generate-go.sh` | Go API into `sdk-go/generated`, then `go build`, `go vet` and the `gofmt -l` check |
| `generate-python.sh` | Python API into `sdk-python/generated`, then `mypy --strict` |
| `tck-java.sh` | Generates API + TCK server, builds them and runs the Hiero TCK against the Java server |
| `tck-ts.sh` | The same for the TypeScript server |

`common.sh` is the shared library (repository location, JDK resolution, CLI build) and is sourced,
never run.

## Options and environment

Every `generate-*.sh` takes:

- `--no-build` — only generate, do not compile or check
- `--test` — also run the generated tests. They fail for every method that is still a stub, which is
  what makes the implementation status visible; the scripts therefore do not fail on them. The Go
  and Python generators emit no tests yet, so the option does nothing there.
- `REBUILD_CLI=1` — build the `metalang` CLI even if the jar is there (after a change of the tooling)
- `PYTHON=<interpreter>` — the interpreter for `generate-python.sh` (default `python3`); it needs to
  be Python 3.12 or newer
- `SKIP_TYPE_CHECK=1` — generate the Python API without running mypy

mypy does not have to be installed: if the interpreter has none, `generate-python.sh` creates the
virtual environment `sdk-python/.venv` (git-ignored) and installs mypy there once. That is also the
way around a Python that refuses `pip install` because its distribution marks it as externally
managed (PEP 668), which is what Homebrew and most Linux distributions do.

The JDK is not taken from the `PATH`: a Maven build gets the version pinned in the `.sdkmanrc` next
to it, from `$JAVA_HOME` if that is new enough, otherwise from the SDKMAN! installation or from
`/usr/libexec/java_home`. For everything else see the toolchain section of the
[main README](../README.md#toolchain).

The two TCK scripts take `[--skip-build] [TCK test file ...]` and need a running
[Solo](https://solo.hiero.org) network plus a clone of the
[Hiero TCK](https://github.com/hiero-ledger/hiero-sdk-tck):

```bash
solo one-shot single deploy
git clone https://github.com/hiero-ledger/hiero-sdk-tck.git ../hiero-sdk-tck
npm --prefix ../hiero-sdk-tck install
TCK_DIR=../hiero-sdk-tck scripts/tck-java.sh src/tests/crypto-service/test-account-create-transaction.ts
```

`TCK_DIR` is required; `TCK_ENV` selects another configuration than `tooling/tck/solo.env` and
`TCK_SERVER_LOG=debug|off` changes how much the server logs about failed calls. `--skip-build` runs
the TCK against what was built before.

There is no TCK script for Rust, Go and Python: `metalang tck generate` supports `--language=java`
and `--language=ts` only.
