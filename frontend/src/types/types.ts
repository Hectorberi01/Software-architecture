export interface ParkingSpot {
  id: number
  code: string
  row: string
  number: number
  hasCharger: boolean
}

export interface Reservation {
  id: number
  parkingSpotId: number
  spotCode: string
  userId: number
  userEmail: string
  reservationDate: string
  createdAt: string
}

export interface CreateReservationRequest {
  parkingSpotId: number
  userId: number
  reservationDate: string
}

export interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  createdAt: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface ReservationHistory {
  activeReservations: Reservation[]
  pastReservations: Reservation[]
}
