export interface LocationSummaryDTO {
  locationId: number;
  locationName: string;
  period: string;
  startDate: string; // ISO date
  endDate: string; // ISO date
  totalEvents: number;
  totalReviews: number;
  averageRating: number;
  totalVisitors: number;
}

export interface EventCountsDTO {
  totalEvents: number;
  regularEvents: number;
  nonRegularEvents: number;
  freeEvents: number;
  paidEvents: number;
}

export interface EventRatingDTO {
  eventId: number;
  eventName: string;
  averageRating: number;
  reviewCount: number;
}

export interface LocationRatingDTO {
  locationId: number;
  locationName: string;
  averageRating: number;
  reviewCount: number;
}

export interface TopRatingsDTO {
  topEvents: EventRatingDTO[];
  locationRating: LocationRatingDTO;
}
