import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, Location } from '../models/event.model';

@Injectable({
  providedIn: 'root',
})
export class EventService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getTodayEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.API_URL}/events/today`);
  }

  getPopularLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.API_URL}/locations/popular`);
  }
}
