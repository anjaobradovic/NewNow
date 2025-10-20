import { Event } from './event.model';

export interface LocationDTO {
  id: number;
  name: string;
  description: string;
  address: string;
  totalRating: number;
  type: string;
  imageUrl?: string;
}

export interface LocationDetailsDTO {
  id: number;
  name: string;
  description: string;
  address: string;
  type: string;
  createdAt?: string; // ISO date
  imageUrl?: string;
  averageRating?: number;
  totalReviews?: number;
  upcomingEvents?: Event[];
}

export interface LocationPageResponse {
  locations: LocationDTO[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
}

export interface ManagerDTO {
  userId: number;
  name: string;
  email: string;
  startDate?: string; // ISO date
  endDate?: string; // ISO date
  active: boolean;
}
