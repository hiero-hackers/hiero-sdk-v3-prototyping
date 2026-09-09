import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const ROOT_DIR = path.resolve(__dirname, '..');
const SUBMODULE_PROTO_DIR = path.join(ROOT_DIR, 'src/base/internal/submodules/hiero-consensus-node/hapi/hedera-protobuf-java-api/src/main/proto');
const PROTO_DIR = path.join(ROOT_DIR, 'src/base/internal/proto-src');
const TEMP_DIR = path.join(ROOT_DIR, 'src/base/internal/proto-temp');

function ensureDir(dir) {
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

function removeDir(dir) {
    if (fs.existsSync(dir)) fs.rmSync(dir, { recursive: true, force: true });
}

// 1. Copy relevant proto directories from submodule
console.log('Copying proto files from submodule...');
removeDir(PROTO_DIR);
ensureDir(PROTO_DIR);

const dirsToCopy = [
    'services',
    'streams',
    'platform/event/*',
    'platform/state/*'
];

for (const dir of dirsToCopy) {
    const src = path.join(SUBMODULE_PROTO_DIR, dir);
    // Use shell to resolve wildcards
    execSync(`cp -r ${src} ${PROTO_DIR}/`, { stdio: 'inherit' });
}

// 2. Remove duplicates BEFORE flattening (as in V2)
console.log('Removing duplicate proto files...');
execSync('node scripts/remove-duplicate-protobuf.js', { stdio: 'inherit' });

// 3. Flatten and fix imports
console.log('Flattening proto directory structure...');
ensureDir(TEMP_DIR);

function findFiles(dir, minDepth, maxDepth, pattern = '.proto') {
    const results = [];
    function search(currentDir, currentDepth) {
        if (currentDepth > maxDepth) return;
        try {
            const entries = fs.readdirSync(currentDir, { withFileTypes: true });
            for (const entry of entries) {
                const fullPath = path.join(currentDir, entry.name);
                if (entry.isDirectory()) {
                    search(fullPath, currentDepth + 1);
                } else if (entry.isFile() && entry.name.endsWith(pattern)) {
                    if (currentDepth >= minDepth) results.push(fullPath);
                }
            }
        } catch (err) {}
    }
    search(dir, 1);
    return results;
}

const subdirFiles = findFiles(PROTO_DIR, 2, Infinity, '.proto');
for (const file of subdirFiles) {
    const relativePath = path.relative(PROTO_DIR, file);
    const flattenedName = relativePath.replace(/[/\\]/g, '_');
    fs.copyFileSync(file, path.join(TEMP_DIR, flattenedName));
}

const rootFiles = findFiles(PROTO_DIR, 1, 1, '.proto');
for (const file of rootFiles) {
    const filename = path.basename(file);
    let prefixedExists = false;
    if (fs.existsSync(path.join(TEMP_DIR, filename))) {
        prefixedExists = true;
    } else {
        const tempFiles = fs.readdirSync(TEMP_DIR);
        for (const tempFile of tempFiles) {
            if (tempFile.endsWith(`_${filename}`)) { prefixedExists = true; break; }
        }
    }
    if (!prefixedExists) {
        fs.copyFileSync(file, path.join(TEMP_DIR, filename));
    }
}

removeDir(PROTO_DIR);
fs.renameSync(TEMP_DIR, PROTO_DIR);

// Update imports
const allProtoFiles = fs.readdirSync(PROTO_DIR)
    .filter(f => f.endsWith('.proto'))
    .map(f => path.join(PROTO_DIR, f));

for (const file of allProtoFiles) {
    let content = fs.readFileSync(file, 'utf8').replace(/\r\n/g, '\n');
    let updatedContent = content;
    
    const importRegex = /import "([^"]*\.proto)";/g;
    const imports = [];
    let match;
    while ((match = importRegex.exec(content)) !== null) imports.push(match[1]);
    
    for (const importFile of imports) {
        if (importFile.startsWith('google/')) continue;
        
        const flatCandidate = importFile.replace(/[/\\]/g, '_');
        let newFilename = '';
        
        if (fs.existsSync(path.join(PROTO_DIR, flatCandidate))) {
            newFilename = flatCandidate;
        } else {
            const basename = path.basename(importFile);
            const protoFiles = fs.readdirSync(PROTO_DIR).filter(f => f.endsWith('.proto'));
            for (const protoFile of protoFiles) {
                if (protoFile.endsWith(`_${basename}`) || protoFile === basename) {
                    newFilename = protoFile;
                    break;
                }
            }
        }
        
        if (newFilename && newFilename !== importFile) {
            const oldImport = `import "${importFile}";`;
            const newImport = `import "${newFilename}";`;
            updatedContent = updatedContent.replace(new RegExp(oldImport.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), newImport);
        }
    }
    
    fs.writeFileSync(file, updatedContent, { encoding: 'utf8', flag: 'w' });
}

console.log('Done preparing proto files.');
