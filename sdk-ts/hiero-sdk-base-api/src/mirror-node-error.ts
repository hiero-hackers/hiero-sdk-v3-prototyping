/**
 * Indicates that a Mirror Node request could not produce the requested page.
 *
 * This is the TypeScript mapping of the common `mirror-node-error` condition. Asynchronous page navigation rejects its
 * promise with this error.
 */
export class MirrorNodeError extends Error {
  /**
   * Creates an error with a detail message and an optional underlying cause.
   *
   * @param message - the detail message
   * @param options - standard error options; `cause` preserves the underlying failure
   */
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "MirrorNodeError";
  }
}
