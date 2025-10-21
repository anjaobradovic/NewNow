import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocationSummaryDTO, EventCountsDTO, TopRatingsDTO } from '../models/analytics.model';
import { ReviewDTO } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly API_URL = '/api/analytics';

  constructor(private http: HttpClient) {}

  getLocationSummary(
    locationId: number,
    period: 'weekly' | 'monthly' | 'yearly' | 'custom' = 'monthly',
    startDate?: string,
    endDate?: string
  ): Observable<LocationSummaryDTO> {
    let params = new HttpParams().set('period', period);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<LocationSummaryDTO>(`${this.API_URL}/locations/${locationId}/summary`, {
      params,
    });
  }

  getEventCounts(locationId: number): Observable<EventCountsDTO> {
    return this.http.get<EventCountsDTO>(`${this.API_URL}/locations/${locationId}/events/counts`);
  }

  getTopRatings(
    locationId: number,
    limit = 10,
    direction: 'asc' | 'desc' = 'desc'
  ): Observable<TopRatingsDTO> {
    const params = new HttpParams().set('limit', limit.toString()).set('direction', direction);
    return this.http.get<TopRatingsDTO>(`${this.API_URL}/locations/${locationId}/ratings/top`, {
      params,
    });
  }

  getLatestReviews(locationId: number): Observable<ReviewDTO[]> {
    return this.http.get<ReviewDTO[]>(`${this.API_URL}/locations/${locationId}/reviews/latest`);
  }
}
