export interface Page<T> {
    readonly data: readonly T[];
    readonly size: number;
    readonly pageIndex: number;
    hasNext(): boolean;
    isFirst(): boolean;
    next(): Promise<Page<T>>;
    first(): Promise<Page<T>>;
}
