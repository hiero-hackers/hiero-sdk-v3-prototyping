import { describe, expectTypeOf, it } from "vitest";
import { MirrorNodeError, Page } from "../src/index.js";
import { TestPage } from "./test-page.js";

describe("Page public types", () => {
  const page: Page<string> = new TestPage(["value"], 1, 0);

  it("should type state as read-only", () => {
    expectTypeOf(page.data).toEqualTypeOf<ReadonlyArray<string>>();
    expectTypeOf(page.size).toEqualTypeOf<number>();
    expectTypeOf(page.pageIndex).toEqualTypeOf<number>();

    // @ts-expect-error data is a ReadonlyArray
    page.data.push("other");
    // @ts-expect-error data elements are read-only
    page.data[0] = "other";
    // @ts-expect-error size has no setter
    page.size = 2;
    // @ts-expect-error pageIndex has no setter
    page.pageIndex = 2;
    // @ts-expect-error data has no setter
    page.data = [];
  });

  it("should type position inspection as synchronous booleans", () => {
    expectTypeOf(page.hasNext).toEqualTypeOf<() => boolean>();
    expectTypeOf(page.isFirst).toEqualTypeOf<() => boolean>();
  });

  it("should type navigation as Promise<Page<T>> with the same T", () => {
    expectTypeOf(page.next).returns.toEqualTypeOf<Promise<Page<string>>>();
    expectTypeOf(page.first).returns.toEqualTypeOf<Promise<Page<string>>>();
    expectTypeOf(page.next).returns.resolves.not.toEqualTypeOf<Page<number>>();
  });

  it("should prevent direct construction", () => {
    // @ts-expect-error Page is abstract and its constructor is protected
    new Page(["value"], 1, 0);
  });

  it("should type MirrorNodeError as an Error with constructor options", () => {
    expectTypeOf<MirrorNodeError>().toExtend<Error>();
    expectTypeOf(MirrorNodeError).toBeConstructibleWith("request failed");
    expectTypeOf(MirrorNodeError).toBeConstructibleWith("request failed", { cause: new Error("cause") });
    expectTypeOf<MirrorNodeError["name"]>().toEqualTypeOf<string>();
  });
});
