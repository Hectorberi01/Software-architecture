export interface ParkingSpot {
    id: number;
    code: string;
    row: string;
    number: number;
    hasCharger: boolean;
}

export type ReservationStatus = 'PENDING' | 'APPROVED' | 'CHECKED_IN' | 'NO_SHOW' | 'CANCELLED' | 'REJECTED';

export interface Reservation {
    id: number;
    userId: number;
    userEmail: string;
    userFirstName: string;
    userLastName: string;
    spotId: number;
    spotCode: string;
    spotRow: string;
    spotHasCharger: boolean;
    startDate: string; // ISO yyyy-MM-dd
    endDate: string;   // ISO yyyy-MM-dd
    status: ReservationStatus;
    createdAt: string;
}

export interface CreateReservationRequest {
    spotId: number;
    startDate: string;
    endDate: string;
}

export interface CheckInResponse {
    reservationId: number;
    spotCode: string;
    status: ReservationStatus;
    message: string;
}
