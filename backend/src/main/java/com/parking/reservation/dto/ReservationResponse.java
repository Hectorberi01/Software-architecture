package com.parking.reservation.dto;

import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private Long spotId;
    private String spotCode;
    private String spotRow;
    private boolean spotHasCharger;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation r) {
        ReservationResponse dto = new ReservationResponse();
        dto.id = r.getId();
        dto.userId = r.getUser().getId();
        dto.userEmail = r.getUser().getEmail();
        dto.userFirstName = r.getUser().getFirstName();
        dto.userLastName = r.getUser().getLastName();
        dto.spotId = r.getSpot().getId();
        dto.spotCode = r.getSpot().getCode();
        dto.spotRow = r.getSpot().getRow();
        dto.spotHasCharger = r.getSpot().isHasCharger();
        dto.startDate = r.getStartDate();
        dto.endDate = r.getEndDate();
        dto.status = r.getStatus();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public Long getSpotId() {
        return spotId;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public String getSpotRow() {
        return spotRow;
    }

    public boolean isSpotHasCharger() {
        return spotHasCharger;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
