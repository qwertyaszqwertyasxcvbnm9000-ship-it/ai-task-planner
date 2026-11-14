package com.taskplanner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public class TimeSlot {
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private boolean available;
    private String occupiedBy;
    private Enums.EnergyDemand energyCapacity;

    public TimeSlot() {}

    public TimeSlot(LocalTime startTime, LocalTime endTime, boolean available, String occupiedBy, Enums.EnergyDemand energyCapacity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
        this.occupiedBy = occupiedBy;
        this.energyCapacity = energyCapacity;
    }

    public int getDurationMinutes() {
        if (startTime == null || endTime == null) return 0;
        int startMin = startTime.getHour() * 60 + startTime.getMinute();
        int endMin = endTime.getHour() * 60 + endTime.getMinute();
        return endMin - startMin;
    }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getOccupiedBy() { return occupiedBy; }
    public void setOccupiedBy(String occupiedBy) { this.occupiedBy = occupiedBy; }

    public Enums.EnergyDemand getEnergyCapacity() { return energyCapacity; }
    public void setEnergyCapacity(Enums.EnergyDemand energyCapacity) { this.energyCapacity = energyCapacity; }
}
