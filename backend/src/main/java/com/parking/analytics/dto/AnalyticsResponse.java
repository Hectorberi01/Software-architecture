package com.parking.analytics.dto;

public class AnalyticsResponse {

    private long totalSpots;
    private long occupiedToday;
    private double occupancyRate;
    private long totalReservations;
    private long noShowCount;
    private double noShowRate;
    private long electricSpotsInUse;
    private long totalElectricSpots;
    private double electricUsageRate;

    public AnalyticsResponse(long totalSpots, long occupiedToday, long totalReservations,
            long noShowCount, long electricSpotsInUse, long totalElectricSpots) {
        this.totalSpots = totalSpots;
        this.occupiedToday = occupiedToday;
        this.occupancyRate = totalSpots > 0 ? (double) occupiedToday / totalSpots * 100 : 0;
        this.totalReservations = totalReservations;
        this.noShowCount = noShowCount;
        this.noShowRate = totalReservations > 0 ? (double) noShowCount / totalReservations * 100 : 0;
        this.electricSpotsInUse = electricSpotsInUse;
        this.totalElectricSpots = totalElectricSpots;
        this.electricUsageRate = totalElectricSpots > 0 ? (double) electricSpotsInUse / totalElectricSpots * 100 : 0;
    }

    public long getTotalSpots() {
        return totalSpots;
    }

    public long getOccupiedToday() {
        return occupiedToday;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public long getTotalReservations() {
        return totalReservations;
    }

    public long getNoShowCount() {
        return noShowCount;
    }

    public double getNoShowRate() {
        return noShowRate;
    }

    public long getElectricSpotsInUse() {
        return electricSpotsInUse;
    }

    public long getTotalElectricSpots() {
        return totalElectricSpots;
    }

    public double getElectricUsageRate() {
        return electricUsageRate;
    }
}
