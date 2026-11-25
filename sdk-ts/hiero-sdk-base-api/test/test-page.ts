import { Page } from "../src/index.js";

export interface TestPageOptions<T> {
  readonly hasNext?: boolean;
  readonly isFirst?: boolean;
  readonly next?: () => Promise<Page<T>>;
  readonly first?: () => Promise<Page<T>>;
}

const unconfigured = (): Promise<never> => Promise.reject(new Error("navigation not configured"));

/** A minimal consumer-style subclass used to exercise the abstract contract. */
export class TestPage<T> extends Page<T> {
  readonly #options: TestPageOptions<T>;

  constructor(data: ReadonlyArray<T>, size: number, pageIndex: number, options: TestPageOptions<T> = {}) {
    super(data, size, pageIndex);
    this.#options = options;
  }

  override hasNext(): boolean {
    return this.#options.hasNext ?? false;
  }

  override isFirst(): boolean {
    return this.#options.isFirst ?? true;
  }

  override next(): Promise<Page<T>> {
    return (this.#options.next ?? unconfigured)();
  }

  override first(): Promise<Page<T>> {
    return (this.#options.first ?? unconfigured)();
  }
}
