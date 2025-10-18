export enum RequestStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
}

export interface AccountRequest {
  id: number;
  email: string;
  name: string;
  phoneNumber?: string;
  birthday?: string;
  address: string;
  city?: string;
  status: RequestStatus;
  createdAt: string;
  rejectionReason?: string;
}

export interface ProcessAccountRequest {
  requestId: number;
  approved: boolean;
  rejectionReason?: string;
}
