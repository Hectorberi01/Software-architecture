export type Role = 'EMPLOYEE' | 'MANAGER' | 'ADMIN';

export interface User {
  id?: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface AuthResponse extends User {
  token: string;
}

export interface LoginPayload {
  email?: string;
  password?: string;
}

export interface RegisterPayload {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
}
