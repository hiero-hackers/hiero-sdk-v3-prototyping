/**
 * One item of a stream that can fail per item (the mapping of the meta-language type `streamResult<T>`): either a
 * value or the error of this item. The stream itself continues after an error item.
 *
 * ```ts
 * for await (const item of topic.subscribeSafely()) {
 *     if (item.ok) {
 *         console.log(item.value);
 *     } else {
 *         console.warn(item.error);
 *     }
 * }
 * ```
 */
export type StreamItem<T> =
    | { readonly ok: true; readonly value: T }
    | { readonly ok: false; readonly error: Error };

/**
 * Creates an item with a value.
 *
 * @param value the value
 * @returns the item
 */
export function success<T>(value: T): StreamItem<T> {
    return Object.freeze({ ok: true, value });
}

/**
 * Creates an item with an error.
 *
 * @param error the error of the item
 * @returns the item
 */
export function failure<T>(error: Error): StreamItem<T> {
    return Object.freeze({ ok: false, error });
}
