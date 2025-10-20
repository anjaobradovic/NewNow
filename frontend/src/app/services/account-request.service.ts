import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AccountRequest } from '../models/account-request.model';

@Injectable({
  providedIn: 'root',
})
export class AccountRequestService {
  private http = inject(HttpClient);
  private readonly API_URL = '/api/admin';

  getPendingRequests(): Observable<AccountRequest[]> {
    // Backend returns paged response; map to array if needed at component level
    const params = new HttpParams().set('status', 'pending').set('page', 0).set('size', 50);
    return this.http
      .get<{ content?: AccountRequest[] }>(`${this.API_URL}/register-requests`, { params })
      .pipe(map((res) => (Array.isArray(res?.content) ? res.content! : (res as any) || [])));
  }

  getAllRequests(status?: string, page = 0, size = 50): Observable<AccountRequest[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http
      .get<{ content?: AccountRequest[] }>(`${this.API_URL}/register-requests`, { params })
      .pipe(map((res) => (Array.isArray(res?.content) ? res.content! : (res as any) || [])));
  }

  approveRequest(id: number): Observable<any> {
    return this.http.patch<any>(`${this.API_URL}/register-requests/${id}/approve`, {});
  }

  rejectRequest(id: number): Observable<any> {
    return this.http.patch<any>(`${this.API_URL}/register-requests/${id}/reject`, {});
  }
}
