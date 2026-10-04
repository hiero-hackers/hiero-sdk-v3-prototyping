/**
 * The hand-written runtime of the TypeScript TCK server. It implements the contract generated from the converter
 * catalogue (`@hiero/tck-contract`); the generated server imports this module by its package name and calls
 * {@link createRuntime}.
 *
 * @packageDocumentation
 */
import type { TckRuntime } from "@hiero/tck-contract";
import { TsTckRuntime } from "./TsTckRuntime.js";

export { SOLO } from "./Utilities.js";

/** Creates the runtime. */
export function createRuntime(): TckRuntime {
    return new TsTckRuntime();
}
