// Compiled against the built package through its published name and `exports` entry, as an external consumer would.
import { MirrorNodeError, Page } from "@hiero-ledger/sdk-base-api";

interface Account {
  readonly id: string;
}

type Fetch = (pageIndex: number) => Promise<ReadonlyArray<Account>>;

class AccountPage extends Page<Account> {
  readonly #fetch: Fetch;
  readonly #last: boolean;

  constructor(data: ReadonlyArray<Account>, pageIndex: number, last: boolean, fetch: Fetch) {
    super(data, data.length, pageIndex);
    this.#fetch = fetch;
    this.#last = last;
  }

  override hasNext(): boolean {
    return !this.#last;
  }

  override isFirst(): boolean {
    return this.pageIndex === 0;
  }

  override next(): Promise<Page<Account>> {
    return this.#load(this.pageIndex + 1);
  }

  override first(): Promise<Page<Account>> {
    return this.#load(0);
  }

  async #load(pageIndex: number): Promise<Page<Account>> {
    try {
      const data = await this.#fetch(pageIndex);
      return new AccountPage(data, pageIndex, data.length === 0, this.#fetch);
    } catch (cause: unknown) {
      throw new MirrorNodeError(`Page ${String(pageIndex)} could not be retrieved`, { cause });
    }
  }
}

export async function collectIds(page: Page<Account>): Promise<string[]> {
  const ids: string[] = [];
  let current: Page<Account> = page;
  for (;;) {
    ids.push(...current.data.map((account) => account.id));
    if (!current.hasNext()) {
      return ids;
    }
    try {
      current = await current.next();
    } catch (error: unknown) {
      if (error instanceof MirrorNodeError) {
        return ids;
      }
      throw error;
    }
  }
}

export function start(fetch: Fetch): AccountPage {
  return new AccountPage([], 0, false, fetch);
}

declare const page: Page<Account>;

// @ts-expect-error page data is read-only for consumers
page.data.push({ id: "0.0.1" });
// @ts-expect-error page state cannot be reassigned
page.size = 1;
// @ts-expect-error Page is abstract and cannot be constructed directly
new Page<Account>([], 0, 0);

// @ts-expect-error a subclass must implement every abstract position and navigation method
class Incomplete extends Page<Account> {
  override hasNext(): boolean {
    return false;
  }
}
void Incomplete;
