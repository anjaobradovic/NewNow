import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocationDTO } from '../models/location.model';
import { EventBasicDTO, ReviewDetailsDTO } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly API_URL = '/api/feed';

  constructor(private http: HttpClient) {}

  getTodayEvents(): Observable<EventBasicDTO[]> {
    return this.http.get<EventBasicDTO[]>(`${this.API_URL}/today-events`);
  }

  getPopularLocations(limit = 10): Observable<LocationDTO[]> {
    return this.http.get<LocationDTO[]>(`${this.API_URL}/popular-locations?limit=${limit}`);
  }

  getPopularLocationLatestReviews(): Observable<ReviewDetailsDTO[]> {
    return this.http.get<ReviewDetailsDTO[]>(`${this.API_URL}/popular-location-latest-reviews`);
  }
}
