import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, Location } from '../models/event.model';
import { PageResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  // Use relative API URL to work with Angular dev proxy and Docker nginx
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  // Today events (EventDTO list)
  getTodayEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.API_URL}/events/today`);
  }

  // Popular locations (kept for home usage)
  getPopularLocations(): Observable<Location[]> {
    // Backend supports /api/locations/popular in tests; keep as-is
    return this.http.get<Location[]>(`${this.API_URL}/locations/popular`);
  }

  // Search events with filters and pagination
  searchEvents(filters: {
    type?: string;
    locationId?: number;
    address?: string;
    priceMin?: number;
    priceMax?: number;
    isFree?: boolean;
    isRegular?: boolean;
    date?: string; // ISO date YYYY-MM-DD
    page?: number;
    size?: number;
  }): Observable<PageResponse<Event>> {
    let params = new HttpParams();
    Object.entries(filters || {}).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params = params.set(k, String(v));
      }
    });
    if (!params.has('page')) params = params.set('page', '0');
    if (!params.has('size')) params = params.set('size', '9');

    return this.http.get<PageResponse<Event>>(`${this.API_URL}/events`, { params });
  }

  // Event details
  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.API_URL}/events/${id}`);
  }

  // Occurrences count until date
  countOccurrences(id: number, untilDate: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.API_URL}/events/${id}/occurrences/count`, {
      params: { untilDate },
    });
  }

  // Create event (multipart) — manager of the location
  createEvent(
    locationId: number,
    payload: {
      name: string;
      address: string;
      type: string;
      date: string; // YYYY-MM-DD
      price?: number;
      recurrent?: boolean;
      image: File;
    }
  ): Observable<Event> {
    const fd = new FormData();
    fd.append('name', payload.name);
    fd.append('address', payload.address);
    fd.append('type', payload.type);
    fd.append('date', payload.date);
    fd.append('price', String(payload.price ?? 0));
    fd.append('recurrent', String(!!payload.recurrent));
    fd.append('image', payload.image);
    return this.http.post<Event>(`${this.API_URL}/locations/${locationId}/events`, fd);
  }

  // Update event details (JSON)
  updateEvent(
    id: number,
    payload: Partial<Pick<Event, 'name' | 'address' | 'type' | 'date' | 'price' | 'recurrent'>> & {
      date?: string; // YYYY-MM-DD
    }
  ): Observable<Event> {
    return this.http.put<Event>(`${this.API_URL}/events/${id}`, payload);
  }

  // Update event image (multipart)
  updateEventImage(id: number, file: File): Observable<{ message: string }> {
    const fd = new FormData();
    fd.append('image', file);
    return this.http.put<{ message: string }>(`${this.API_URL}/events/${id}/image`, fd);
  }
}
