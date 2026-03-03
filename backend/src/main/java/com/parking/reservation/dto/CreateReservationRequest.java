package com.parking.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateReservationRequest {

    @NotNull
    private Long spotId;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
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
