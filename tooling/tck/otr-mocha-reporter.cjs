// A mocha reporter for the Hiero TCK: the usual console output (mocha's `spec` reporter) plus the results as Open
// Test Reporting (event-based XML, schema 0.2.0, ADR-0012), so that they can be rendered and read together with the
// results of the SDK's own tests. tooling/tck/run-tck.sh passes it to mocha with `--reporter`.
//
// Environment:
//   OTR_OUTPUT  the file to write (default: open-test-report.xml in the working directory); a JSON summary is written
//               next to it (<file>.summary.json)
//   TCK_SDK     the language of the SDK under test (java|ts), written as data entry of the root
//   OTR_GIT_DIR the Git repository of the SDK under test (default: none): its origin (without credentials), branch,
//               commit and status are written into the infrastructure (Git extension of Open Test Reporting); the
//               commit of the TCK clone (the working directory) is a data entry of the root
//
// Every test gets the tag `testId:tck/<file>/<title path in kebab-case>` (the file relative to src/tests, without
// extension). In mocha's parallel mode the events of a file arrive when the file is finished, so the times are those
// of the reporter and the durations are approximate.
"use strict";

const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const { execFileSync } = require("node:child_process");

// mocha of the TCK clone (this file lives outside of it)
const Mocha = require(require.resolve("mocha", { paths: [process.cwd()] }));
const { EVENT_RUN_BEGIN, EVENT_RUN_END, EVENT_SUITE_BEGIN, EVENT_SUITE_END, EVENT_TEST_BEGIN, EVENT_TEST_PASS,
    EVENT_TEST_FAIL, EVENT_TEST_PENDING } = Mocha.Runner.constants;

const ROOT_ID = "tck";
const MOCHA_ID = "__mocha_id__";

class OpenTestReportingReporter extends Mocha.reporters.Spec {

    constructor(runner, options) {
        super(runner, options);
        this.file = path.resolve(process.env.OTR_OUTPUT || "open-test-report.xml");
        this.started = new Set();
        this.finished = new Set();
        this.counts = { tests: 0, passed: 0, failed: 0, skipped: 0 };
        this.fd = undefined; // opened when the run begins: a run that stops in the TCK's preflight leaves no file

        runner.once(EVENT_RUN_BEGIN, () => this.begin());
        runner.on(EVENT_SUITE_BEGIN, (suite) => {
            if (!suite.root) {
                this.start(this.id(suite), this.parent(suite), suite.title, this.fileOf(suite));
            }
        });
        runner.on(EVENT_SUITE_END, (suite) => {
            if (!suite.root) {
                this.finish(this.id(suite), "SUCCESSFUL");
            }
        });
        runner.on(EVENT_TEST_BEGIN, (test) => this.startTest(test));
        runner.on(EVENT_TEST_PASS, (test) => {
            this.counts.passed++;
            this.finish(this.id(test), "SUCCESSFUL");
        });
        runner.on(EVENT_TEST_FAIL, (test, error) => {
            if (test.type === "hook") {
                // a failed hook (before/after) is no test: it fails the suite it belongs to
                const id = this.id(test);
                this.start(id, this.parent(test), test.title, test.file);
                this.finish(id, "ERRORED", message(error));
                return;
            }
            this.startTest(test);
            this.counts.failed++;
            this.finish(this.id(test), "FAILED", message(error));
        });
        runner.on(EVENT_TEST_PENDING, (test) => {
            this.startTest(test);
            this.counts.skipped++;
            this.finish(this.id(test), "SKIPPED");
        });
        runner.once(EVENT_RUN_END, () => this.end());
    }

    begin() {
        fs.mkdirSync(path.dirname(this.file), { recursive: true });
        this.fd = fs.openSync(this.file, "w");
        this.write('<?xml version="1.0" encoding="UTF-8"?>\n'
            + '<e:events xmlns="https://schemas.opentest4j.org/reporting/core/0.2.0"'
            + ' xmlns:e="https://schemas.opentest4j.org/reporting/events/0.2.0"'
            + ' xmlns:git="https://schemas.opentest4j.org/reporting/git/0.2.0">\n'
            + "<infrastructure><hostName>" + text(os.hostname()) + "</hostName><userName>"
            + text(os.userInfo().username) + "</userName><operatingSystem>" + text(os.type())
            + "</operatingSystem><cpuCores>" + os.cpus().length + "</cpuCores>"
            + (process.env.OTR_GIT_DIR ? gitInfrastructure(process.env.OTR_GIT_DIR) : "") + "</infrastructure>\n");
        const entries = [];
        if (process.env.TCK_SDK) {
            entries.push('<entry key="sdk">' + text(process.env.TCK_SDK) + "</entry>");
        }
        const tckCommit = git(process.cwd(), "rev-parse", "HEAD");
        if (tckCommit) {
            entries.push('<entry key="tck-commit">' + text(tckCommit) + "</entry>");
        }
        const sdk = entries.length ? '<attachments><data time="' + now() + '">' + entries.join("")
            + "</data></attachments>" : "";
        this.write('<e:started id="' + ROOT_ID + '" name="Hiero TCK" time="' + now() + '">' + sdk + "</e:started>\n");
        this.started.add(ROOT_ID);
    }

    end() {
        if (this.fd === undefined) {
            return;
        }
        for (const id of [...this.started].reverse()) {
            if (!this.finished.has(id) && id !== ROOT_ID) {
                this.finish(id, "ABORTED", "the run ended before this element finished");
            }
        }
        this.finish(ROOT_ID, "SUCCESSFUL");
        this.write("</e:events>\n");
        fs.closeSync(this.fd);
        this.counts.tests = this.counts.passed + this.counts.failed + this.counts.skipped;
        fs.writeFileSync(this.file + ".summary.json", JSON.stringify(this.counts) + "\n");
    }

    startTest(test) {
        const id = this.id(test);
        if (this.started.has(id)) {
            return;
        }
        const titles = typeof test.titlePath === "function" ? test.titlePath() : test.$$titlePath || [test.title];
        const testId = "tck/" + relativeFile(test.file) + "/" + kebab(titles.join(" "));
        this.start(id, this.parent(test), test.title, test.file,
            "<metadata><tags><tag>" + text("testId:" + testId) + "</tag></tags></metadata>");
    }

    start(id, parentId, name, file, metadata) {
        this.started.add(id);
        const source = file ? "<sources><fileSource path=\"" + attribute(file) + "\"/></sources>" : "";
        this.write('<e:started id="' + attribute(id) + '" name="' + attribute(name || "(unnamed)") + '" parentId="'
            + attribute(parentId) + '" time="' + now() + '">' + (metadata || "") + source + "</e:started>\n");
    }

    finish(id, status, reason) {
        if (this.finished.has(id)) {
            return;
        }
        this.finished.add(id);
        const result = reason
            ? '<result status="' + status + '"><reason>' + text(reason) + "</reason></result>"
            : '<result status="' + status + '"/>';
        this.write('<e:finished id="' + attribute(id) + '" time="' + now() + '">' + result + "</e:finished>\n");
    }

    id(runnable) {
        return String(runnable.id || runnable[MOCHA_ID] || runnable.fullTitle?.() || runnable.$$fullTitle);
    }

    // a suite of the top level (child of a root suite, one root per file in parallel mode) belongs to the TCK root
    parent(runnable) {
        const parent = runnable.parent;
        if (!parent) {
            return ROOT_ID;
        }
        const id = this.id(parent);
        return this.started.has(id) ? id : ROOT_ID;
    }

    fileOf(suite) {
        return suite.file || (suite.tests && suite.tests[0] && suite.tests[0].file) || undefined;
    }

    write(chunk) {
        fs.writeSync(this.fd, chunk);
    }
}

// the output of a git command, undefined if it fails (no repository, no remote, git missing)
function git(directory, ...args) {
    try {
        return execFileSync("git", ["-C", directory, ...args], { encoding: "utf8", stdio: ["ignore", "pipe", "ignore"] })
            .replace(/\n$/, "");
    } catch {
        return undefined;
    }
}

// the elements of the Git extension: repository (origin without credentials), branch, commit and status
function gitInfrastructure(directory) {
    const commit = git(directory, "rev-parse", "HEAD");
    if (!commit) {
        return "";
    }
    let xml = "";
    const origin = git(directory, "remote", "get-url", "origin");
    if (origin) {
        xml += '<git:repository originUrl="' + attribute(origin.replace(/\/\/[^/@]*@/, "//")) + '"/>';
    }
    const branch = git(directory, "rev-parse", "--abbrev-ref", "HEAD");
    if (branch && branch !== "HEAD") {
        xml += "<git:branch>" + text(branch) + "</git:branch>";
    }
    xml += "<git:commit>" + text(commit) + "</git:commit>";
    const status = git(directory, "status", "--porcelain");
    if (status !== undefined) {
        xml += '<git:status clean="' + (status.length === 0) + '">' + text(status) + "</git:status>";
    }
    return xml;
}

function now() {
    return new Date().toISOString();
}

function message(error) {
    if (!error) {
        return "failed";
    }
    return (error.message || String(error)) + (error.stack ? "\n" + error.stack : "");
}

function relativeFile(file) {
    if (!file) {
        return "unknown";
    }
    const marker = path.join("src", "tests") + path.sep;
    const index = file.lastIndexOf(marker);
    const relative = index >= 0 ? file.substring(index + marker.length) : path.basename(file);
    return relative.replace(/\.[cm]?[jt]s$/, "").split(path.sep).join("/");
}

function kebab(value) {
    return value.replace(/([a-z0-9])([A-Z])/g, "$1-$2").toLowerCase().replace(/[^a-z0-9]+/g, "-")
        .replace(/^-|-$/g, "");
}

// XML 1.0 allows only some control characters
function clean(value) {
    return String(value).replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F￾￿]/g, "");
}

function text(value) {
    return clean(value).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function attribute(value) {
    return text(value).replace(/"/g, "&quot;").replace(/\n/g, "&#10;");
}

module.exports = OpenTestReportingReporter;
