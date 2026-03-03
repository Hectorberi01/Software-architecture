import { Role } from './auth';

export interface UserAdminDto {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
    enabled: boolean;
    createdAt: string;
}

export interface CreateUserRequest {
    email?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
    role?: Role;
}

export interface UpdateUserRequest {
    firstName?: string;
    lastName?: string;
    role?: Role;
    enabled?: boolean;
}
