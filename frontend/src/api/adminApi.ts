import { UserAdminDto, CreateUserRequest, UpdateUserRequest } from '../types/admin';

const API_URL = '/api/admin/users';

const getHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const adminApi = {
    getAllUsers: async (): Promise<UserAdminDto[]> => {
        const res = await fetch(API_URL, { headers: getHeaders() });
        if (!res.ok) throw new Error('Accès refusé');
        return res.json();
    },

    createUser: async (data: CreateUserRequest): Promise<UserAdminDto> => {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error('Erreur de création');
        return res.json();
    },

    updateUser: async (id: number, data: UpdateUserRequest): Promise<UserAdminDto> => {
        const res = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error('Erreur de mise à jour');
        return res.json();
    },

    deleteUser: async (id: number): Promise<void> => {
        const res = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getHeaders()
        });
        if (!res.ok) throw new Error('Erreur de suppression');
    }
};
