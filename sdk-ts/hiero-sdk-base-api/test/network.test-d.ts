import { describe, expectTypeOf, it } from "vitest";
import { Network, NativeTokenUnit } from "../src/index.js";
import { OtherUnit, TestUnit } from "./test-token.js";

describe("Network public types", () => {
  const network = new Network(Uint8Array.of(0), "net", TestUnit.BASE);

  it("should type its state", () => {
    expectTypeOf(network).toEqualTypeOf<Network<TestUnit>>();
    expectTypeOf(network.id).toEqualTypeOf<Uint8Array>();
    expectTypeOf(network.name).toEqualTypeOf<string | null>();
    expectTypeOf(network.nativeTokenUnit).toEqualTypeOf<TestUnit>();
  });

  it("should keep state read-only", () => {
    // @ts-expect-error id has no setter
    network.id = Uint8Array.of(1);
    // @ts-expect-error name has no setter
    network.name = null;
    // @ts-expect-error nativeTokenUnit has no setter
    network.nativeTokenUnit = TestUnit.CENT;
  });

  it("should reject invalid constructor arguments", () => {
    // @ts-expect-error name must be passed explicitly; undefined is not accepted
    new Network(Uint8Array.of(0), undefined, TestUnit.BASE);
    // @ts-expect-error id must be a Uint8Array
    new Network([0], null, TestUnit.BASE);
    // @ts-expect-error the unit must be a NativeTokenUnit
    new Network(Uint8Array.of(0), null, { symbol: "t", baseUnitFactor: 1n });
  });

  it("should be assignable to Network<NativeTokenUnit> as the wildcard network", () => {
    expectTypeOf<Network<TestUnit>>().toExtend<Network<NativeTokenUnit>>();
    expectTypeOf<Network<OtherUnit>>().toExtend<Network<NativeTokenUnit>>();
    expectTypeOf<Network<OtherUnit>>().not.toExtend<Network<TestUnit>>();
  });
});
