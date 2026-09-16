import * as grpc from '@grpc/grpc-js';
import { Channel } from './Channel.js';

export class NodeChannel extends Channel {
  private _client: grpc.Client | null = null;
  private readonly address: string;

  constructor(address: string, grpcDeadline?: number) {
    super(grpcDeadline);
    this.address = address;
  }

  private async _initializeClient(): Promise<void> {
    if (this._client) return;
    this._client = new grpc.Client(this.address, grpc.credentials.createInsecure(), {
      'grpc.max_receive_message_length': -1,
      'grpc.keepalive_time_ms': 10000,
      'grpc.keepalive_timeout_ms': 5000,
      'grpc.keepalive_permit_without_calls': 1
    });
  }

  protected _createUnaryClient(serviceName: string): (method: any, requestData: Uint8Array, callback: any) => void {
    return (method: any, requestData: Uint8Array, callback: any) => {
      this._initializeClient().then(() => {
        const deadline = new Date();
        deadline.setMilliseconds(deadline.getMilliseconds() + this.grpcDeadline);

        this._client!.waitForReady(deadline, (err) => {
          if (err) {
            callback(new Error('timeout'));
            return;
          }
          
          this._client!.makeUnaryRequest(
            `/proto.${serviceName}/${method.name}`,
            (arg: any) => arg,
            (arg: any) => arg,
            Buffer.from(requestData),
            new grpc.Metadata(),
            { deadline },
            (error, response) => {
              if (error) {
                callback(error);
              } else {
                callback(null, new Uint8Array(response as Buffer));
              }
            }
          );
        });
      }).catch((err) => callback(err));
    };
  }

  close(): void {
    if (this._client) {
      this._client.close();
    }
  }
}
