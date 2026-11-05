const P3 = 26 * 26 * 26;
const P5 = 26 * 26 * 26 * 26 * 26;
const WEIGHT = 31;
const PERMUTATION = 1_000_003;
const DOT_DIGIT = 10;
const LEDGER_ID_PADDING = 6;
const LETTER_A = "a".charCodeAt(0);

/**
 * Computes the HIP-15 checksum of a no-checksum address string for a ledger.
 *
 * Every intermediate value stays below 2^53, so `number` arithmetic is exact.
 *
 * @internal
 * @param ledgerId - the ledger id bytes of the network
 * @param address - a canonical no-checksum address string such as `"0.0.123"`
 * @returns the five-letter lowercase checksum
 * @see https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-15.md
 */
export function hip15Checksum(ledgerId: Uint8Array, address: string): string {
  const digits = Array.from(address, (char) => (char === "." ? DOT_DIGIT : Number(char)));

  let sd0 = 0;
  let sd1 = 0;
  let sd = 0;
  digits.forEach((digit, index) => {
    if (index % 2 === 0) {
      sd0 = (sd0 + digit) % 11;
    } else {
      sd1 = (sd1 + digit) % 11;
    }
    sd = (sd * WEIGHT + digit) % P3;
  });

  let sh = 0;
  for (const byte of [...ledgerId, ...new Array<number>(LEDGER_ID_PADDING).fill(0)]) {
    sh = (sh * WEIGHT + byte) % P5;
  }

  const c = ((((digits.length % 5) * 11 + sd0) * 11 + sd1) * P3 + sd + sh) % P5;
  let cp = (c * PERMUTATION) % P5;

  let checksum = "";
  for (let i = 0; i < 5; i++) {
    checksum = String.fromCharCode(LETTER_A + (cp % 26)) + checksum;
    cp = Math.floor(cp / 26);
  }
  return checksum;
}
