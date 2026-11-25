// Compiled against the built package through its published name and `exports` entry, as an external consumer would.
import { NativeToken, NativeTokenUnit } from "@hiero-ledger/sdk-base-api";

export class CoinUnit extends NativeTokenUnit {
  static readonly SATOSHI: CoinUnit = new CoinUnit("sat", 1n);
  static readonly COIN: CoinUnit = new CoinUnit("COIN", 100_000_000n);

  private readonly brand = "coin";

  private constructor(symbol: string, baseUnitFactor: bigint) {
    super(symbol, baseUnitFactor);
  }
}

export class Coin extends NativeToken<Coin, CoinUnit> {
  static of(amount: bigint, unit: CoinUnit = CoinUnit.COIN): Coin {
    return new Coin(amount, unit);
  }

  private constructor(amount: bigint, unit: CoinUnit) {
    super(amount, unit);
  }

  protected override create(amount: bigint, unit: CoinUnit): Coin {
    return new Coin(amount, unit);
  }
}

export function inSatoshi(coin: Coin): bigint {
  const converted: Coin = coin.to(CoinUnit.SATOSHI);
  return converted.amount === coin.toBaseUnits() ? converted.amount : -1n;
}

export function describeAmount(token: NativeToken<Coin, CoinUnit>): string {
  return `${token.amount.toString()} ${token.unit.symbol}`;
}

class OtherUnit extends NativeTokenUnit {
  private readonly brand = "other";
  constructor() {
    super("o", 1n);
  }
}

// @ts-expect-error a unit of another token is rejected
Coin.of(1n).to(new OtherUnit());
// @ts-expect-error the conversion hook is not part of the public call surface
Coin.of(1n).create(1n, CoinUnit.COIN);

// @ts-expect-error a token subclass must implement the create hook
class Incomplete extends NativeToken<Incomplete, CoinUnit> {}
void Incomplete;
