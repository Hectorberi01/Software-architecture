import { CheckInResponse } from '../types/parking';

const API_URL = '/api/checkin';

const getHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const checkInApi = {
    checkInDashboard: async (spotId: number): Promise<CheckInResponse> => {
        const res = await fetch(`${API_URL}/spot/${spotId}`, {
            method: 'POST',
            headers: getHeaders()
        });
        if (!res.ok) throw new Error('Erreur de check-in');
        return res.json();
    },

    checkInQrCode: async (spotCode: string, userEmail: string): Promise<CheckInResponse> => {
        const res = await fetch(`${API_URL}/spot/${spotCode}/qr?userEmail=${encodeURIComponent(userEmail)}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!res.ok) throw new Error('Code QR invalide ou pas de réservation active');
        return res.json();
    }
};
