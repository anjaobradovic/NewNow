import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ManagedLocationDTO,
  PageResponse,
  ReviewDTO,
  UpdateProfileRequest,
  UserProfile,
} from '../models/user.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly API_URL = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  getMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/me`);
  }

  updateMe(payload: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.patch<UserProfile>(`${this.API_URL}/me`, payload);
  }

  updateAvatar(file: File): Observable<MessageResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<MessageResponse>(`${this.API_URL}/me/avatar`, formData);
  }

  changePassword(
    currentPassword: string,
    newPassword: string,
    confirmPassword: string
  ): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/me/change-password`, {
      currentPassword,
      newPassword,
      confirmPassword,
    });
  }

  getMyReviews(
    page = 0,
    size = 10,
    sort: 'rating' | 'date' = 'date',
    order: 'asc' | 'desc' = 'desc'
  ): Observable<PageResponse<ReviewDTO>> {
    return this.http.get<PageResponse<ReviewDTO>>(
      `${this.API_URL}/me/reviews?page=${page}&size=${size}&sort=${sort}&order=${order}`
    );
  }

  getManagedLocations(): Observable<ManagedLocationDTO[]> {
    return this.http.get<ManagedLocationDTO[]>(`${this.API_URL}/me/managed-locations`);
  }
}
