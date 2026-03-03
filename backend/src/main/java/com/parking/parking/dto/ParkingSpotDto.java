package com.parking.parking.dto;

import com.parking.parking.model.ParkingSpot;

public class ParkingSpotDto {

    private Long id;
    private String code;
    private String row;
    private Integer number;
    private boolean hasCharger;

    public static ParkingSpotDto from(ParkingSpot spot) {
        ParkingSpotDto dto = new ParkingSpotDto();
        dto.id = spot.getId();
        dto.code = spot.getCode();
        dto.row = spot.getRow();
        dto.number = spot.getNumber();
        dto.hasCharger = spot.isHasCharger();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getRow() {
        return row;
    }

    public Integer getNumber() {
        return number;
    }

    public boolean isHasCharger() {
        return hasCharger;
    }
}
