package com.taskplanner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class ScheduleRequest {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Integer bufferMinutes;
    private Boolean energyAware;
    private Boolean autoExtendDuration;

    public ScheduleRequest() {
        this.date = LocalDate.now();
        this.bufferMinutes = 15;
        this.energyAware = true;
        this.autoExtendDuration = true;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getBufferMinutes() { return bufferMinutes; }
    public void setBufferMinutes(Integer bufferMinutes) { this.bufferMinutes = bufferMinutes; }

    public Boolean getEnergyAware() { return energyAware; }
    public void setEnergyAware(Boolean energyAware) { this.energyAware = energyAware; }

    public Boolean getAutoExtendDuration() { return autoExtendDuration; }
    public void setAutoExtendDuration(Boolean autoExtendDuration) { this.autoExtendDuration = autoExtendDuration; }
}
