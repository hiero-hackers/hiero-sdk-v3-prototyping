## Purpose

Define the language-neutral contract for an immutable native-token exchange rate and its expiration state. SDK-specific
OpenSpec changes use this document as their behavioral starting point and add only language mapping requirements.

## ADDED Requirements

### Requirement: Immutable exchange-rate state

The SDK SHALL expose an `ExchangeRate` abstraction containing an immutable `expirationTime` and an immutable
`exchangeRateInUsdCents`. `expirationTime` SHALL be a time-zone-aware date and time, and
`exchangeRateInUsdCents` SHALL be a double-precision floating-point value expressed in USD cents.

#### Scenario: Exchange-rate state is observed

- **Given** an exchange rate with an expiration time and a value expressed in USD cents.
- **When** a caller reads the exchange-rate state.
- **Then** the caller receives the same expiration time and USD-cent value without being able to modify the stored
  state.

### Requirement: Exchange-rate expiration

The SDK SHALL expose `isExpired()` to report whether the current time is later than `expirationTime`.

#### Scenario: Exchange rate has expired

- **Given** an exchange rate whose expiration time is earlier than the current time.
- **When** a caller invokes `isExpired()`.
- **Then** the operation returns `true`.

#### Scenario: Exchange rate has not expired

- **Given** an exchange rate whose expiration time is equal to or later than the current time.
- **When** a caller invokes `isExpired()`.
- **Then** the operation returns `false`.

## Language-Neutral API Schema

```text
namespace nativeToken

// Represents the exchange rate of a native token in USD cents.
abstraction ExchangeRate {
    @@immutable expirationTime: zonedDateTime
    @@immutable exchangeRateInUsdCents: double

    // Returns true if the current time is past expirationTime.
    bool isExpired()
}
```

## Questions & Comments

- [@hendrikebbers](https://github.com/hendrikebbers): Should `ExchangeRate.exchangeRateInUsdCents` really be `double`
  instead of `decimal`?
  > [@oGranny](https://github.com/oGranny): I believe, for high precision money related variables `decimal` should be
  > used instead of `double`.
