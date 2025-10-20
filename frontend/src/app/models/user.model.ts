export interface UserProfile {
  id: number;
  email: string;
  name: string;
  phoneNumber?: string;
  birthday?: string; // ISO date
  address?: string;
  city?: string;
  createdAt?: string; // ISO date
  roles: string[];
  avatarUrl?: string;
}

export interface UpdateProfileRequest {
  name?: string;
  phoneNumber?: string;
  birthday?: string; // ISO date YYYY-MM-DD
  address?: string;
  city?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface RateDTO {
  id: number;
  performance: number;
  soundAndLighting: number;
  venue: number;
  overallImpression: number;
  averageRating: number;
}

export interface ReviewDTO {
  id: number;
  createdAt: string; // ISO
  eventCount: number;
  hidden: boolean;
  locationId: number;
  locationName: string;
  eventId: number;
  eventName: string;
  rate: RateDTO;
}

export interface ManagedLocationDTO {
  id: number;
  locationName: string;
  locationAddress: string;
  locationType: string;
  startDate?: string;
  endDate?: string;
  isActive: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page index
  size: number;
  first?: boolean;
  last?: boolean;
}
