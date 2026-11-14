package com.taskplanner.service;

import com.taskplanner.model.RoutineAnchor;
import com.taskplanner.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CalendarSyncService {

    private static final DateTimeFormatter ICS_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Generates a standard RFC 5545 iCalendar (.ics) feed string.
     */
    public String generateIcsCalendar(LocalDate date, List<Task> tasks, List<RoutineAnchor> anchors) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//AI Routine Task Planner//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:Daily Routine Schedule\r\n");

        LocalDate targetDate = date != null ? date : LocalDate.now();

        // 1. Routine Anchors
        if (anchors != null) {
            for (RoutineAnchor anchor : anchors) {
                if (anchor.getStartTime() != null && anchor.getEndTime() != null) {
                    LocalDateTime startDt = LocalDateTime.of(targetDate, anchor.getStartTime());
                    LocalDateTime endDt = LocalDateTime.of(targetDate, anchor.getEndTime());

                    sb.append("BEGIN:VEVENT\r\n");
                    sb.append("UID:anchor-").append(anchor.getId()).append("@taskplanner.local\r\n");
                    sb.append("DTSTAMP:").append(LocalDateTime.now().format(ICS_DATE_TIME)).append("\r\n");
                    sb.append("DTSTART:").append(startDt.format(ICS_DATE_TIME)).append("\r\n");
                    sb.append("DTEND:").append(endDt.format(ICS_DATE_TIME)).append("\r\n");
                    sb.append("SUMMARY:[Routine] ").append(escapeIcs(anchor.getName())).append("\r\n");
                    sb.append("DESCRIPTION:Recurring routine anchor (").append(anchor.getCategory()).append(")\r\n");
                    sb.append("STATUS:CONFIRMED\r\n");
                    sb.append("END:VEVENT\r\n");
                }
            }
        }

        // 2. Scheduled Tasks
        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getScheduledStartTime() != null && task.getScheduledEndTime() != null) {
                    LocalDate tDate = task.getScheduledDate() != null ? task.getScheduledDate() : targetDate;
                    LocalDateTime startDt = LocalDateTime.of(tDate, task.getScheduledStartTime());
                    LocalDateTime endDt = LocalDateTime.of(tDate, task.getScheduledEndTime());

                    sb.append("BEGIN:VEVENT\r\n");
                    sb.append("UID:task-").append(task.getId()).append("@taskplanner.local\r\n");
                    sb.append("DTSTAMP:").append(LocalDateTime.now().format(ICS_DATE_TIME)).append("\r\n");
                    sb.append("DTSTART:").append(startDt.format(ICS_DATE_TIME)).append("\r\n");
                    sb.append("DTEND:").append(endDt.format(ICS_DATE_TIME)).append("\r\n");
                    sb.append("SUMMARY:").append(escapeIcs(task.getTitle())).append("\r\n");

                    String desc = (task.getDescription() != null ? task.getDescription() : "") +
                            (task.getReasoning() != null ? "\n\nAI Reasoning: " + task.getReasoning() : "");
                    sb.append("DESCRIPTION:").append(escapeIcs(desc)).append("\r\n");
                    sb.append("PRIORITY:").append(task.getPriority() != null ? task.getPriority().name() : "MEDIUM").append("\r\n");
                    sb.append("STATUS:").append(task.getStatus() != null ? task.getStatus().name() : "CONFIRMED").append("\r\n");
                    sb.append("END:VEVENT\r\n");
                }
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
}
