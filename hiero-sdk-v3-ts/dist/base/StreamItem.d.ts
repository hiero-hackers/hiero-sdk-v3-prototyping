export type StreamItem<T, E = Error> = {
    ok: true;
    value: T;
} | {
    ok: false;
    error: E;
};
export declare function isStreamItem(item: any): item is StreamItem<unknown, unknown>;
