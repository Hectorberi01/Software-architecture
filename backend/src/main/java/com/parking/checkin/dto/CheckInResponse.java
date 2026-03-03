package com.parking.checkin.dto;

import com.parking.reservation.model.ReservationStatus;

public class CheckInResponse {

    private Long reservationId;
    private String spotCode;
    private ReservationStatus status;
    private String message;

    public CheckInResponse(Long reservationId, String spotCode, ReservationStatus status, String message) {
        this.reservationId = reservationId;
        this.spotCode = spotCode;
        this.status = status;
        this.message = message;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
