package com.parking.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_spots")
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "`row`", nullable = false, length = 1)
    private String row;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private boolean hasCharger;

    public ParkingSpot() {}

    public ParkingSpot(Long id, String code, String row, int number, boolean hasCharger) {
        this.id = id;
        this.code = code;
        this.row = row;
        this.number = number;
        this.hasCharger = hasCharger;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public boolean isHasCharger() { return hasCharger; }
    public void setHasCharger(boolean hasCharger) { this.hasCharger = hasCharger; }
}
