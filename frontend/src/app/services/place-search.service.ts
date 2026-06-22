import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PlaceSearchResult {
  id: number;
  name: string;
  description?: string;
  address?: string;
  type?: string;
  reviewCount: number;
  totalRating?: number;
  imageUrl?: string;
  hasPdf?: boolean;
}

export interface PlaceSearchPageResponse {
  content: PlaceSearchResult[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface PlaceSearchFilters {
  name?: string;
  description?: string;
  pdf?: string;
  reviewsFrom?: number;
  reviewsTo?: number;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class PlaceSearchService {
  private readonly API_URL = '/api/search/places';

  constructor(private http: HttpClient) {}

  search(filters: PlaceSearchFilters): Observable<PlaceSearchPageResponse> {
    let params = new HttpParams();
    Object.entries(filters || {}).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params = params.set(k, String(v));
      }
    });
    if (!params.has('page')) params = params.set('page', '0');
    if (!params.has('size')) params = params.set('size', '10');
    return this.http.get<PlaceSearchPageResponse>(this.API_URL, { params });
  }
}
