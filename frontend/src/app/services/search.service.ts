import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/user.model';
import { LocationDTO } from '../models/location.model';
import { Event } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly API_URL = '/api/search';

  constructor(private http: HttpClient) {}

  searchLocations(filters: {
    q?: string;
    type?: string;
    address?: string;
    page?: number;
    size?: number;
  }): Observable<PageResponse<LocationDTO>> {
    let params = new HttpParams();
    Object.entries(filters || {}).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') params = params.set(k, String(v));
    });
    if (!params.has('page')) params = params.set('page', '0');
    if (!params.has('size')) params = params.set('size', '12');

    return this.http.get<PageResponse<LocationDTO>>(`${this.API_URL}/locations`, { params });
  }

  searchEvents(filters: {
    type?: string;
    locationId?: number;
    address?: string;
    minPrice?: number;
    maxPrice?: number;
    startDate?: string; // ISO YYYY-MM-DD
    endDate?: string; // ISO YYYY-MM-DD
    past?: boolean;
    future?: boolean;
    regularOnly?: boolean;
    page?: number;
    size?: number;
  }): Observable<PageResponse<Event>> {
    let params = new HttpParams();
    const mapKey: Record<string, string> = {
      minPrice: 'minPrice',
      maxPrice: 'maxPrice',
      startDate: 'startDate',
      endDate: 'endDate',
      regularOnly: 'isRegular',
    };
    Object.entries(filters || {}).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        const key = mapKey[k] ?? k;
        params = params.set(key, String(v));
      }
    });
    if (!params.has('page')) params = params.set('page', '0');
    if (!params.has('size')) params = params.set('size', '9');

    return this.http.get<PageResponse<Event>>(`${this.API_URL}/events`, { params });
  }
}
