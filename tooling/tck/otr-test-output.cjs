// The output of every single TCK test, for the Open Test Reporting results (ADR-0012): a mocha root hook plugin that
// tooling/tck/run-tck.sh loads with `--require` (in mocha's parallel mode it runs in every worker), and the naming of
// the files that tooling/tck/otr-mocha-reporter.cjs attaches to the test.
//
// While a test runs (from the first beforeEach to the last afterEach hook), everything it writes to stdout and stderr
// also goes to a file of that test, and every JSON-RPC request to the TCK server carries the id of the test in the
// header X-TCK-Test-Id; a server that knows the header writes its log of the call to a third file (the Java server:
// sdk-java/tck/runtime, JsonRpcServer). The output still reaches the console as before.
//
// Environment:
//   OTR_TEST_OUTPUT_DIR  the directory of the files (default: nothing is captured)
//   JSON_RPC_SERVER_URL  the TCK server (the TCK configuration): only requests to it get the header
//
// The files are named by mocha's id of the test, which is the same in a worker and in the main process that reports
// the results: <id>.stdout.log, <id>.stderr.log and <id>.server.log.
"use strict";

const fs = require("node:fs");
const path = require("node:path");

/** The header that names the test of a JSON-RPC request. */
const TEST_ID_HEADER = "X-TCK-Test-Id";

/** The sources of the output of a test, as Open Test Reporting names them, with the suffix of their files. */
const SOURCES = { stdout: "stdout", stderr: "stderr", "tck-server": "server" };

/**
 * The file of one kind of output of a test.
 *
 * @param directory the directory of the files
 * @param id        mocha's id of the test
 * @param source    a key of SOURCES
 */
function outputFile(directory, id, source) {
    return path.join(directory, String(id).replace(/[^A-Za-z0-9_-]/g, "_") + "." + SOURCES[source] + ".log");
}

// set by the reporter while it writes the result of a test: without --parallel it runs in the same process, after the
// test and before its afterEach hooks
const state = { paused: false };

const directory = process.env.OTR_TEST_OUTPUT_DIR;
let current; // the test that runs: { id, stdout: [], stderr: [], restore() }

function capture(id) {
    release();
    const test = { id, stdout: [], stderr: [], restore: [] };
    for (const name of ["stdout", "stderr"]) {
        const stream = process[name];
        const write = stream.write;
        stream.write = function (chunk, ...rest) {
            if (!state.paused) {
                test[name].push(typeof chunk === "string" ? chunk : Buffer.from(chunk).toString("utf8"));
            }
            return write.call(this, chunk, ...rest);
        };
        test.restore.push(() => {
            stream.write = write;
        });
    }
    current = test;
}

function release() {
    const test = current;
    if (!test) {
        return;
    }
    current = undefined;
    test.restore.forEach((restore) => restore());
    for (const name of ["stdout", "stderr"]) {
        if (test[name].length > 0) {
            fs.appendFileSync(outputFile(directory, test.id, name), test[name].join(""));
        }
    }
}

// the header on every request of the TCK to its server; axios of the TCK clone, the working directory
function addTestIdHeader() {
    const server = process.env.JSON_RPC_SERVER_URL;
    let axios;
    try {
        axios = require(require.resolve("axios", { paths: [process.cwd()] }));
    } catch {
        return;
    }
    (axios.default || axios).interceptors.request.use((config) => {
        if (current && (!server || String(config.url || "").startsWith(server))) {
            config.headers.set(TEST_ID_HEADER, String(current.id));
        }
        return config;
    });
}

function testId(context) {
    const test = context.currentTest;
    return test && (test.id || test.__mocha_id__);
}

const mochaHooks = {};
if (directory) {
    fs.mkdirSync(directory, { recursive: true });
    addTestIdHeader();
    mochaHooks.beforeEach = function () {
        const id = testId(this);
        if (id) {
            capture(id);
        }
    };
    mochaHooks.afterEach = function () {
        release();
    };
}

module.exports = { mochaHooks, outputFile, state, SOURCES, TEST_ID_HEADER };
