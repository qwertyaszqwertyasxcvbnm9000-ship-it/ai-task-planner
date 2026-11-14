package com.taskplanner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private Enums.TaskCategory category;
    private Enums.Priority priority;
    private Enums.EnergyDemand energyDemand;
    private int estimatedMinutes;
    private Integer actualMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime scheduledStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime scheduledEndTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime actualStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime actualEndTime;

    private Enums.TaskStatus status;
    private Enums.FeedbackRating feedbackRating;
    private String feedbackNotes;
    private String reasoning;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.status = Enums.TaskStatus.PENDING;
        this.category = Enums.TaskCategory.DEEP_WORK;
        this.priority = Enums.Priority.MEDIUM;
        this.energyDemand = Enums.EnergyDemand.MEDIUM;
        this.estimatedMinutes = 45;
        this.scheduledDate = LocalDate.now();
    }

    public Task(String title, int estimatedMinutes, Enums.TaskCategory category, Enums.Priority priority) {
        this();
        this.title = title;
        this.estimatedMinutes = estimatedMinutes;
        this.category = category;
        this.priority = priority;
        this.energyDemand = category != null ? category.getTypicalEnergy() : Enums.EnergyDemand.MEDIUM;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Enums.TaskCategory getCategory() { return category; }
    public void setCategory(Enums.TaskCategory category) { this.category = category; }

    public Enums.Priority getPriority() { return priority; }
    public void setPriority(Enums.Priority priority) { this.priority = priority; }

    public Enums.EnergyDemand getEnergyDemand() { return energyDemand; }
    public void setEnergyDemand(Enums.EnergyDemand energyDemand) { this.energyDemand = energyDemand; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public Integer getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(Integer actualMinutes) { this.actualMinutes = actualMinutes; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public LocalTime getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(LocalTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }

    public LocalTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalTime actualEndTime) { this.actualEndTime = actualEndTime; }

    public Enums.TaskStatus getStatus() { return status; }
    public void setStatus(Enums.TaskStatus status) { this.status = status; }

    public Enums.FeedbackRating getFeedbackRating() { return feedbackRating; }
    public void setFeedbackRating(Enums.FeedbackRating feedbackRating) { this.feedbackRating = feedbackRating; }

    public String getFeedbackNotes() { return feedbackNotes; }
    public void setFeedbackNotes(String feedbackNotes) { this.feedbackNotes = feedbackNotes; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
