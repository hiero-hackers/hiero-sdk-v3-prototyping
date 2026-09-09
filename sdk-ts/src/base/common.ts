/**
 * Error thrown when a mirror node operation fails.
 */
export class MirrorNodeError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'MirrorNodeError';
    }
}

/**
 * A paginated collection of items.
 */
export interface Page<T> {
    /** The list of items in the current page */
    readonly data: ReadonlyArray<T>;
    /** The number of items in the current page */
    readonly size: number;
    /** The 0-based index of the current page */
    readonly pageIndex: number;

    /**
     * Check if there is a next page.
     * @returns True if a subsequent page exists.
     */
    hasNext(): boolean;

    /**
     * Check if this is the first page.
     * @returns True if this is the first page (pageIndex == 0).
     */
    isFirst(): boolean;

    /**
     * Fetch the next page of results.
     * @returns A promise resolving to the next Page of items.
     * @throws {MirrorNodeError} If the fetch fails.
     */
    next(): Promise<Page<T>>;

    /**
     * Fetch the first page of results.
     * @returns A promise resolving to the first Page of items.
     * @throws {MirrorNodeError} If the fetch fails.
     */
    first(): Promise<Page<T>>;
}
