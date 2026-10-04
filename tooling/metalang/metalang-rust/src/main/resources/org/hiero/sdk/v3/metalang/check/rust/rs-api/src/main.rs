//! Reads the public API of the crates of a Cargo workspace and prints it as lines of tab-separated fields, one per
//! declaration (`D`), supertype or implemented trait (`H`), member (`M`) and parse error (`E`). Every item is named
//! by its shortest public path (`hiero_base::ledger::AccountId`), and every type in a signature is written with that
//! path (or with the full path of an external item), so that two workspaces can be compared independently of their
//! file layout and imports. Used by `metalang check --language=rust`.

use std::collections::{BTreeMap, BTreeSet, HashSet, VecDeque};
use std::path::{Path, PathBuf};

use quote::ToTokens;
use syn::visit_mut::{self, VisitMut};

struct Crate {
    name: String,
    root: usize,
}

#[derive(Clone)]
struct Use {
    alias: String,
    path: Vec<String>,
    glob: bool,
    public: bool,
}

struct Module {
    krate: usize,
    parent: Option<usize>,
    file: String,
    items: Vec<syn::Item>,
    children: BTreeMap<String, (usize, bool)>,
    defined: BTreeMap<String, (String, bool)>,
    uses: Vec<Use>,
}

#[derive(Clone, PartialEq)]
enum Target {
    Item(usize, String),
    Module(usize),
    External(String),
}

struct Api {
    workspace: PathBuf,
    crates: Vec<Crate>,
    modules: Vec<Module>,
    ids: BTreeMap<(usize, String), String>,
    module_ids: BTreeMap<usize, String>,
    out: BTreeSet<String>,
}

fn main() {
    let args: Vec<String> = std::env::args().collect();
    if args.len() != 2 {
        eprintln!("usage: rs-api <workspace directory>");
        std::process::exit(2);
    }
    let workspace = PathBuf::from(&args[1]);
    let mut api = Api {
        workspace: workspace.clone(),
        crates: Vec::new(),
        modules: Vec::new(),
        ids: BTreeMap::new(),
        module_ids: BTreeMap::new(),
        out: BTreeSet::new(),
    };
    let mut manifests = Vec::new();
    find_manifests(&workspace, &mut manifests);
    manifests.sort();
    for manifest in manifests {
        api.load_crate(&manifest);
    }
    api.canonical();
    api.emit();
    for line in &api.out {
        println!("{line}");
    }
}

fn find_manifests(dir: &Path, out: &mut Vec<PathBuf>) {
    let Ok(entries) = std::fs::read_dir(dir) else {
        return;
    };
    for entry in entries.flatten() {
        let path = entry.path();
        let name = entry.file_name().to_string_lossy().to_string();
        if path.is_dir() {
            if name != "target" && !name.starts_with('.') && name != "node_modules" {
                find_manifests(&path, out);
            }
        } else if name == "Cargo.toml" {
            out.push(path);
        }
    }
}

/// The library name of a manifest (`[lib] name` or the package name with `_`), if it has a package.
fn library_name(manifest: &str) -> Option<String> {
    let mut section = String::new();
    let mut package = None;
    let mut library = None;
    for line in manifest.lines() {
        let line = line.trim();
        if line.starts_with('[') {
            section = line.trim_matches(|c| c == '[' || c == ']').to_string();
        } else if let Some(value) = line.strip_prefix("name") {
            let value = value.trim_start().strip_prefix('=').map(|v| v.trim().trim_matches('"').to_string());
            match section.as_str() {
                "package" => package = value,
                "lib" => library = value,
                _ => {}
            }
        }
    }
    library.or(package).map(|n| n.replace('-', "_"))
}

fn tidy(text: &str) -> String {
    let mut t = text.to_string();
    for (from, to) in [
        (" :: ", "::"), (":: ", "::"), (" ::", "::"), (" ,", ","), ("( ", "("), (" )", ")"), ("< ", "<"),
        (" >", ">"), (" <", "<"), ("& ", "&"), (" :", ":"), ("[ ", "["), (" ]", "]"), (" ;", ";"), (" (", "("),
        ("' ", "'"),
    ] {
        while t.contains(from) {
            t = t.replace(from, to);
        }
    }
    t.replace("->", " -> ").replace("  ", " ").replace("-> ", "-> ").trim().to_string()
}

fn is_test_module(attrs: &[syn::Attribute]) -> bool {
    attrs.iter().any(|a| a.path().is_ident("cfg") && a.to_token_stream().to_string().contains("test"))
}

fn public(vis: &syn::Visibility) -> bool {
    matches!(vis, syn::Visibility::Public(_))
}

fn line(span: proc_macro2::Span) -> usize {
    span.start().line
}

fn flatten(tree: &syn::UseTree, prefix: Vec<String>, public: bool, out: &mut Vec<Use>) {
    match tree {
        syn::UseTree::Path(p) => {
            let mut next = prefix;
            next.push(p.ident.to_string());
            flatten(&p.tree, next, public, out);
        }
        syn::UseTree::Name(n) => {
            let name = n.ident.to_string();
            if name == "self" {
                if let Some(last) = prefix.last() {
                    out.push(Use { alias: last.clone(), path: prefix.clone(), glob: false, public });
                }
            } else {
                let mut path = prefix;
                path.push(name.clone());
                out.push(Use { alias: name, path, glob: false, public });
            }
        }
        syn::UseTree::Rename(r) => {
            let mut path = prefix;
            path.push(r.ident.to_string());
            out.push(Use { alias: r.rename.to_string(), path, glob: false, public });
        }
        syn::UseTree::Glob(_) => out.push(Use { alias: String::new(), path: prefix, glob: true, public }),
        syn::UseTree::Group(g) => {
            for item in &g.items {
                flatten(item, prefix.clone(), public, out);
            }
        }
    }
}

impl Api {
    fn relative(&self, path: &Path) -> String {
        path.strip_prefix(&self.workspace).unwrap_or(path).to_string_lossy().replace('\\', "/")
    }

    fn load_crate(&mut self, manifest: &Path) {
        let Ok(text) = std::fs::read_to_string(manifest) else {
            return;
        };
        let Some(name) = library_name(&text) else {
            return;
        };
        let dir = manifest.parent().unwrap_or(Path::new("."));
        let lib = dir.join("src/lib.rs");
        if !lib.is_file() {
            return;
        }
        let krate = self.crates.len();
        self.crates.push(Crate { name, root: 0 });
        let root = self.load_file(krate, None, &lib, &dir.join("src"));
        self.crates[krate].root = root;
    }

    fn load_file(&mut self, krate: usize, parent: Option<usize>, file: &Path, children: &Path) -> usize {
        let display = self.relative(file);
        let items = match std::fs::read_to_string(file) {
            Ok(text) => match syn::parse_file(&text) {
                Ok(parsed) => parsed.items,
                Err(e) => {
                    self.out.insert(format!("E\t{display}\t{}\t{}", line(e.span()), e.to_string().replace('\t', " ")));
                    Vec::new()
                }
            },
            Err(e) => {
                self.out.insert(format!("E\t{display}\t0\t{e}"));
                Vec::new()
            }
        };
        self.add_module(krate, parent, display, items, children)
    }

    fn add_module(&mut self, krate: usize, parent: Option<usize>, file: String, items: Vec<syn::Item>,
                  children: &Path) -> usize {
        let index = self.modules.len();
        self.modules.push(Module {
            krate,
            parent,
            file,
            items: Vec::new(),
            children: BTreeMap::new(),
            defined: BTreeMap::new(),
            uses: Vec::new(),
        });
        let mut kept = Vec::new();
        for item in items {
            match &item {
                syn::Item::Mod(m) if !is_test_module(&m.attrs) => {
                    let name = m.ident.to_string();
                    let dir = children.join(&name);
                    let child = if let Some((_, content)) = &m.content {
                        let file = self.modules[index].file.clone();
                        self.add_module(krate, Some(index), file, content.clone(), &dir)
                    } else {
                        let flat = children.join(format!("{name}.rs"));
                        let nested = dir.join("mod.rs");
                        let file = if flat.is_file() { flat } else { nested };
                        self.load_file(krate, Some(index), &file, &dir)
                    };
                    self.modules[index].children.insert(name, (child, public(&m.vis)));
                }
                syn::Item::Mod(_) => {}
                syn::Item::Use(u) => {
                    let mut uses = Vec::new();
                    flatten(&u.tree, Vec::new(), public(&u.vis), &mut uses);
                    self.modules[index].uses.extend(uses);
                }
                _ => {
                    if let Some((name, kind, vis)) = definition(&item) {
                        self.modules[index].defined.insert(name, (kind.to_string(), vis));
                    }
                    kept.push(item);
                }
            }
        }
        self.modules[index].items = kept;
        index
    }

    fn crate_root(&self, name: &str) -> Option<usize> {
        self.crates.iter().find(|c| c.name == name).map(|c| c.root)
    }

    /// Looks a name up in the scope of a module.
    fn lookup(&self, module: usize, name: &str, depth: usize) -> Option<Target> {
        if depth > 24 {
            return None;
        }
        let m = &self.modules[module];
        if let Some((child, _)) = m.children.get(name) {
            return Some(Target::Module(*child));
        }
        if m.defined.contains_key(name) {
            return Some(Target::Item(module, name.to_string()));
        }
        for u in m.uses.iter().filter(|u| !u.glob && u.alias == name) {
            if let Some(target) = self.resolve(module, &u.path, depth + 1) {
                return Some(target);
            }
        }
        for u in m.uses.iter().filter(|u| u.glob) {
            if let Some(Target::Module(g)) = self.resolve(module, &u.path, depth + 1) {
                if g != module {
                    if let Some(target) = self.lookup(g, name, depth + 1) {
                        return Some(target);
                    }
                }
            }
        }
        None
    }

    /// Resolves a path in the scope of a module.
    fn resolve(&self, module: usize, segments: &[String], depth: usize) -> Option<Target> {
        if segments.is_empty() || depth > 24 {
            return None;
        }
        let mut index = 0;
        let mut current = match segments[0].as_str() {
            "crate" => {
                index = 1;
                Target::Module(self.crates[self.modules[module].krate].root)
            }
            "self" => {
                index = 1;
                Target::Module(module)
            }
            "super" => {
                let mut m = module;
                while index < segments.len() && segments[index] == "super" {
                    m = self.modules[m].parent?;
                    index += 1;
                }
                Target::Module(m)
            }
            "Self" => return None,
            first => {
                index = 1;
                match self.lookup(module, first, depth) {
                    Some(target) => target,
                    None => match self.crate_root(first) {
                        Some(root) => Target::Module(root),
                        None => return Some(Target::External(segments.join("::"))),
                    },
                }
            }
        };
        for segment in &segments[index..] {
            current = match current {
                Target::Module(m) => self.lookup(m, segment, depth + 1)?,
                _ => return None,
            };
        }
        Some(current)
    }

    /// The shortest public path of every item and module.
    fn canonical(&mut self) {
        let mut queue: VecDeque<(usize, String)> = VecDeque::new();
        for c in &self.crates {
            queue.push_back((c.root, c.name.clone()));
        }
        let mut visited: HashSet<(usize, String)> = HashSet::new();
        let mut ids: BTreeMap<(usize, String), String> = BTreeMap::new();
        let mut modules: BTreeMap<usize, String> = BTreeMap::new();
        let better = |old: Option<&String>, new: &String| match old {
            None => true,
            Some(old) => {
                let (a, b) = (old.matches("::").count(), new.matches("::").count());
                b < a || (b == a && new < old)
            }
        };
        while let Some((module, prefix)) = queue.pop_front() {
            if prefix.matches("::").count() > 16 || !visited.insert((module, prefix.clone())) {
                continue;
            }
            if better(modules.get(&module), &prefix) {
                modules.insert(module, prefix.clone());
            }
            let m = &self.modules[module];
            for (name, (_, public)) in &m.defined {
                if *public {
                    let id = format!("{prefix}::{name}");
                    let key = (module, name.clone());
                    if better(ids.get(&key), &id) {
                        ids.insert(key, id);
                    }
                }
            }
            for (name, (child, public)) in &m.children {
                if *public {
                    queue.push_back((*child, format!("{prefix}::{name}")));
                }
            }
            for u in m.uses.iter().filter(|u| u.public) {
                match self.resolve(module, &u.path, 0) {
                    Some(Target::Module(g)) => {
                        let next = if u.glob { prefix.clone() } else { format!("{prefix}::{}", u.alias) };
                        queue.push_back((g, next));
                    }
                    Some(Target::Item(dm, name)) if !u.glob && u.alias != "_" => {
                        let id = format!("{prefix}::{}", u.alias);
                        let key = (dm, name);
                        if better(ids.get(&key), &id) {
                            ids.insert(key, id);
                        }
                    }
                    _ => {}
                }
            }
        }
        self.ids = ids;
        self.module_ids = modules;
    }

    /// The text of a resolved path: the public path of an item, the full path of an external one.
    fn path_text(&self, module: usize, segments: &[String]) -> Option<String> {
        match self.resolve(module, segments, 0)? {
            Target::Item(m, name) => Some(self.ids.get(&(m, name.clone())).cloned().unwrap_or_else(|| {
                format!("{}::{}", self.module_ids.get(&m).cloned().unwrap_or_default(), name)
            })),
            Target::Module(m) => self.module_ids.get(&m).cloned(),
            Target::External(path) => Some(path),
        }
    }

    fn normalize<T: ToTokens + Clone>(&self, module: usize, generics: &HashSet<String>, node: &T,
                                      visit: fn(&mut Rewriter, &mut T)) -> String {
        let mut copy = node.clone();
        let mut rewriter = Rewriter { api: self, module, generics: generics.clone() };
        visit(&mut rewriter, &mut copy);
        tidy(&copy.to_token_stream().to_string())
    }

    fn signature(&self, module: usize, scope: &HashSet<String>, sig: &syn::Signature) -> String {
        let mut generics = scope.clone();
        generics.extend(generic_names(&sig.generics));
        let mut sig = sig.clone();
        for input in sig.inputs.iter_mut() {
            match input {
                syn::FnArg::Receiver(r) => r.attrs.clear(),
                syn::FnArg::Typed(t) => t.attrs.clear(),
            }
        }
        self.normalize(module, &generics, &sig, |r, s| r.visit_signature_mut(s))
    }

    fn emit(&mut self) {
        let mut lines = BTreeSet::new();
        for (index, module) in self.modules.iter().enumerate() {
            for item in &module.items {
                self.emit_item(index, item, &mut lines);
            }
        }
        self.out.extend(lines);
    }

    fn id(&self, module: usize, name: &str) -> Option<&String> {
        self.ids.get(&(module, name.to_string()))
    }

    fn emit_item(&self, module: usize, item: &syn::Item, out: &mut BTreeSet<String>) {
        let file = &self.modules[module].file;
        let at = |span: proc_macro2::Span| format!("{file}\t{}", line(span));
        match item {
            syn::Item::Struct(s) => {
                let Some(id) = self.id(module, &s.ident.to_string()) else { return };
                let scope = generic_names(&s.generics);
                out.insert(format!("D\t{id}\tstruct\t{}\t{}", self.generics(module, &s.generics), at(s.ident.span())));
                derives(&s.attrs, id, out);
                for field in &s.fields {
                    if public(&field.vis) {
                        if let Some(name) = &field.ident {
                            out.insert(format!("M\t{id}\tfield {name}\t{}\t{}",
                                self.normalize(module, &scope, &field.ty, |r, t| r.visit_type_mut(t)), at(name.span())));
                        }
                    }
                }
            }
            syn::Item::Enum(e) => {
                let Some(id) = self.id(module, &e.ident.to_string()) else { return };
                let scope = generic_names(&e.generics);
                out.insert(format!("D\t{id}\tenum\t{}\t{}", self.generics(module, &e.generics), at(e.ident.span())));
                derives(&e.attrs, id, out);
                for variant in &e.variants {
                    let fields = self.normalize(module, &scope, &variant.fields, |r, f| r.visit_fields_mut(f));
                    out.insert(format!("M\t{id}\tvariant {}\t{fields}\t{}", variant.ident, at(variant.ident.span())));
                }
            }
            syn::Item::Trait(t) => {
                let Some(id) = self.id(module, &t.ident.to_string()) else { return };
                let scope = generic_names(&t.generics);
                out.insert(format!("D\t{id}\ttrait\t{}\t{}", self.generics(module, &t.generics), at(t.ident.span())));
                for bound in &t.supertraits {
                    if let syn::TypeParamBound::Trait(b) = bound {
                        out.insert(format!("H\t{id}\textends\t{}",
                            self.normalize(module, &scope, b, |r, b| r.visit_trait_bound_mut(b))));
                    }
                }
                for member in &t.items {
                    match member {
                        syn::TraitItem::Fn(f) => {
                            out.insert(format!("M\t{id}\tfn {}\t{}\t{}", f.sig.ident,
                                self.signature(module, &scope, &f.sig), at(f.sig.ident.span())));
                        }
                        syn::TraitItem::Type(ty) => {
                            out.insert(format!("M\t{id}\ttype {}\t{}\t{}", ty.ident,
                                self.normalize(module, &scope, ty, |r, t| r.visit_trait_item_type_mut(t)),
                                at(ty.ident.span())));
                        }
                        syn::TraitItem::Const(c) => {
                            out.insert(format!("M\t{id}\tconst {}\t{}\t{}", c.ident,
                                self.normalize(module, &scope, &c.ty, |r, t| r.visit_type_mut(t)),
                                at(c.ident.span())));
                        }
                        _ => {}
                    }
                }
            }
            syn::Item::Fn(f) => {
                let Some(id) = self.id(module, &f.sig.ident.to_string()) else { return };
                out.insert(format!("D\t{id}\tfn\t{}\t{}", self.signature(module, &HashSet::new(), &f.sig),
                    at(f.sig.ident.span())));
            }
            syn::Item::Const(c) => {
                let Some(id) = self.id(module, &c.ident.to_string()) else { return };
                out.insert(format!("D\t{id}\tconst\t{}\t{}",
                    self.normalize(module, &HashSet::new(), c.ty.as_ref(), |r, t| r.visit_type_mut(t)),
                    at(c.ident.span())));
            }
            syn::Item::Static(s) => {
                let Some(id) = self.id(module, &s.ident.to_string()) else { return };
                out.insert(format!("D\t{id}\tstatic\t{}\t{}",
                    self.normalize(module, &HashSet::new(), s.ty.as_ref(), |r, t| r.visit_type_mut(t)),
                    at(s.ident.span())));
            }
            syn::Item::Type(t) => {
                let Some(id) = self.id(module, &t.ident.to_string()) else { return };
                let scope = generic_names(&t.generics);
                out.insert(format!("D\t{id}\ttype\t{} = {}\t{}", self.generics(module, &t.generics),
                    self.normalize(module, &scope, t.ty.as_ref(), |r, t| r.visit_type_mut(t)), at(t.ident.span())));
            }
            syn::Item::Impl(i) => self.emit_impl(module, i, out),
            _ => {}
        }
    }

    fn emit_impl(&self, module: usize, item: &syn::ItemImpl, out: &mut BTreeSet<String>) {
        let file = &self.modules[module].file;
        let scope = generic_names(&item.generics);
        let (target, dynamic) = match item.self_ty.as_ref() {
            syn::Type::Path(p) if p.qself.is_none() => (path_segments(&p.path), false),
            syn::Type::TraitObject(o) => match o.bounds.first() {
                Some(syn::TypeParamBound::Trait(b)) => (path_segments(&b.path), true),
                _ => return,
            },
            _ => return,
        };
        if target.len() == 1 && scope.contains(&target[0]) {
            return; // a blanket implementation
        }
        let Some(Target::Item(m, name)) = self.resolve(module, &target, 0) else { return };
        let Some(id) = self.id(m, &name) else { return };
        if let Some((_, trait_path, _)) = &item.trait_ {
            let normalized = self.normalize(module, &scope, trait_path, |r, p| r.visit_path_mut(p));
            out.insert(format!("H\t{id}\timplements\t{}", short_trait(&normalized)));
            return;
        }
        for member in &item.items {
            if let syn::ImplItem::Fn(f) = member {
                if public(&f.vis) {
                    out.insert(format!("M\t{id}\t{}fn {}\t{}\t{file}\t{}", if dynamic { "dyn " } else { "" },
                        f.sig.ident, self.signature(module, &scope, &f.sig), line(f.sig.ident.span())));
                }
            } else if let syn::ImplItem::Const(c) = member {
                if public(&c.vis) {
                    out.insert(format!("M\t{id}\tconst {}\t{}\t{file}\t{}", c.ident,
                        self.normalize(module, &scope, &c.ty, |r, t| r.visit_type_mut(t)), line(c.ident.span())));
                }
            }
        }
    }

    fn generics(&self, module: usize, generics: &syn::Generics) -> String {
        if generics.params.is_empty() {
            return String::new();
        }
        let scope = generic_names(generics);
        let mut copy = generics.clone();
        copy.where_clause = None;
        self.normalize(module, &scope, &copy, |r, g| r.visit_generics_mut(g))
    }
}

/// The well-known traits of the standard library are written by their name.
fn short_trait(path: &str) -> String {
    for name in ["Clone", "Copy", "Debug", "PartialEq", "Eq", "Hash", "Default", "Display", "FromStr", "From",
        "Error", "PartialOrd", "Ord"] {
        for prefix in ["std::fmt::", "core::fmt::", "std::clone::", "std::cmp::", "std::hash::", "std::default::",
            "std::str::", "std::convert::", "std::error::", "core::clone::", "core::cmp::", "core::hash::",
            "core::str::", "core::convert::", ""] {
            let full = format!("{prefix}{name}");
            if path == full || path.starts_with(&format!("{full}<")) {
                return format!("{name}{}", &path[full.len()..]);
            }
        }
    }
    path.to_string()
}

fn derives(attrs: &[syn::Attribute], id: &str, out: &mut BTreeSet<String>) {
    for attr in attrs {
        if attr.path().is_ident("derive") {
            let _ = attr.parse_nested_meta(|meta| {
                if let Some(name) = meta.path.segments.last() {
                    out.insert(format!("H\t{id}\timplements\t{}", name.ident));
                }
                Ok(())
            });
        }
    }
}

fn definition(item: &syn::Item) -> Option<(String, &'static str, bool)> {
    match item {
        syn::Item::Struct(s) => Some((s.ident.to_string(), "struct", public(&s.vis))),
        syn::Item::Enum(e) => Some((e.ident.to_string(), "enum", public(&e.vis))),
        syn::Item::Trait(t) => Some((t.ident.to_string(), "trait", public(&t.vis))),
        syn::Item::Fn(f) => Some((f.sig.ident.to_string(), "fn", public(&f.vis))),
        syn::Item::Const(c) => Some((c.ident.to_string(), "const", public(&c.vis))),
        syn::Item::Static(s) => Some((s.ident.to_string(), "static", public(&s.vis))),
        syn::Item::Type(t) => Some((t.ident.to_string(), "type", public(&t.vis))),
        syn::Item::Union(u) => Some((u.ident.to_string(), "union", public(&u.vis))),
        _ => None,
    }
}

fn generic_names(generics: &syn::Generics) -> HashSet<String> {
    generics.params.iter().filter_map(|p| match p {
        syn::GenericParam::Type(t) => Some(t.ident.to_string()),
        _ => None,
    }).collect()
}

fn path_segments(path: &syn::Path) -> Vec<String> {
    path.segments.iter().map(|s| s.ident.to_string()).collect()
}

struct Rewriter<'a> {
    api: &'a Api,
    module: usize,
    generics: HashSet<String>,
}

impl VisitMut for Rewriter<'_> {
    fn visit_path_mut(&mut self, path: &mut syn::Path) {
        visit_mut::visit_path_mut(self, path);
        if path.leading_colon.is_some() {
            return;
        }
        let segments = path_segments(path);
        if segments.is_empty() || segments[0] == "Self" || (segments.len() == 1 && self.generics.contains(&segments[0]))
            || self.generics.contains(&segments[0]) {
            return;
        }
        let Some(text) = self.api.path_text(self.module, &segments) else { return };
        let Ok(mut replacement) = syn::parse_str::<syn::Path>(&text) else { return };
        if let (Some(last), Some(original)) = (replacement.segments.last_mut(), path.segments.last()) {
            last.arguments = original.arguments.clone();
        }
        *path = replacement;
    }
}
