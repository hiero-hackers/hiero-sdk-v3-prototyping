/**
 * Indicates that a value does not have the required format, for example an address string that cannot be parsed.
 *
 * This is the TypeScript mapping of the common `illegal-format` error.
 */
export class IllegalFormatError extends Error {
  /**
   * Creates an error with a detail message and an optional underlying cause.
   *
   * @param message - the detail message
   * @param options - standard error options; `cause` preserves the underlying failure
   */
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "IllegalFormatError";
  }
}
