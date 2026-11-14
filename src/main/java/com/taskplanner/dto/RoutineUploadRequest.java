package com.taskplanner.dto;

public class RoutineUploadRequest {
    private String format; // "CSV", "JSON", "ICS", "TEXT"
    private String content;
    private boolean autoApplyToProfile = true;

    public RoutineUploadRequest() {}

    public RoutineUploadRequest(String format, String content, boolean autoApplyToProfile) {
        this.format = format;
        this.content = content;
        this.autoApplyToProfile = autoApplyToProfile;
    }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isAutoApplyToProfile() { return autoApplyToProfile; }
    public void setAutoApplyToProfile(boolean autoApplyToProfile) { this.autoApplyToProfile = autoApplyToProfile; }
}
