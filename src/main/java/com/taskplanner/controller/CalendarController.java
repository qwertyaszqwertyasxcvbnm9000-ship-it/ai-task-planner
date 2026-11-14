package com.taskplanner.controller;

import com.taskplanner.model.RoutineAnchor;
import com.taskplanner.model.Task;
import com.taskplanner.service.CalendarSyncService;
import com.taskplanner.service.TaskService;
import com.taskplanner.service.UserProfileService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*")
public class CalendarController {

    private final CalendarSyncService calendarSyncService;
    private final TaskService taskService;
    private final UserProfileService profileService;

    public CalendarController(CalendarSyncService calendarSyncService,
                              TaskService taskService,
                              UserProfileService profileService) {
        this.calendarSyncService = calendarSyncService;
        this.taskService = taskService;
        this.profileService = profileService;
    }

    @GetMapping(value = "/export.ics", produces = "text/calendar")
    public ResponseEntity<String> exportIcs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        List<Task> tasks = taskService.getTasksForDate(target);
        List<RoutineAnchor> anchors = profileService.getRoutineAnchors();

        String icsContent = calendarSyncService.generateIcsCalendar(target, tasks, anchors);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", "routine_schedule_" + target + ".ics");

        return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent);
    }
}
