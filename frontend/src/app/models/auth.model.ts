export interface User {
  email: string;
  name: string;
  roles: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  phoneNumber?: string;
  birthday?: string; // ISO date string (YYYY-MM-DD)
  address: string;
  city?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  email: string;
  name: string;
  roles: string[];
}

export interface MessageResponse {
  message: string;
}
