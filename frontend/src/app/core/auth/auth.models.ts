export type UserRole = 'USER' | 'ADMIN' | 'DEMO';

export interface AuthUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
}

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface MessageResponse {
  message: string;
}

/** Einheitliches Fehlerformat des Backends (siehe docs/api-design.md). */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  correlationId?: string;
  fieldErrors?: { field: string; message: string }[];
}

export type AuthStatus = 'unknown' | 'authenticated' | 'anonymous';
