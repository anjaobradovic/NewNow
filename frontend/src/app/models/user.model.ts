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

// New: basic user and event details for review details
export interface UserBasicDTO {
  id: number;
  name: string;
  email: string;
}

export interface EventBasicDTO {
  id: number;
  name: string;
  type: string;
  date: string; // ISO date
  recurrent: boolean;
}

export interface RateDetailsDTO {
  performance: number | null;
  soundAndLighting: number | null;
  venue: number | null;
  overallImpression: number | null;
  average: number;
}

export interface ReviewDetailsDTO {
  id: number;
  createdAt: string; // ISO
  comment?: string;
  eventCount: number;
  hidden: boolean;
  author: UserBasicDTO;
  event: EventBasicDTO;
  ratings: RateDetailsDTO;
}

export interface CommentDTO {
  id: number;
  text: string;
  createdAt: string; // ISO
  author: UserBasicDTO;
  parentCommentId?: number;
  replies: CommentDTO[];
}

// Requests for reviews & comments
export interface CreateReviewRequest {
  eventId: number;
  performance?: number | null;
  soundAndLighting?: number | null;
  venue?: number | null;
  overallImpression?: number | null;
  comment?: string;
}

export interface UpdateReviewRequest {
  performance?: number | null;
  soundAndLighting?: number | null;
  venue?: number | null;
  overallImpression?: number | null;
  comment?: string;
}

export interface CreateCommentRequest {
  text: string;
  parentCommentId?: number;
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
