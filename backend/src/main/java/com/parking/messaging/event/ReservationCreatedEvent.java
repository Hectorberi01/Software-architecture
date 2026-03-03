package com.parking.messaging.event;

import java.time.LocalDate;

public class ReservationCreatedEvent {

    private Long reservationId;
    private String userEmail;
    private String userFirstName;
    private String spotCode;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReservationCreatedEvent() {
    }

    public ReservationCreatedEvent(Long reservationId, String userEmail, String userFirstName,
            String spotCode, LocalDate startDate, LocalDate endDate) {
        this.reservationId = reservationId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.spotCode = spotCode;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public void setSpotCode(String spotCode) {
        this.spotCode = spotCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
