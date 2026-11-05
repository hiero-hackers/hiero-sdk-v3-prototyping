import { describe, expectTypeOf, it } from "vitest";
import { Address, BaseAddress, Network, NativeTokenUnit, ZERO_ADDRESS } from "../src/index.js";
import { OtherUnit, TestUnit } from "./test-token.js";

describe("Address public types", () => {
  const address = Address.fromString("0.0.123");
  const base: BaseAddress = address;

  it("should narrow num on Address only", () => {
    expectTypeOf(address.num).toEqualTypeOf<bigint>();
    expectTypeOf(base.num).toEqualTypeOf<bigint | null>();
    expectTypeOf<Address>().toExtend<BaseAddress>();
  });

  it("should type its state and methods", () => {
    expectTypeOf(address.shard).toEqualTypeOf<bigint>();
    expectTypeOf(address.realm).toEqualTypeOf<bigint>();
    expectTypeOf(address.checksum).toEqualTypeOf<string>();
    expectTypeOf(address.toString).returns.toEqualTypeOf<string>();
    expectTypeOf(address.toStringWithChecksum).returns.toEqualTypeOf<string>();
    expectTypeOf(Address.of).returns.toEqualTypeOf<Address>();
    expectTypeOf(Address.fromString).returns.toEqualTypeOf<Address>();
    expectTypeOf(ZERO_ADDRESS).toEqualTypeOf<Address>();
  });

  it("should accept a network of any unit for checksum validation", () => {
    expectTypeOf(address.validateChecksum).parameter(0).toEqualTypeOf<Network<NativeTokenUnit>>();
    address.validateChecksum(new Network(Uint8Array.of(0), null, TestUnit.BASE));
    address.validateChecksum(new Network(Uint8Array.of(0), null, OtherUnit.BASE));
  });

  it("should keep state read-only and construction controlled", () => {
    // @ts-expect-error num has no setter
    address.num = 1n;
    // @ts-expect-error shard has no setter
    address.shard = 1n;
    // @ts-expect-error checksum has no setter
    address.checksum = "";
    // @ts-expect-error the Address constructor is private
    new Address(0n, 0n, 0n, "");
    // @ts-expect-error BaseAddress is abstract
    new BaseAddress(0n, 0n, "");
    // @ts-expect-error numbers must be bigint
    Address.of(0, 0, 1);
  });
});
