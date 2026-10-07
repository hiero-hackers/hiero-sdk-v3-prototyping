/** The detail of the log, selected by `TCK_SERVER_LOG`. */
export type LogLevel = "off" | "error" | "debug";

const PREFIX = "[tck-server] ";

function level(value: string | undefined): LogLevel {
    if (value === undefined || value.trim() === "") {
        return "error";
    }
    const name = value.trim().toLowerCase();
    if (name === "off" || name === "error" || name === "debug") {
        return name;
    }
    console.error(`${PREFIX}unknown TCK_SERVER_LOG '${value}', using 'error'`);
    return "error";
}

/**
 * The error log of the server. The TCK test driver only shows the `message` of a JSON-RPC error ("Internal error"),
 * not its `data`, so the cause of a failed call is only visible here: every distinct failure is logged to `stderr`
 * once with its exception, and when the server stops a summary lists them with the number of calls.
 *
 * The environment variable `TCK_SERVER_LOG` selects the detail: `error` (the default) logs the first call of each
 * distinct failure, `debug` logs every call with the stack trace of its exception, and `off` logs nothing. As long as
 * the API is generated stubs, the summary lists the methods to implement next.
 */
export class ServerLog {

    readonly #level: LogLevel;
    readonly #failures = new Map<string, number>();
    #reported = false;

    constructor(logLevel: LogLevel = level(process.env["TCK_SERVER_LOG"])) {
        this.#level = logLevel;
    }

    /**
     * Records a failed call and logs it.
     *
     * @param method the JSON-RPC method, `null` if the request did not name one
     * @param code   the JSON-RPC error code of the response
     * @param detail the `data.message` of the response
     * @param cause  the exception the call failed with, `null` if the server rejected the request itself
     */
    failed(method: string | null, code: number, detail: string, cause: unknown): void {
        if (this.#level === "off") {
            return;
        }
        const key = `${method ?? "<no method>"} -> ${code} ${detail}`;
        const count = this.#failures.get(key) ?? 0;
        this.#failures.set(key, count + 1);
        if (this.#level === "debug") {
            console.error(PREFIX + key);
            if (cause instanceof Error && cause.stack !== undefined) {
                console.error(cause.stack);
            }
        } else if (count === 0) {
            console.error(`${PREFIX}${key} (further calls are only counted, see the summary)`);
        }
    }

    /**
     * Logs the summary of the distinct failures, most frequent first. Does nothing if there was none, and logs it only
     * once: both `stop()` and the signal handlers of the server ask for it.
     */
    summary(): void {
        if (this.#level === "off" || this.#failures.size === 0 || this.#reported) {
            return;
        }
        this.#reported = true;
        console.error(`${PREFIX}failed calls by cause (${this.#failures.size} distinct):`);
        [...this.#failures.entries()]
            .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
            .forEach(([key, count]) => console.error(`${PREFIX}  ${count}x ${key}`));
    }
}
