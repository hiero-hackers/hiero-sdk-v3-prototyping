// Compiled against the built package through its published name and `exports` entry, as an external consumer would.
import { Network, NativeTokenUnit } from "@hiero-ledger/sdk-base-api";

class GasUnit extends NativeTokenUnit {
  static readonly WEI: GasUnit = new GasUnit();

  private readonly brand = "gas";

  private constructor() {
    super("wei", 1n);
  }
}

export const devnet: Network<GasUnit> = new Network(Uint8Array.of(0x2a), "devnet", GasUnit.WEI);
export const unnamed: Network<GasUnit> = new Network(new Uint8Array(0), null, GasUnit.WEI);

export function label(network: Network<NativeTokenUnit>): string {
  const hex = Array.from(network.id, (byte) => byte.toString(16).padStart(2, "0")).join("");
  return `${network.name ?? "unnamed"} (0x${hex}, ${network.nativeTokenUnit.symbol})`;
}

export const devnetLabel: string = label(devnet);
export const unitOfDevnet: GasUnit = devnet.nativeTokenUnit;

// @ts-expect-error the network state is read-only
devnet.name = "other";
