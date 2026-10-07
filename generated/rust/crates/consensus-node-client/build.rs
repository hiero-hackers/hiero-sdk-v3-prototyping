//! Compiles the vendored protobuf definitions of the consensus node into `OUT_DIR`.
//!
//! The generated code is an implementation detail, not public API: `lib.rs` includes it through a
//! private `mod proto`, so nothing leaves the crate. `protoc` comes from `protoc-bin-vendored`, so
//! building this crate needs no protobuf installation.

use std::io::Result;
use std::path::{Path, PathBuf};

fn main() -> Result<()> {
    // /protobuf at the repository root; see protobuf/README.md
    let root = Path::new(env!("CARGO_MANIFEST_DIR"))
        .join("../../../../protobuf/consensus-node")
        .canonicalize()?;

    let mut files: Vec<PathBuf> = Vec::new();
    collect(&root, &mut files)?;
    // consensus-node/mirror duplicates the mirror node's own definitions (same proto package,
    // same messages); nothing imports it. See protobuf/README.md, "Findings".
    files.retain(|file| !file.starts_with(root.join("mirror")));
    files.sort();

    for file in &files {
        println!("cargo:rerun-if-changed={}", file.display());
    }

    // SAFETY: build scripts are single-threaded, this runs before prost-build reads the variable.
    unsafe {
        std::env::set_var(
            "PROTOC",
            protoc_bin_vendored::protoc_bin_path().expect("a vendored protoc for this platform"),
        );
    }

    prost_build::Config::new()
        // one file with the whole module tree, so lib.rs needs a single include!
        .include_file("_protobuf.rs")
        .compile_protos(&files, &[root])
}

fn collect(directory: &Path, files: &mut Vec<PathBuf>) -> Result<()> {
    for entry in std::fs::read_dir(directory)? {
        let path = entry?.path();
        if path.is_dir() {
            collect(&path, files)?;
        } else if path.extension().is_some_and(|extension| extension == "proto") {
            files.push(path);
        }
    }
    Ok(())
}
