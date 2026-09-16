import { Channel } from './Channel.js';

export function encodeRequest(data: Uint8Array): Uint8Array {
  const frame = new Uint8Array(data.length + 5);
  frame[0] = 0; // Data frame
  const view = new DataView(frame.buffer, frame.byteOffset, frame.byteLength);
  view.setUint32(1, data.length, false);
  frame.set(data, 5);
  return frame;
}

export function decodeUnaryResponse(data: Uint8Array): Uint8Array {
  let offset = 0;
  let responseData: Uint8Array | null = null;
  const view = new DataView(data.buffer, data.byteOffset, data.byteLength);

  while (offset < data.length) {
    const flags = data[offset];
    const length = view.getUint32(offset + 1, false);
    offset += 5;

    if (flags === 0x00) {
      responseData = data.subarray(offset, offset + length);
    } else if (flags === 0x80) {
      // Trailers (ignore for now)
    }
    offset += length;
  }
  
  if (!responseData) throw new Error('No grpc-web data frame found');
  return responseData;
}

export class WebChannel extends Channel {
  private readonly address: string;

  constructor(address: string, grpcDeadline?: number) {
    super(grpcDeadline);
    this.address = address;
  }

  protected _createUnaryClient(serviceName: string): (method: any, requestData: Uint8Array, callback: any) => void {
    return (method: any, requestData: Uint8Array, callback: any) => {
      const url = `https://${this.address}/proto.${serviceName}/${method.name}`;
      const frame = encodeRequest(requestData);

      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), this.grpcDeadline);

      fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/grpc-web+proto',
          'X-User-Agent': 'hiero-sdk-js/3.0.0',
          'Accept': 'application/grpc-web+proto'
        },
        body: frame as unknown as BodyInit,
        signal: controller.signal
      })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const buffer = await response.arrayBuffer();
        const payload = decodeUnaryResponse(new Uint8Array(buffer));
        callback(null, payload);
      })
      .catch((err) => {
        if (err.name === 'AbortError') {
          callback(new Error('timeout'));
        } else {
          callback(err);
        }
      })
      .finally(() => {
        clearTimeout(timeoutId);
      });
    };
  }
}
