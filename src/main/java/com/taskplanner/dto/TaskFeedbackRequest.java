package com.taskplanner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskplanner.model.Enums;
import java.time.LocalTime;

public class TaskFeedbackRequest {
    private String taskId;
    private Integer actualMinutes;
    private Enums.FeedbackRating feedbackRating;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime actualStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime actualEndTime;

    private String notes;

    public TaskFeedbackRequest() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public Integer getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(Integer actualMinutes) { this.actualMinutes = actualMinutes; }

    public Enums.FeedbackRating getFeedbackRating() { return feedbackRating; }
    public void setFeedbackRating(Enums.FeedbackRating feedbackRating) { this.feedbackRating = feedbackRating; }

    public LocalTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalTime actualEndTime) { this.actualEndTime = actualEndTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
