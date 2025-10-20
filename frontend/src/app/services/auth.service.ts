import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  MessageResponse,
} from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user_data';

  currentUser = signal<User | null>(null);
  isAuthenticated = signal<boolean>(false);

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const refresh = localStorage.getItem(this.REFRESH_TOKEN_KEY);
    const userData = localStorage.getItem(this.USER_KEY);

    if (token && refresh && userData) {
      this.currentUser.set(JSON.parse(userData));
      this.isAuthenticated.set(true);
    }
  }

  register(request: RegisterRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/register-request`, request);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request).pipe(
      tap((response) => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
        const user: User = {
          email: response.email,
          name: response.name,
          roles: response.roles,
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
        this.isAuthenticated.set(true);
      })
    );
  }

  logout(navigate = true): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/logout`, {}).pipe(
      tap(() => {
        this.clearSession();
        if (navigate) this.router.navigate(['/']);
      })
    );
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem(this.REFRESH_TOKEN_KEY);
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken }).pipe(
      tap((response) => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
        const user: User = {
          email: response.email,
          name: response.name,
          roles: response.roles,
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
        this.isAuthenticated.set(true);
      })
    );
  }

  clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  hasRole(role: string): boolean {
    const user = this.currentUser();
    return user?.roles?.includes(role) || false;
  }
}
