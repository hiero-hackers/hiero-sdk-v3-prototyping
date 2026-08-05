import { describe, it, expect } from 'vitest';
import type { Page } from '../src/base/common.js';
import { MirrorNodeError } from '../src/base/common.js';
import { PageImpl } from '../src/base/internal/common-impl.js';

describe('Common API', () => {
    describe('Page', () => {
        const mockFetcher = async (index: number): Promise<Page<string>> => {
            if (index === 0) {
                return new PageImpl(['A', 'B'], 0, true, mockFetcher);
            } else if (index === 1) {
                return new PageImpl(['C'], 1, false, mockFetcher);
            }
            throw new MirrorNodeError('Out of bounds');
        };

        it('should correctly expose size and data', async () => {
            const page = await mockFetcher(0);
            expect(page.size).toBe(2);
            expect(page.data).toEqual(['A', 'B']);
        });

        it('should correctly identify first page', async () => {
            const firstPage = await mockFetcher(0);
            expect(firstPage.isFirst()).toBe(true);

            const secondPage = await firstPage.next();
            expect(secondPage.isFirst()).toBe(false);
        });

        it('should fetch next page', async () => {
            const firstPage = await mockFetcher(0);
            expect(firstPage.hasNext()).toBe(true);
            
            const secondPage = await firstPage.next();
            expect(secondPage.data).toEqual(['C']);
            expect(secondPage.size).toBe(1);
            expect(secondPage.pageIndex).toBe(1);
            expect(secondPage.hasNext()).toBe(false);
        });

        it('should throw MirrorNodeError when calling next() and hasNext() is false', async () => {
            const firstPage = await mockFetcher(0);
            const secondPage = await firstPage.next();
            
            expect(secondPage.hasNext()).toBe(false);
            await expect(secondPage.next()).rejects.toThrow(MirrorNodeError);
            await expect(secondPage.next()).rejects.toThrow('No next page available');
        });

        it('should fetch first page from any page', async () => {
            const secondPage = await mockFetcher(1);
            expect(secondPage.pageIndex).toBe(1);

            const firstPage = await secondPage.first();
            expect(firstPage.pageIndex).toBe(0);
            expect(firstPage.data).toEqual(['A', 'B']);
        });
    });
});
