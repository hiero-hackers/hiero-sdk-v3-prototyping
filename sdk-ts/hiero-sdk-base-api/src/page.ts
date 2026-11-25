// Type-only import used by the TSDoc {@link} references below.
import type { MirrorNodeError } from "./mirror-node-error.js";

const INT32_MIN = -2_147_483_648;
const INT32_MAX = 2_147_483_647;

function requireInt32(value: number, name: string): number {
  if (!Number.isInteger(value) || value < INT32_MIN || value > INT32_MAX) {
    throw new RangeError(`${name} must be a 32-bit signed integer, but was ${String(value)}`);
  }
  return value;
}

/**
 * An immutable page of asynchronously navigable Mirror Node data.
 *
 * Implementations extend this class and provide position inspection and navigation. The page state (`data`, `size`
 * and `pageIndex`) is validated and snapshotted once by this class and cannot be reassigned afterwards. Overriding the
 * state accessors in a subclass is not supported.
 *
 * @typeParam T - the type of each value in the page
 */
export abstract class Page<T> {
  readonly #data: ReadonlyArray<T>;
  readonly #size: number;
  readonly #pageIndex: number;

  /**
   * Creates a page and stores a frozen snapshot of its data.
   *
   * @param data - the ordered page values; later changes to this array are not reflected by the page
   * @param size - the declared page size, a 32-bit signed integer
   * @param pageIndex - the zero-based page index, a 32-bit signed integer
   * @throws TypeError if `data` is not an array
   * @throws RangeError if `size` or `pageIndex` is not a 32-bit signed integer
   */
  protected constructor(data: ReadonlyArray<T>, size: number, pageIndex: number) {
    if (!Array.isArray(data)) {
      throw new TypeError("data must be an array");
    }
    this.#data = Object.freeze([...data]);
    this.#size = requireInt32(size, "size");
    this.#pageIndex = requireInt32(pageIndex, "pageIndex");
  }

  /**
   * The immutable, ordered data snapshot of this page.
   */
  get data(): ReadonlyArray<T> {
    return this.#data;
  }

  /**
   * The declared page size.
   */
  get size(): number {
    return this.#size;
  }

  /**
   * The zero-based page index.
   */
  get pageIndex(): number {
    return this.#pageIndex;
  }

  /**
   * Reports whether another page is available, without retrieving it.
   *
   * @returns `true` when another page is available
   */
  abstract hasNext(): boolean;

  /**
   * Reports whether this is the first page, without retrieving any page.
   *
   * @returns `true` when this is the first page
   */
  abstract isFirst(): boolean;

  /**
   * Retrieves the next page.
   *
   * @returns a promise that resolves with the next page, or rejects with a {@link MirrorNodeError} when the Mirror
   *   Node request cannot produce it
   */
  abstract next(): Promise<Page<T>>;

  /**
   * Retrieves the first page associated with the same paginated operation.
   *
   * @returns a promise that resolves with the first page, or rejects with a {@link MirrorNodeError} when the Mirror
   *   Node request cannot produce it
   */
  abstract first(): Promise<Page<T>>;
}
