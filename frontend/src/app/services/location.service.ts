import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import {
  LocationDTO,
  LocationDetailsDTO,
  LocationPageResponse,
  ManagerDTO,
} from '../models/location.model';
import { Event } from '../models/event.model';
import { PageResponse, ReviewDTO } from '../models/user.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class LocationService {
  // Use relative API URL to work both in dev (Angular proxy) and Docker (nginx proxy)
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  // List & details
  getLocations(search = '', page = 0, size = 12): Observable<LocationPageResponse> {
    const q = search
      ? `?search=${encodeURIComponent(search)}&page=${page}&size=${size}`
      : `?page=${page}&size=${size}`;
    return this.http.get<LocationPageResponse>(`${this.API_URL}/locations${q}`);
  }

  getLocation(id: number): Observable<LocationDetailsDTO> {
    return this.http.get<LocationDetailsDTO>(`${this.API_URL}/locations/${id}`);
  }

  getUpcomingEvents(
    id: number,
    page = 0,
    size = 6,
    dateFrom?: string
  ): Observable<PageResponse<Event>> {
    let q = `?page=${page}&size=${size}`;
    if (dateFrom) q += `&dateFrom=${encodeURIComponent(dateFrom)}`;
    return this.http.get<PageResponse<Event>>(
      `${this.API_URL}/locations/${id}/events/upcoming${q}`
    );
  }

  getLocationReviews(
    id: number,
    sort: 'rating' | 'date' = 'date',
    order: 'asc' | 'desc' = 'desc',
    page = 0,
    size = 10
  ): Observable<PageResponse<ReviewDTO>> {
    return this.http.get<PageResponse<ReviewDTO>>(
      `${this.API_URL}/locations/${id}/reviews?sort=${sort}&order=${order}&page=${page}&size=${size}`
    );
  }

  // Admin: create/update
  createLocation(payload: {
    name: string;
    address: string;
    type: string;
    description?: string;
    image: File;
  }): Observable<LocationDTO> {
    const formData = new FormData();
    formData.append('name', payload.name);
    formData.append('address', payload.address);
    formData.append('type', payload.type);
    if (payload.description) formData.append('description', payload.description);
    formData.append('image', payload.image);
    return this.http.post<LocationDTO>(`${this.API_URL}/locations`, formData);
  }

  patchLocation(
    id: number,
    dto: { address?: string; type?: string; description?: string }
  ): Observable<LocationDTO> {
    return this.http.patch<LocationDTO>(`${this.API_URL}/locations/${id}`, dto);
  }

  updateLocationImage(id: number, image: File): Observable<LocationDTO> {
    const formData = new FormData();
    formData.append('image', image);
    return this.http.put<LocationDTO>(`${this.API_URL}/locations/${id}/image`, formData);
  }

  // Admin: managers
  getManagers(locationId: number): Observable<ManagerDTO[]> {
    return this.http.get<ManagerDTO[]>(`${this.API_URL}/admin/locations/${locationId}/managers`);
  }

  assignManager(locationId: number, userId: number): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.API_URL}/admin/locations/${locationId}/managers`,
      { userId }
    );
  }

  removeManager(locationId: number, userId: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(
      `${this.API_URL}/admin/locations/${locationId}/managers/${userId}`
    );
  }

  // Admin: user lookup helpers
  searchUsers(q: string): Observable<Array<{ id: number; name: string; email: string }>> {
    return this.http
      .get<{ content?: any[]; [k: string]: any }>(`${this.API_URL}/admin/users`, {
        params: { q: q || '', page: 0, size: 10 } as any,
      })
      .pipe(map((res) => (Array.isArray(res?.content) ? res.content : (res as any) || [])));
  }

  checkUserByEmail(
    email: string
  ): Observable<{ exists: boolean; email?: string; name?: string; roles?: string[] }> {
    return this.http.get<{ exists: boolean; email?: string; name?: string; roles?: string[] }>(
      `${this.API_URL}/debug/check-user/${encodeURIComponent(email)}`
    );
  }
}
