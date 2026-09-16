import { proto } from '../proto/proto.js';

export abstract class Channel {
  protected _crypto: proto.CryptoService | null = null;
  protected _smartContract: proto.SmartContractService | null = null;
  protected _file: proto.FileService | null = null;
  protected _consensus: proto.ConsensusService | null = null;
  protected _freeze: proto.FreezeService | null = null;
  protected _network: proto.NetworkService | null = null;
  protected _token: proto.TokenService | null = null;
  protected _schedule: proto.ScheduleService | null = null;
  protected _util: proto.UtilService | null = null;
  protected _addressBook: proto.AddressBookService | null = null;

  constructor(protected readonly grpcDeadline: number = 10000) {}

  protected abstract _createUnaryClient(serviceName: string): (method: any, requestData: Uint8Array, callback: any) => void;

  get crypto(): proto.CryptoService {
    if (!this._crypto) {
      this._crypto = proto.CryptoService.create(this._createUnaryClient('CryptoService'));
    }
    return this._crypto;
  }

  get smartContract(): proto.SmartContractService {
    if (!this._smartContract) {
      this._smartContract = proto.SmartContractService.create(this._createUnaryClient('SmartContractService'));
    }
    return this._smartContract;
  }

  get file(): proto.FileService {
    if (!this._file) {
      this._file = proto.FileService.create(this._createUnaryClient('FileService'));
    }
    return this._file;
  }

  get consensus(): proto.ConsensusService {
    if (!this._consensus) {
      this._consensus = proto.ConsensusService.create(this._createUnaryClient('ConsensusService'));
    }
    return this._consensus;
  }

  get freeze(): proto.FreezeService {
    if (!this._freeze) {
      this._freeze = proto.FreezeService.create(this._createUnaryClient('FreezeService'));
    }
    return this._freeze;
  }

  get network(): proto.NetworkService {
    if (!this._network) {
      this._network = proto.NetworkService.create(this._createUnaryClient('NetworkService'));
    }
    return this._network;
  }

  get token(): proto.TokenService {
    if (!this._token) {
      this._token = proto.TokenService.create(this._createUnaryClient('TokenService'));
    }
    return this._token;
  }

  get schedule(): proto.ScheduleService {
    if (!this._schedule) {
      this._schedule = proto.ScheduleService.create(this._createUnaryClient('ScheduleService'));
    }
    return this._schedule;
  }

  get util(): proto.UtilService {
    if (!this._util) {
      this._util = proto.UtilService.create(this._createUnaryClient('UtilService'));
    }
    return this._util;
  }

  get addressBook(): proto.AddressBookService {
    if (!this._addressBook) {
      this._addressBook = proto.AddressBookService.create(this._createUnaryClient('AddressBookService'));
    }
    return this._addressBook;
  }
}
