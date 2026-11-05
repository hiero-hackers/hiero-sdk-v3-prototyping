import { Buffer } from "node:buffer";
import { describe, expect, it } from "vitest";
import { Address, BaseAddress, IllegalFormatError, Network, ZERO_ADDRESS } from "../src/index.js";
import { hip15Checksum } from "../src/checksum.js";
import { TestUnit } from "./test-token.js";

const UINT64_MAX = 2n ** 64n - 1n;

/** Reference vectors from HIP-15, keyed by ledger id. */
const HIP15_VECTORS: ReadonlyArray<readonly [Uint8Array, ReadonlyArray<string>]> = [
  [
    Uint8Array.of(0x00),
    [
      "0.0.1-dfkxr",
      "0.0.4-cjcuq",
      "0.0.5-ktach",
      "0.0.6-tcxjy",
      "0.0.12-uuuup",
      "0.0.123-vfmkw",
      "0.0.1234567890-zbhlt",
      "12.345.6789-aoyyt",
      "1.23.456-adpbr",
    ],
  ],
  [
    Uint8Array.of(0xa1, 0xff, 0x01),
    [
      "0.0.1-xzlgq",
      "0.0.4-xdddp",
      "0.0.5-fnalg",
      "0.0.6-nwxsx",
      "0.0.12-povdo",
      "0.0.123-pzmtv",
      "0.0.1234567890-tvhus",
      "12.345.6789-vizhs",
      "1.23.456-uxpkq",
    ],
  ],
];

const network = (id: Uint8Array): Network<TestUnit> => new Network(id, null, TestUnit.BASE);
const LEDGER_00 = network(Uint8Array.of(0x00));
const LEDGER_A1FF01 = network(Uint8Array.of(0xa1, 0xff, 0x01));

describe("Address.of", () => {
  it("should expose its state", () => {
    const address = Address.of(1n, 2n, 3n, "abcde");

    expect(address.shard).toBe(1n);
    expect(address.realm).toBe(2n);
    expect(address.num).toBe(3n);
    expect(address.checksum).toBe("abcde");
    expect(address).toBeInstanceOf(BaseAddress);
  });

  it("should default to an empty checksum", () => {
    expect(Address.of(0n, 0n, 1n).checksum).toBe("");
  });

  it("should accept the uint64 boundaries", () => {
    const address = Address.of(UINT64_MAX, UINT64_MAX, UINT64_MAX);
    expect([address.shard, address.realm, address.num]).toEqual([UINT64_MAX, UINT64_MAX, UINT64_MAX]);
  });

  it.each([
    [-1n, 0n, 0n],
    [0n, -1n, 0n],
    [0n, 0n, -1n],
    [UINT64_MAX + 1n, 0n, 0n],
    [0n, UINT64_MAX + 1n, 0n],
    [0n, 0n, UINT64_MAX + 1n],
  ])("should reject %s.%s.%s as out of range", (shard, realm, num) => {
    expect(() => Address.of(shard, realm, num)).toThrow(RangeError);
  });

  it.each([
    [1, 0n, 0n, ""],
    [0n, "0", 0n, ""],
    [0n, 0n, null, ""],
    [0n, 0n, 0n, null],
    [0n, 0n, 0n, 12345],
  ])("should reject non-bigint numbers and non-string checksums %#", (shard, realm, num, checksum) => {
    expect(() =>
      Address.of(shard as bigint, realm as bigint, num as bigint, checksum as unknown as string),
    ).toThrow(TypeError);
  });

  it.each(["VFMKW", "vfmk", "vfmkwx", "vfm-k", " vfmkw", "vfmk1"])("should reject the checksum %j", (checksum) => {
    expect(() => Address.of(0n, 0n, 123n, checksum)).toThrow(IllegalFormatError);
  });

  it("should not allow reassigning its state", () => {
    const address = Address.of(1n, 2n, 3n, "abcde");

    for (const key of ["shard", "realm", "num", "checksum"] as const) {
      expect(() => {
        (address as unknown as Record<string, unknown>)[key] = 9n;
      }).toThrow(TypeError);
    }
    expect(address.toStringWithChecksum()).toBe("1.2.3-abcde");
  });
});

describe("Address.fromString", () => {
  it.each([
    ["0.0.123", 0n, 0n, 123n, ""],
    ["0.0.123-vfmkw", 0n, 0n, 123n, "vfmkw"],
    ["12.345.6789-aoyyt", 12n, 345n, 6789n, "aoyyt"],
    ["0.0.0", 0n, 0n, 0n, ""],
    [`${UINT64_MAX}.${UINT64_MAX}.${UINT64_MAX}`, UINT64_MAX, UINT64_MAX, UINT64_MAX, ""],
  ])("should parse %j", (text, shard, realm, num, checksum) => {
    const address = Address.fromString(text);

    expect(address.shard).toBe(shard);
    expect(address.realm).toBe(realm);
    expect(address.num).toBe(num);
    expect(address.checksum).toBe(checksum);
  });

  it.each([
    // Rejected inputs listed in HIP-15.
    "0.00.123",
    "0.0.0123-vfmkw",
    "0.0.123-VFMKW",
    "0.0.123-vFmKw",
    "0.0.123#vfmkw",
    "0.0.123vfmkw",
    "0.0.123 - vfmkw",
    "0.123",
    "0.0.123.",
    "0.0.123-vf",
    "0.0.123-vfm-kw",
    "0.0.123-vfmkwxxxx",
    // Further non-canonical or invalid input.
    "",
    " 0.0.123",
    "0.0.123 ",
    "-1.0.0",
    "+1.0.0",
    "0.0.1e3",
    "0x1.0.0",
    "0.0.１２３",
    "0.0.123-",
    `0.0.${UINT64_MAX + 1n}`,
    `${UINT64_MAX + 1n}.0.0`,
  ])("should reject %j", (text) => {
    expect(() => Address.fromString(text)).toThrow(IllegalFormatError);
  });

  it("should parse an address whose checksum is wrong for every network, without validating it", () => {
    // HIP-15 lists 0.0.123-abcde as invalid only because the checksum does not match; the format is valid.
    const address = Address.fromString("0.0.123-abcde");
    expect(address.validateChecksum(LEDGER_00)).toBe(false);
  });

  it.each([null, undefined, 123, {}])("should reject the non-string %#", (value) => {
    expect(() => Address.fromString(value as unknown as string)).toThrow(TypeError);
  });

  it.each(["0.0.123", "0.0.123-vfmkw", "12.345.6789-aoyyt", `${UINT64_MAX}.0.1`])("should round-trip %j", (text) => {
    expect(Address.fromString(text).toStringWithChecksum()).toBe(text);
  });
});

describe("Address formatting", () => {
  it("should format with and without the checksum", () => {
    const address = Address.of(1n, 23n, 456n, "adpbr");

    expect(address.toString()).toBe("1.23.456");
    expect(String(address)).toBe("1.23.456");
    expect(`${address}`).toBe("1.23.456");
    expect(address.toStringWithChecksum()).toBe("1.23.456-adpbr");
    expect(Address.of(1n, 23n, 456n).toStringWithChecksum()).toBe("1.23.456");
  });
});

describe("HIP-15 checksum", () => {
  for (const [ledgerId, vectors] of HIP15_VECTORS) {
    describe(`ledger id 0x${Buffer.from(ledgerId).toString("hex")}`, () => {
      it.each(vectors)("should compute and validate %s", (vector) => {
        const [plain = "", expected = ""] = vector.split("-");
        const address = Address.fromString(vector);

        expect(hip15Checksum(ledgerId, plain)).toBe(expected);
        expect(address.validateChecksum(network(ledgerId))).toBe(true);
      });
    });
  }

  it("should not validate a checksum against another ledger", () => {
    expect(Address.fromString("0.0.123-vfmkw").validateChecksum(LEDGER_A1FF01)).toBe(false);
    expect(Address.fromString("0.0.123-pzmtv").validateChecksum(LEDGER_00)).toBe(false);
  });

  it("should return false for an address without a checksum", () => {
    expect(Address.fromString("0.0.123").validateChecksum(LEDGER_00)).toBe(false);
    expect(ZERO_ADDRESS.validateChecksum(LEDGER_00)).toBe(false);
  });

  it("should be independent of the network name and unit", () => {
    const named = new Network(Uint8Array.of(0x00), "mainnet", TestUnit.WHOLE);
    expect(Address.fromString("0.0.123-vfmkw").validateChecksum(named)).toBe(true);
  });

  it.each([null, undefined, { id: Uint8Array.of(0) }])("should reject a non-network %#", (value) => {
    expect(() =>
      Address.fromString("0.0.123-vfmkw").validateChecksum(value as unknown as Network<TestUnit>),
    ).toThrow(TypeError);
  });

  it("should produce five lowercase letters for large addresses", () => {
    expect(hip15Checksum(Uint8Array.of(0xff, 0xff, 0xff, 0xff), `${UINT64_MAX}.${UINT64_MAX}.${UINT64_MAX}`)).toMatch(
      /^[a-z]{5}$/,
    );
  });
});

describe("ZERO_ADDRESS", () => {
  it("should be 0.0.0 with an empty checksum", () => {
    expect(ZERO_ADDRESS).toBeInstanceOf(Address);
    expect([ZERO_ADDRESS.shard, ZERO_ADDRESS.realm, ZERO_ADDRESS.num]).toEqual([0n, 0n, 0n]);
    expect(ZERO_ADDRESS.checksum).toBe("");
    expect(ZERO_ADDRESS.toString()).toBe("0.0.0");
    expect(ZERO_ADDRESS.toStringWithChecksum()).toBe("0.0.0");
  });
});

describe("IllegalFormatError", () => {
  it("should preserve message, name and cause", () => {
    const cause = new Error("root");
    const error = new IllegalFormatError("bad format", { cause });

    expect(error).toBeInstanceOf(Error);
    expect(error.name).toBe("IllegalFormatError");
    expect(error.message).toBe("bad format");
    expect(error.cause).toBe(cause);
  });
});
