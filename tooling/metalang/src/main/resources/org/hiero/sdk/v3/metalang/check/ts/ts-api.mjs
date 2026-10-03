// Reads the public API of a TypeScript workspace (packages/<folder>/src/**) and prints it as tab-separated lines for
// the structural comparison of `metalang check --language=ts`. The sources are only parsed (no type checking), so a
// project can be checked without building it. A declaration belongs to the namespace of its directory
// (packages/<folder>/src/<namespace path>), so the files of a namespace may be organised freely. Type names are
// resolved through the imports to "<folder>:<namespace>#<Name>", so the import style does not matter.
//
// Usage: node ts-api.mjs <root> <directory of the typescript package>
//
// Output lines:
//   D  <id> <kind> <type parameters> <file> <line>       a declaration (an id may have several kinds, e.g. an
//                                                         interface and a namespace)
//   H  <id> <extends|implements> <type>                  a supertype
//   M  <id> <member> <signature> <file> <line>           a member; several lines of one member are its overloads
//   E  <file> <line> <message>                           a syntax error

import fs from "node:fs";
import path from "node:path";
import { pathToFileURL } from "node:url";

const [root, typescriptDirectory] = process.argv.slice(2);
const ts = (await import(pathToFileURL(path.join(typescriptDirectory, "lib", "typescript.js")).href)).default;

const out = [];
const clean = (text) => String(text).replace(/\s+/g, " ").trim();
const emit = (...fields) => out.push(fields.map(clean).join("\t"));

function sources(directory) {
    const result = [];
    if (!fs.existsSync(directory)) {
        return result;
    }
    for (const entry of fs.readdirSync(directory, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name))) {
        const file = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            if (entry.name !== "node_modules" && entry.name !== "dist") {
                result.push(...sources(file));
            }
        } else if (entry.name.endsWith(".ts") && !entry.name.endsWith(".test.ts") && !entry.name.endsWith(".d.ts")) {
            result.push(file);
        }
    }
    return result;
}

/** The namespace key of a directory: "<folder>:<namespace>" for packages/<folder>/src/<a>/<b>. */
function namespaceOf(directory) {
    const parts = path.relative(root, directory).split(path.sep);
    if (parts.length < 3 || parts[0] !== "packages" || parts[2] !== "src") {
        return null;
    }
    return parts[1] + ":" + parts.slice(3).join(".");
}

/** The namespace key of a module specifier, or null for modules outside of the workspace. */
function moduleNamespace(specifier, directory) {
    if (specifier.startsWith(".")) {
        return namespaceOf(path.dirname(path.resolve(directory, specifier)));
    }
    const match = /^@[^/]+\/([^/]+)\/(.+)$/.exec(specifier);
    return match ? match[1] + ":" + match[2].split("/").join(".") : null;
}

const packagesDirectory = path.join(root, "packages");
const files = fs.existsSync(packagesDirectory)
    ? fs.readdirSync(packagesDirectory).sort().flatMap((p) => sources(path.join(packagesDirectory, p, "src")))
    : [];

for (const file of files) {
    const relative = path.relative(root, file).split(path.sep).join("/");
    const text = fs.readFileSync(file, "utf8");
    const sourceFile = ts.createSourceFile(file, text, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);
    const line = (node) => sourceFile.getLineAndCharacterOfPosition(node.getStart(sourceFile)).line + 1;
    for (const diagnostic of sourceFile.parseDiagnostics ?? []) {
        emit("E", relative, sourceFile.getLineAndCharacterOfPosition(diagnostic.start ?? 0).line + 1,
            ts.flattenDiagnosticMessageText(diagnostic.messageText, " "));
    }
    const namespace = namespaceOf(path.dirname(file));
    if (namespace === null) {
        continue;
    }
    // imported names and the names declared in this file
    const imports = new Map();
    const local = new Set();
    for (const statement of sourceFile.statements) {
        if (ts.isImportDeclaration(statement) && statement.importClause && ts.isStringLiteral(statement.moduleSpecifier)) {
            const target = moduleNamespace(statement.moduleSpecifier.text, path.dirname(file));
            const bindings = statement.importClause.namedBindings;
            if (bindings && ts.isNamedImports(bindings)) {
                for (const element of bindings.elements) {
                    const imported = (element.propertyName ?? element.name).text;
                    imports.set(element.name.text, target === null ? imported : target + "#" + imported);
                }
            }
        } else if (statement.name && ts.isIdentifier(statement.name)) {
            local.add(statement.name.text);
        }
    }

    const qualify = (name, typeParameters) => {
        const [first, ...rest] = name.split(".");
        if (typeParameters.has(first)) {
            return name;
        }
        const resolved = imports.get(first) ?? (local.has(first) ? namespace + "#" + first : first);
        return [resolved, ...rest].join(".");
    };

    const typeText = (node, typeParameters) => {
        if (!node) {
            return "";
        }
        const t = (n) => typeText(n, typeParameters);
        switch (node.kind) {
            case ts.SyntaxKind.TypeReference: {
                const name = qualify(node.typeName.getText(sourceFile), typeParameters);
                const args = (node.typeArguments ?? []).map(t);
                if ((name === "Array" || name === "ReadonlyArray") && args.length === 1) {
                    return (name === "ReadonlyArray" ? "readonly " : "") + args[0] + "[]";
                }
                return name + (args.length ? "<" + args.join(", ") + ">" : "");
            }
            case ts.SyntaxKind.ArrayType:
                return t(node.elementType) + "[]";
            case ts.SyntaxKind.UnionType:
                return node.types.map(t).sort().join(" | ");
            case ts.SyntaxKind.IntersectionType:
                return node.types.map(t).sort().join(" & ");
            case ts.SyntaxKind.ParenthesizedType:
                return t(node.type);
            case ts.SyntaxKind.TypeOperator:
                return ts.tokenToString(node.operator) + " " + t(node.type);
            case ts.SyntaxKind.FunctionType:
            case ts.SyntaxKind.ConstructorType:
                return (node.kind === ts.SyntaxKind.ConstructorType ? "new " : "") + "("
                    + node.parameters.map((p) => parameterText(p, typeParameters)).join(", ") + ") => " + t(node.type);
            case ts.SyntaxKind.TypeLiteral:
                return "{ " + node.members.map((m) => memberText(m, typeParameters)).sort().join("; ") + " }";
            case ts.SyntaxKind.TupleType:
                return "[" + node.elements.map(t).join(", ") + "]";
            case ts.SyntaxKind.TypeQuery:
                return "typeof " + qualify(node.exprName.getText(sourceFile), typeParameters);
            default:
                return node.getText(sourceFile);
        }
    };

    const parameterText = (parameter, typeParameters) =>
        (parameter.dotDotDotToken ? "..." : "") + typeText(parameter.type, typeParameters)
        + (parameter.questionToken ? "?" : "");

    const memberText = (member, typeParameters) => {
        const name = member.name ? member.name.getText(sourceFile) : "";
        const readonly = (member.modifiers ?? []).some((m) => m.kind === ts.SyntaxKind.ReadonlyKeyword) ? "readonly " : "";
        if (ts.isPropertySignature(member)) {
            return readonly + name + (member.questionToken ? "?" : "") + ": " + typeText(member.type, typeParameters);
        }
        if (ts.isMethodSignature(member)) {
            return name + signatureText(member, typeParameters);
        }
        return member.getText(sourceFile);
    };

    const typeParameterNames = (node, outer) => {
        const names = new Set(outer);
        for (const parameter of node.typeParameters ?? []) {
            names.add(parameter.name.text);
        }
        return names;
    };

    const typeParametersText = (node, typeParameters) => (node.typeParameters ?? []).length === 0 ? ""
        : "<" + node.typeParameters.map((p) => p.name.text + (p.constraint ? " extends "
            + typeText(p.constraint, typeParameters) : "")).join(", ") + ">";

    const signatureText = (node, outer) => {
        const typeParameters = typeParameterNames(node, outer);
        return typeParametersText(node, typeParameters) + "("
            + node.parameters.map((p) => parameterText(p, typeParameters)).join(", ") + ")"
            + (node.type ? ": " + typeText(node.type, typeParameters) : "");
    };

    const has = (node, kind) => (node.modifiers ?? []).some((m) => m.kind === kind);
    const exported = (node) => has(node, ts.SyntaxKind.ExportKeyword);
    const hidden = (node) => node.name && ts.isPrivateIdentifier(node.name) || has(node, ts.SyntaxKind.PrivateKeyword);

    /** The signatures of an overloaded function or method: the implementation only if there are no overloads. */
    const overloads = (nodes) => {
        const declarations = nodes.filter((n) => !n.body);
        return declarations.length > 0 ? declarations : nodes;
    };

    const members = (id, nodes, typeParameters) => {
        const accessors = new Map();
        const methods = new Map();
        for (const member of nodes) {
            if (hidden(member)) {
                continue;
            }
            const isStatic = has(member, ts.SyntaxKind.StaticKeyword) ? "static " : "";
            const name = member.name ? member.name.getText(sourceFile) : "";
            if (ts.isPropertyDeclaration(member) || ts.isPropertySignature(member)) {
                const readonly = has(member, ts.SyntaxKind.ReadonlyKeyword) ? "readonly " : "";
                emit("M", id, isStatic + "property " + name, readonly + (member.questionToken ? "optional " : "")
                    + typeText(member.type, typeParameters), relative, line(member));
            } else if (ts.isGetAccessorDeclaration(member) || ts.isSetAccessorDeclaration(member)) {
                const key = isStatic + "property " + name;
                const accessor = accessors.get(key) ?? { line: line(member) };
                if (ts.isGetAccessorDeclaration(member)) {
                    accessor.type = typeText(member.type, typeParameters);
                } else {
                    accessor.set = true;
                    accessor.type ??= typeText(member.parameters[0]?.type, typeParameters);
                }
                accessors.set(key, accessor);
            } else if (ts.isMethodDeclaration(member) || ts.isMethodSignature(member)) {
                const key = isStatic + "method " + name;
                methods.set(key, [...(methods.get(key) ?? []), member]);
            } else if (ts.isConstructorDeclaration(member) || ts.isConstructSignatureDeclaration(member)) {
                methods.set("constructor", [...(methods.get("constructor") ?? []), member]);
            } else if (ts.isCallSignatureDeclaration(member)) {
                methods.set("call", [...(methods.get("call") ?? []), member]);
            } else if (ts.isIndexSignatureDeclaration(member)) {
                emit("M", id, "index", member.getText(sourceFile), relative, line(member));
            }
        }
        // a getter without setter is a readonly property; get and set are a property
        for (const [key, accessor] of accessors) {
            emit("M", id, key, (accessor.set ? "" : "readonly ") + accessor.type, relative, accessor.line);
        }
        for (const [key, nodes] of methods) {
            for (const node of overloads(nodes)) {
                emit("M", id, key, signatureText(node, typeParameters), relative, line(node));
            }
        }
    };

    const functions = new Map();
    for (const statement of sourceFile.statements) {
        if (!exported(statement)) {
            continue;
        }
        if (ts.isFunctionDeclaration(statement) && statement.name) {
            const id = namespace + "#" + statement.name.text;
            functions.set(id, [...(functions.get(id) ?? []), statement]);
            continue;
        }
        if (ts.isVariableStatement(statement)) {
            for (const declaration of statement.declarationList.declarations) {
                const id = namespace + "#" + declaration.name.getText(sourceFile);
                emit("D", id, "const", "", relative, line(declaration));
                emit("M", id, "type", typeText(declaration.type, new Set()), relative, line(declaration));
            }
            continue;
        }
        if (!statement.name) {
            continue;
        }
        const name = statement.name.text;
        const id = namespace + "#" + name;
        const typeParameters = typeParameterNames(statement, new Set());
        if (ts.isClassDeclaration(statement)) {
            emit("D", id, (has(statement, ts.SyntaxKind.AbstractKeyword) ? "abstract " : "") + "class",
                typeParametersText(statement, typeParameters), relative, line(statement));
            for (const clause of statement.heritageClauses ?? []) {
                for (const type of clause.types) {
                    const text = qualify(type.expression.getText(sourceFile), typeParameters)
                        + (type.typeArguments ? "<" + type.typeArguments.map((a) => typeText(a, typeParameters))
                            .join(", ") + ">" : "");
                    emit("H", id, clause.token === ts.SyntaxKind.ExtendsKeyword ? "extends" : "implements", text);
                }
            }
            members(id, statement.members, typeParameters);
        } else if (ts.isInterfaceDeclaration(statement)) {
            emit("D", id, "interface", typeParametersText(statement, typeParameters), relative, line(statement));
            for (const clause of statement.heritageClauses ?? []) {
                for (const type of clause.types) {
                    emit("H", id, "extends", qualify(type.expression.getText(sourceFile), typeParameters)
                        + (type.typeArguments ? "<" + type.typeArguments.map((a) => typeText(a, typeParameters))
                            .join(", ") + ">" : ""));
                }
            }
            members(id, statement.members, typeParameters);
        } else if (ts.isTypeAliasDeclaration(statement)) {
            emit("D", id, "type", typeParametersText(statement, typeParameters), relative, line(statement));
            emit("M", id, "type", typeText(statement.type, typeParameters), relative, line(statement));
        } else if (ts.isEnumDeclaration(statement)) {
            emit("D", id, "enum", "", relative, line(statement));
            for (const member of statement.members) {
                emit("M", id, "member " + member.name.getText(sourceFile), member.initializer
                    ? member.initializer.getText(sourceFile) : "", relative, line(member));
            }
        } else if (ts.isModuleDeclaration(statement) && statement.body && ts.isModuleBlock(statement.body)) {
            emit("D", id, "namespace", "", relative, line(statement));
            const nested = new Map();
            for (const inner of statement.body.statements) {
                if (exported(inner) && ts.isFunctionDeclaration(inner) && inner.name) {
                    nested.set(inner.name.text, [...(nested.get(inner.name.text) ?? []), inner]);
                }
            }
            for (const [functionName, nodes] of nested) {
                for (const node of overloads(nodes)) {
                    emit("M", id, "function " + functionName, signatureText(node, new Set()), relative, line(node));
                }
            }
        }
    }
    for (const [id, nodes] of functions) {
        emit("D", id, "function", "", relative, line(nodes[0]));
        for (const node of overloads(nodes)) {
            emit("M", id, "signature", signatureText(node, new Set()), relative, line(node));
        }
    }
}

process.stdout.write(out.join("\n") + (out.length ? "\n" : ""));
