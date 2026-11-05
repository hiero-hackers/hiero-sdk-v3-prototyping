import { describe, expect, it, vi } from "vitest";
import { MirrorNodeError, Page } from "../src/index.js";
import { TestPage } from "./test-page.js";

describe("Page state", () => {
  it.each([null, undefined, "not-an-array", { length: 0 }])("should reject non-array data %#", (data) => {
    expect(() => new TestPage(data as unknown as ReadonlyArray<string>, 0, 0)).toThrow(TypeError);
  });

  it.each([
    ["size", 1.5, 0],
    ["size", Number.NaN, 0],
    ["size", 2 ** 31, 0],
    ["pageIndex", 0, -(2 ** 31) - 1],
    ["pageIndex", 0, Number.POSITIVE_INFINITY],
  ])("should reject a %s outside the int32 range", (_name, size, pageIndex) => {
    expect(() => new TestPage([], size, pageIndex)).toThrow(RangeError);
  });

  it("should accept int32 boundary values", () => {
    const page = new TestPage([], 2 ** 31 - 1, -(2 ** 31));
    expect(page.size).toBe(2 ** 31 - 1);
    expect(page.pageIndex).toBe(-(2 ** 31));
  });

  it("should store a snapshot that ignores later changes to the source array", () => {
    const source = ["first", "second"];
    const page = new TestPage(source, 2, 0);

    source.push("third");
    source[0] = "changed";

    expect(page.data).toEqual(["first", "second"]);
    expect(page.data).not.toBe(source);
  });

  it("should expose frozen data that cannot be mutated at runtime", () => {
    const page = new TestPage(["value"], 1, 0);

    expect(Object.isFrozen(page.data)).toBe(true);
    expect(() => (page.data as string[]).push("other")).toThrow(TypeError);
    expect(() => {
      (page.data as string[])[0] = "other";
    }).toThrow(TypeError);
    expect(page.data).toEqual(["value"]);
  });

  it("should not allow reassigning state accessors", () => {
    const page = new TestPage(["value"], 1, 0);

    expect(() => {
      (page as { size: number }).size = 9;
    }).toThrow(TypeError);
    expect(() => {
      (page as { pageIndex: number }).pageIndex = 9;
    }).toThrow(TypeError);
    expect(() => {
      (page as { data: unknown }).data = [];
    }).toThrow(TypeError);
    expect(page.size).toBe(1);
    expect(page.pageIndex).toBe(0);
  });

  it("should preserve element order and allow element types that include null", () => {
    const page = new TestPage<string | null>(["a", null, "c"], 3, 0);
    expect(page.data).toEqual(["a", null, "c"]);
  });

  it("should expose the declared state and position", () => {
    const page = new TestPage(["value"], 7, 3, { hasNext: true, isFirst: false });

    expect(page.size).toBe(7);
    expect(page.pageIndex).toBe(3);
    expect(page.hasNext()).toBe(true);
    expect(page.isFirst()).toBe(false);
    expect(page).toBeInstanceOf(Page);
  });
});

describe("Page position inspection", () => {
  it("should not initiate page retrieval", () => {
    const next = vi.fn<() => Promise<Page<string>>>();
    const first = vi.fn<() => Promise<Page<string>>>();
    const page = new TestPage(["value"], 1, 0, { hasNext: true, isFirst: true, next, first });

    page.hasNext();
    page.isFirst();

    expect(next).not.toHaveBeenCalled();
    expect(first).not.toHaveBeenCalled();
  });
});

describe("Page navigation", () => {
  it("should resolve next() and first() with the requested pages", async () => {
    const firstPage = new TestPage(["first"], 1, 0, { hasNext: true, isFirst: true });
    const nextPage = new TestPage(["next"], 1, 2, { hasNext: false, isFirst: false });
    const current = new TestPage(["current"], 1, 1, {
      hasNext: true,
      isFirst: false,
      next: () => Promise.resolve(nextPage),
      first: () => Promise.resolve(firstPage),
    });

    await expect(current.next()).resolves.toBe(nextPage);
    await expect(current.first()).resolves.toBe(firstPage);
  });

  it("should reject next() and first() with MirrorNodeError on failure", async () => {
    const cause = new Error("transport failure");
    const failure = (): Promise<never> => Promise.reject(new MirrorNodeError("request failed", { cause }));
    const page = new TestPage<string>([], 0, 0, { next: failure, first: failure });

    for (const navigate of [() => page.next(), () => page.first()]) {
      const error: unknown = await navigate().catch((e: unknown) => e);
      expect(error).toBeInstanceOf(MirrorNodeError);
      expect(error).toBeInstanceOf(Error);
      expect((error as MirrorNodeError).cause).toBe(cause);
    }
  });
});

describe("MirrorNodeError", () => {
  it("should preserve message, name and cause", () => {
    const cause = new Error("transport failure");
    const error = new MirrorNodeError("request failed", { cause });

    expect(error.message).toBe("request failed");
    expect(error.name).toBe("MirrorNodeError");
    expect(error.cause).toBe(cause);
    expect(error.stack).toContain("MirrorNodeError");
  });

  it("should allow omitting the cause", () => {
    const error = new MirrorNodeError("request failed");
    expect(error.cause).toBeUndefined();
    expect("cause" in error).toBe(false);
  });
});
