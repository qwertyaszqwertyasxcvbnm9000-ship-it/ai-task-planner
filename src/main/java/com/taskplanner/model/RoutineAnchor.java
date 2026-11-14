package com.taskplanner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoutineAnchor {
    private String id;
    private String name;
    private Enums.TaskCategory category;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private boolean flexible;
    private Set<DayOfWeek> daysOfWeek;
    private String notes;

    public RoutineAnchor() {
        this.id = UUID.randomUUID().toString();
        this.daysOfWeek = new HashSet<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            this.daysOfWeek.add(day);
        }
        this.flexible = false;
    }

    public RoutineAnchor(String name, LocalTime startTime, LocalTime endTime, Enums.TaskCategory category, boolean flexible) {
        this();
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.flexible = flexible;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Enums.TaskCategory getCategory() { return category; }
    public void setCategory(Enums.TaskCategory category) { this.category = category; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isFlexible() { return flexible; }
    public void setFlexible(boolean flexible) { this.flexible = flexible; }

    public Set<DayOfWeek> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
