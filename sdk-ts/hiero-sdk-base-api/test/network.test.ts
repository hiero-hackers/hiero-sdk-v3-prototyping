import { Buffer } from "node:buffer";
import { describe, expect, it } from "vitest";
import { Network } from "../src/index.js";
import { OtherUnit, TestUnit } from "./test-token.js";

describe("Network state", () => {
  it("should expose its id, name and native token unit", () => {
    const network = new Network(Uint8Array.of(0x00, 0x01), "testnet", TestUnit.BASE);

    expect(network.id).toEqual(Uint8Array.of(0x00, 0x01));
    expect(network.name).toBe("testnet");
    expect(network.nativeTokenUnit).toBe(TestUnit.BASE);
  });

  it("should accept a null name, an empty name and an empty id", () => {
    expect(new Network(Uint8Array.of(3), null, TestUnit.BASE).name).toBeNull();
    expect(new Network(Uint8Array.of(3), "", TestUnit.BASE).name).toBe("");
    expect(new Network(new Uint8Array(0), null, TestUnit.BASE).id).toEqual(new Uint8Array(0));
  });

  it("should normalize a Uint8Array subclass to a plain Uint8Array", () => {
    const id = new Network(Buffer.from([1, 2]), null, TestUnit.BASE).id;

    expect(Object.getPrototypeOf(id)).toBe(Uint8Array.prototype);
    expect(id).toEqual(Uint8Array.of(1, 2));
  });
});

describe("Network validation", () => {
  it.each([null, undefined, [0, 1], new ArrayBuffer(2), new Uint16Array(2), "0001"])(
    "should reject a non-Uint8Array id %#",
    (id) => {
      expect(() => new Network(id as unknown as Uint8Array, null, TestUnit.BASE)).toThrow(TypeError);
    },
  );

  it.each([undefined, 1, {}])("should reject a name that is neither a string nor null %#", (name) => {
    expect(() => new Network(Uint8Array.of(0), name as unknown as string, TestUnit.BASE)).toThrow(TypeError);
  });

  it.each([null, undefined, { symbol: "t", baseUnitFactor: 1n }])("should reject a non-unit %#", (unit) => {
    expect(() => new Network(Uint8Array.of(0), null, unit as unknown as TestUnit)).toThrow(TypeError);
  });
});

describe("Network immutability", () => {
  it("should not reflect later changes to the constructor input", () => {
    const source = Uint8Array.of(1, 2, 3);
    const network = new Network(source, "net", TestUnit.BASE);

    source[0] = 9;

    expect(network.id).toEqual(Uint8Array.of(1, 2, 3));
  });

  it("should return a new copy from every id read", () => {
    const network = new Network(Uint8Array.of(1, 2, 3), "net", TestUnit.BASE);
    const read = network.id;

    read[0] = 9;
    read.fill(0);

    expect(network.id).toEqual(Uint8Array.of(1, 2, 3));
    expect(network.id).not.toBe(network.id);
  });

  it("should not allow reassigning its state", () => {
    const network = new Network(Uint8Array.of(1), "net", TestUnit.BASE);

    expect(() => {
      (network as { id: Uint8Array }).id = Uint8Array.of(2);
    }).toThrow(TypeError);
    expect(() => {
      (network as { name: string | null }).name = "other";
    }).toThrow(TypeError);
    expect(() => {
      (network as { nativeTokenUnit: unknown }).nativeTokenUnit = OtherUnit.BASE;
    }).toThrow(TypeError);
    expect(network.id).toEqual(Uint8Array.of(1));
    expect(network.name).toBe("net");
    expect(network.nativeTokenUnit).toBe(TestUnit.BASE);
  });
});
