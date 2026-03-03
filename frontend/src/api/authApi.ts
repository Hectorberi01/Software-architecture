import { AuthResponse, LoginPayload, RegisterPayload } from '../types/auth';

const API_URL = '/api/auth';

export const authApi = {
    login: async (data: LoginPayload): Promise<AuthResponse> => {
        const res = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!res.ok) throw new Error('Identifiants invalides');
        return res.json();
    },

    register: async (data: RegisterPayload): Promise<AuthResponse> => {
        const res = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!res.ok) throw new Error('Erreur lors de l\'enregistrement. Email peut-être déjà pris.');
        return res.json();
    },
};
