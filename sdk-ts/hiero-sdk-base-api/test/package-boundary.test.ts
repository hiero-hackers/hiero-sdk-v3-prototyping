import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import * as api from "../src/index.js";

interface PackageJson {
  readonly dependencies?: Record<string, string>;
  readonly peerDependencies?: Record<string, string>;
  readonly optionalDependencies?: Record<string, string>;
  readonly exports: Record<string, unknown>;
  readonly files: ReadonlyArray<string>;
}

const pkg = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8")) as PackageJson;

describe("Package boundary", () => {
  it("should have no runtime, peer or optional dependencies", () => {
    expect(pkg.dependencies ?? {}).toEqual({});
    expect(pkg.peerDependencies ?? {}).toEqual({});
    expect(pkg.optionalDependencies ?? {}).toEqual({});
  });

  it("should publish a single typed entry point from dist only", () => {
    expect(Object.keys(pkg.exports)).toEqual(["."]);
    expect(pkg.exports["."]).toEqual({ types: "./dist/index.d.ts", default: "./dist/index.js" });
    expect(pkg.files).toEqual(["dist"]);
  });

  it("should export exactly the public contracts", () => {
    expect(Object.keys(api).sort()).toEqual([
      "Address",
      "BaseAddress",
      "IllegalFormatError",
      "MirrorNodeError",
      "NativeToken",
      "NativeTokenUnit",
      "Network",
      "Page",
      "ZERO_ADDRESS",
    ]);
  });
});
