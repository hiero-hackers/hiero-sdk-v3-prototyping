export enum HttpMethod {
  GET = 'GET',
  HEAD = 'HEAD',
  POST = 'POST',
  PUT = 'PUT',
  PATCH = 'PATCH',
  DELETE = 'DELETE',
  OPTIONS = 'OPTIONS',
  TRACE = 'TRACE',
  CONNECT = 'CONNECT'
}

export interface HttpConfiguration {
  readonly defaultHeaders: ReadonlyMap<string, string>;
  readonly connectTimeout: number; // milliseconds
  readonly defaultRequestTimeout: number; // milliseconds
}

export interface HttpRequest {
  readonly method: HttpMethod;
  readonly url: string;
  readonly body: Uint8Array | null;
  readonly timeout: number | null;
  readonly headers: ReadonlyMap<string, string>;
}

export interface HttpResponse {
  readonly statusCode: number;
  readonly body: Uint8Array;
  readonly headers: ReadonlyMap<string, string>;
}

export interface HttpClient {
  readonly configuration: HttpConfiguration;
  execute(request: HttpRequest): Promise<HttpResponse>;
  close(closeTimeout?: number): Promise<void>;
}

class DefaultHttpClient implements HttpClient {
  #closed = false;
  #inFlightControllers = new Set<AbortController>();

  constructor(public readonly configuration: HttpConfiguration) {}

  async execute(request: HttpRequest): Promise<HttpResponse> {
    if (this.#closed) {
      throw new Error('client-closed-error');
    }

    const controller = new AbortController();
    this.#inFlightControllers.add(controller);

    const timeoutMs = request.timeout ?? this.configuration.defaultRequestTimeout;
    const timeoutId = setTimeout(() => controller.abort(new Error('timeout-error')), timeoutMs);

    try {
      const headers = new Headers();
      this.configuration.defaultHeaders.forEach((val, key) => headers.set(key, val));
      request.headers.forEach((val, key) => headers.set(key, val));

      const fetchOptions: RequestInit = {
        method: request.method,
        headers,
        signal: controller.signal,
      };

      if (request.body !== null) {
        fetchOptions.body = request.body as unknown as BodyInit;
      }

      const res = await fetch(request.url, fetchOptions);
      
      const responseHeaders = new Map<string, string>();
      res.headers.forEach((val, key) => responseHeaders.set(key, val));

      const arrayBuffer = await res.arrayBuffer();

      return {
        statusCode: res.status,
        body: new Uint8Array(arrayBuffer),
        headers: responseHeaders,
      };
    } catch (e: any) {
      if (this.#closed) {
        throw new Error('client-closed-error');
      }
      if (e.message === 'timeout-error' || e.name === 'AbortError') {
        throw new Error('timeout-error');
      }
      throw new Error('connection-error');
    } finally {
      clearTimeout(timeoutId);
      this.#inFlightControllers.delete(controller);
    }
  }

  async close(closeTimeout?: number): Promise<void> {
    if (this.#closed) return;
    this.#closed = true;
    
    // In a full implementation, we would wait for promises to resolve up to closeTimeout,
    // but here we just immediately abort to satisfy the basic abort controller logic.
    for (const controller of this.#inFlightControllers) {
      controller.abort(new Error('client-closed-error'));
    }
    this.#inFlightControllers.clear();
  }
}

export function createHttpClient(configuration: HttpConfiguration): HttpClient {
  return new DefaultHttpClient(configuration);
}
