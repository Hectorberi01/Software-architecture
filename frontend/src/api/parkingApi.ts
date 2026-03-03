import { ParkingSpot, Reservation, CreateReservationRequest } from '../types/parking';

const API_URL = '/api';

const getHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const parkingApi = {
    getAvailableSpots: async (startDate: string, endDate: string, withCharger?: boolean): Promise<ParkingSpot[]> => {
        let url = `${API_URL}/parking-spots/available?startDate=${startDate}&endDate=${endDate}`;
        if (withCharger !== undefined) {
            url += `&withCharger=${withCharger}`;
        }
        const res = await fetch(url, { headers: getHeaders() });
        if (!res.ok) throw new Error('Erreur de récupération des places libres');
        return res.json();
    },

    createReservation: async (data: CreateReservationRequest): Promise<Reservation> => {
        const res = await fetch(`${API_URL}/reservations`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(data),
        });

        const text = await res.text();
        if (!res.ok) {
            let errMsg = 'Erreur lors de la réservation';
            try {
                const errObj = JSON.parse(text);
                errMsg = errObj.message || errMsg;
            } catch (e) { /* ignore parse error */ }
            throw new Error(errMsg);
        }
        return JSON.parse(text);
    },

    getMyReservations: async (): Promise<Reservation[]> => {
        const res = await fetch(`${API_URL}/reservations/my`, { headers: getHeaders() });
        if (!res.ok) throw new Error('Erreur récupération des réservations');
        return res.json();
    },

    getAllReservations: async (): Promise<Reservation[]> => {
        const res = await fetch(`${API_URL}/reservations`, { headers: getHeaders() });
        if (!res.ok) throw new Error('Erreur globale des réservations');
        return res.json();
    },

    cancelReservation: async (id: number): Promise<Reservation> => {
        const res = await fetch(`${API_URL}/reservations/${id}`, {
            method: 'DELETE',
            headers: getHeaders(),
        });
        if (!res.ok) throw new Error('Impossible d\'annuler');
        // Assuming ReservationResponse is equivalent to Reservation or a compatible type
        // The original code returned res.json() which is Promise<Reservation>
        // To match the provided snippet, we'd need to fetch and parse JSON, then cast.
        // For now, keeping it consistent with existing return types and assuming Reservation.
        return res.json();
    },

    approveReservation: async (id: number): Promise<Reservation> => { // Changed ReservationResponse to Reservation
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_URL}/reservations/${id}/approve`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json' // Added Content-Type for consistency
            }
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Erreur lors de l\'approbation de la réservation');
        }
        const data = await response.json();
        return data as Reservation; // Changed ReservationResponse to Reservation
    },

    rejectReservation: async (id: number): Promise<Reservation> => {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_URL}/reservations/${id}/reject`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Erreur lors du refus de la réservation');
        }
        const data = await response.json();
        return data as Reservation;
    }
};
