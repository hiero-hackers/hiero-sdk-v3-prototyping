import type { Page } from '../common.js';
import { MirrorNodeError } from '../common.js';

export type PageFetcher<T> = (pageIndex: number) => Promise<Page<T>>;

export class PageImpl<T> implements Page<T> {
    public readonly size: number;

    constructor(
        public readonly data: ReadonlyArray<T>,
        public readonly pageIndex: number,
        private readonly _hasNext: boolean,
        private readonly fetcher: PageFetcher<T>
    ) {
        this.size = data.length;
        Object.freeze(this);
    }

    hasNext(): boolean {
        return this._hasNext;
    }

    isFirst(): boolean {
        return this.pageIndex === 0;
    }

    async next(): Promise<Page<T>> {
        if (!this.hasNext()) {
            throw new MirrorNodeError('No next page available');
        }
        return this.fetcher(this.pageIndex + 1);
    }

    async first(): Promise<Page<T>> {
        return this.fetcher(0);
    }
}
