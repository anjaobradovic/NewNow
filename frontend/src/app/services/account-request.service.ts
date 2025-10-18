import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountRequest, ProcessAccountRequest } from '../models/account-request.model';

@Injectable({
  providedIn: 'root',
})
export class AccountRequestService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api';
  private apiUrl = `${this.API_URL}/admin/requests`;

  getPendingRequests(): Observable<AccountRequest[]> {
    return this.http.get<AccountRequest[]>(`${this.apiUrl}/pending`);
  }

  getAllRequests(): Observable<AccountRequest[]> {
    return this.http.get<AccountRequest[]>(this.apiUrl);
  }

  getRequestById(id: number): Observable<AccountRequest> {
    return this.http.get<AccountRequest>(`${this.apiUrl}/${id}`);
  }

  processRequest(data: ProcessAccountRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/process`, data, {
      responseType: 'text',
    });
  }
}
