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
  userEmail: string
  reservationDate: string
  createdAt: string
}

export interface CreateReservationRequest {
  parkingSpotId: number
  userEmail: string
  reservationDate: string
}
