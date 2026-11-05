// Compiled against the built package through its published name and `exports` entry, as an external consumer would.
import {
  Address,
  BaseAddress,
  IllegalFormatError,
  Network,
  NativeTokenUnit,
  ZERO_ADDRESS,
} from "@hiero-ledger/sdk-base-api";

export function parseOrZero(text: string): Address {
  try {
    return Address.fromString(text);
  } catch (error: unknown) {
    if (error instanceof IllegalFormatError) {
      return ZERO_ADDRESS;
    }
    throw error;
  }
}

export function isFor(address: BaseAddress, network: Network<NativeTokenUnit>): boolean {
  return address.checksum === "" || address.validateChecksum(network);
}

export function entityNumber(address: Address): bigint {
  return address.num + 0n;
}

export function maybeNumber(address: BaseAddress): bigint | null {
  return address.num;
}

export const formatted: string = Address.of(0n, 0n, 1001n).toStringWithChecksum();

// @ts-expect-error Address is final: its constructor is private, so it cannot be extended
class SpecialAddress extends Address {}
void SpecialAddress;

// @ts-expect-error num may be null on BaseAddress
export const notNarrowed: bigint = ({} as BaseAddress).num;
