# Native Token API

The Native Token API provides foundational abstractions for working with network-specific native tokens. It encapsulates standard unit conversions and real-time exchange rates, providing a strictly typed structure for handling token denominations.

## Core Abstractions

This API exposes three primary interfaces, mapping identically to the SDK specifications:

### `NativeTokenUnit`
Represents a specific denomination or unit of a native token (e.g. the base smallest unit or a named multiple like Hbars vs Tinybars).
- `symbol`: The display symbol of the unit (e.g., `"ℏ"` or `"tℏ"`).
- `baseUnitFactor`: The multiplier required to calculate the number of raw, indivisible base units contained within one of this unit.

### `NativeToken<Self, Unit>`
Represents a specific amount of a native token, strictly tied to a `NativeTokenUnit`.
- `amount`: The token amount expressed in the current `unit`.
- `unit`: The associated `NativeTokenUnit` describing the magnitude of `amount`.
- `to(targetUnit)`: Converts this token amount to a different unit, returning a new instance.
- `toBaseUnits()`: Normalizes and calculates the absolute total amount expressed solely in the smallest indivisible base units.

### `ExchangeRate`
Represents the current exchange rate of a native token in USD cents.
- `exchangeRateInUsdCents`: The exchange rate of the native token relative to USD cents, strictly represented as a `number` (double precision).
- `expirationTime`: A `Date` object defining exactly when this exchange rate ceases to be valid.
- `isExpired()`: Helper method to determine if the current system time has eclipsed the `expirationTime`.

## Extension and Usage

These interfaces are designed as foundational abstractions (`BaseNativeTokenUnit` and `BaseNativeToken`). They are strictly meant to be extended by concrete token implementations (like `Hbar`) in subsequent namespaces rather than instantiated directly by SDK end-users.
