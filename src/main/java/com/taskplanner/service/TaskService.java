package com.taskplanner.service;

import com.taskplanner.dto.ScheduleRequest;
import com.taskplanner.dto.TaskFeedbackRequest;
import com.taskplanner.model.Enums;
import com.taskplanner.model.HabitDriftReport;
import com.taskplanner.model.SchedulePlan;
import com.taskplanner.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {

    private final Map<String, Task> tasksMap = new ConcurrentHashMap<>();
    private final UserProfileService profileService;
    private final BehavioralEngine behavioralEngine;
    private final OpenSourceAiService aiService;

    public TaskService(UserProfileService profileService,
                       BehavioralEngine behavioralEngine,
                       OpenSourceAiService aiService) {
        this.profileService = profileService;
        this.behavioralEngine = behavioralEngine;
        this.aiService = aiService;
        initSampleTasks();
    }

    private void initSampleTasks() {
        LocalDate today = LocalDate.now();

        Task t1 = new Task("Architect Core AI Engine & Scheduling Constraints", 90, Enums.TaskCategory.DEEP_WORK, Enums.Priority.HIGH);
        t1.setEnergyDemand(Enums.EnergyDemand.HIGH);
        t1.setScheduledDate(today);

        Task t2 = new Task("Inbox Zero & Review Team Pull Requests", 45, Enums.TaskCategory.ADMINISTRATIVE, Enums.Priority.MEDIUM);
        t2.setEnergyDemand(Enums.EnergyDemand.LOW);
        t2.setScheduledDate(today);

        Task t3 = new Task("Draft Feature Proposal & System Architecture Document", 60, Enums.TaskCategory.CREATIVE, Enums.Priority.HIGH);
        t3.setEnergyDemand(Enums.EnergyDemand.HIGH);
        t3.setScheduledDate(today);

        Task t4 = new Task("Study Advanced Time-Series Bayesian Learning", 45, Enums.TaskCategory.LEARNING, Enums.Priority.MEDIUM);
        t4.setEnergyDemand(Enums.EnergyDemand.MEDIUM);
        t4.setScheduledDate(today);

        Task t5 = new Task("Grocery Restock & Weekly Meal Prep", 45, Enums.TaskCategory.ERRAND, Enums.Priority.LOW);
        t5.setEnergyDemand(Enums.EnergyDemand.LOW);
        t5.setScheduledDate(today);

        tasksMap.put(t1.getId(), t1);
        tasksMap.put(t2.getId(), t2);
        tasksMap.put(t3.getId(), t3);
        tasksMap.put(t4.getId(), t4);
        tasksMap.put(t5.getId(), t5);

        // Pre-seed some past completed tasks to showcase habit learning & drift analytics
        LocalDate yesterday = today.minusDays(1);
        Task past1 = new Task("Complete Quarterly OKR Review", 60, Enums.TaskCategory.DEEP_WORK, Enums.Priority.HIGH);
        past1.setScheduledDate(yesterday);
        past1.setScheduledStartTime(LocalTime.of(9, 30));
        past1.setScheduledEndTime(LocalTime.of(10, 30));
        past1.setActualStartTime(LocalTime.of(9, 45));
        past1.setActualEndTime(LocalTime.of(11, 0));
        past1.setActualMinutes(75);
        past1.setStatus(Enums.TaskStatus.COMPLETED);
        past1.setFeedbackRating(Enums.FeedbackRating.FELT_RUSHED);
        tasksMap.put(past1.getId(), past1);
        behavioralEngine.recordTaskCompletion(profileService.getProfile(), past1);

        Task past2 = new Task("Organize Cloud Server Invoices", 30, Enums.TaskCategory.ADMINISTRATIVE, Enums.Priority.LOW);
        past2.setScheduledDate(yesterday);
        past2.setScheduledStartTime(LocalTime.of(14, 0));
        past2.setScheduledEndTime(LocalTime.of(14, 30));
        past2.setActualStartTime(LocalTime.of(14, 5));
        past2.setActualEndTime(LocalTime.of(14, 25));
        past2.setActualMinutes(20);
        past2.setStatus(Enums.TaskStatus.COMPLETED);
        past2.setFeedbackRating(Enums.FeedbackRating.EASY_FAST);
        tasksMap.put(past2.getId(), past2);
        behavioralEngine.recordTaskCompletion(profileService.getProfile(), past2);
    }

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>(tasksMap.values());
        list.sort(Comparator.comparing(Task::getScheduledDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getScheduledStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    public List<Task> getTasksForDate(LocalDate date) {
        List<Task> list = new ArrayList<>();
        for (Task t : tasksMap.values()) {
            if (date.equals(t.getScheduledDate())) {
                list.add(t);
            }
        }
        list.sort(Comparator.comparing(Task::getScheduledStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    public Task getTaskById(String id) {
        return tasksMap.get(id);
    }

    public Task saveTask(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(UUID.randomUUID().toString());
        }
        if (task.getScheduledDate() == null) {
            task.setScheduledDate(LocalDate.now());
        }
        if (task.getEnergyDemand() == null && task.getCategory() != null) {
            task.setEnergyDemand(task.getCategory().getTypicalEnergy());
        }
        tasksMap.put(task.getId(), task);
        return task;
    }

    public boolean deleteTask(String id) {
        return tasksMap.remove(id) != null;
    }

    public SchedulePlan scheduleDay(LocalDate date, ScheduleRequest request) {
        int buffer = request != null && request.getBufferMinutes() != null ? request.getBufferMinutes() : profileService.getProfile().getDefaultBufferMinutes();
        List<Task> tasksForDay = getTasksForDate(date);
        SchedulePlan plan = aiService.generateOptimalSchedule(
                date,
                tasksForDay,
                profileService.getRoutineAnchors(),
                profileService.getProfile(),
                buffer
        );

        // Update task storage with scheduled times
        if (plan.getScheduledTasks() != null) {
            for (Task t : plan.getScheduledTasks()) {
                tasksMap.put(t.getId(), t);
            }
        }

        return plan;
    }

    public Task recordFeedback(TaskFeedbackRequest feedback) {
        Task task = tasksMap.get(feedback.getTaskId());
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + feedback.getTaskId());
        }

        if (feedback.getActualMinutes() != null) {
            task.setActualMinutes(feedback.getActualMinutes());
        } else if (task.getEstimatedMinutes() > 0) {
            task.setActualMinutes(task.getEstimatedMinutes());
        }

        if (feedback.getActualStartTime() != null) {
            task.setActualStartTime(feedback.getActualStartTime());
        }
        if (feedback.getActualEndTime() != null) {
            task.setActualEndTime(feedback.getActualEndTime());
        }
        if (feedback.getFeedbackRating() != null) {
            task.setFeedbackRating(feedback.getFeedbackRating());
        }
        if (feedback.getNotes() != null) {
            task.setFeedbackNotes(feedback.getNotes());
        }

        task.setStatus(Enums.TaskStatus.COMPLETED);
        tasksMap.put(task.getId(), task);

        // Trigger AI / statistical behavioral learning
        behavioralEngine.recordTaskCompletion(profileService.getProfile(), task);

        return task;
    }

    public HabitDriftReport getHabitDriftReport() {
        return behavioralEngine.analyzeHabitDrift(profileService.getProfile(), new ArrayList<>(tasksMap.values()));
    }
}
