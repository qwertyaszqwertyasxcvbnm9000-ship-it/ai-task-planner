package com.taskplanner.controller;

import com.taskplanner.dto.ScheduleRequest;
import com.taskplanner.dto.TaskFeedbackRequest;
import com.taskplanner.model.HabitDriftReport;
import com.taskplanner.model.SchedulePlan;
import com.taskplanner.model.Task;
import com.taskplanner.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/day")
    public ResponseEntity<List<Task>> getTasksForDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(taskService.getTasksForDate(target));
    }

    @PostMapping
    public ResponseEntity<Task> createOrUpdateTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        boolean deleted = taskService.deleteTask(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/schedule")
    public ResponseEntity<SchedulePlan> scheduleDay(@RequestBody(required = false) ScheduleRequest request) {
        ScheduleRequest req = request != null ? request : new ScheduleRequest();
        LocalDate target = req.getDate() != null ? req.getDate() : LocalDate.now();
        return ResponseEntity.ok(taskService.scheduleDay(target, req));
    }

    @PostMapping("/feedback")
    public ResponseEntity<Task> recordFeedback(@RequestBody TaskFeedbackRequest feedback) {
        return ResponseEntity.ok(taskService.recordFeedback(feedback));
    }

    @GetMapping("/drift")
    public ResponseEntity<HabitDriftReport> getHabitDrift() {
        return ResponseEntity.ok(taskService.getHabitDriftReport());
    }
}
