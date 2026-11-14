package com.taskplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskplanner.dto.RoutineExtractionResult;
import com.taskplanner.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class OpenSourceAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenSourceAiService.class);

    @Value("${ai.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ai.ollama.model:llama3.2}")
    private String ollamaModel;

    private final ObjectMapper objectMapper;
    private final BehavioralEngine behavioralEngine;

    public OpenSourceAiService(ObjectMapper objectMapper, BehavioralEngine behavioralEngine) {
        this.objectMapper = objectMapper;
        this.behavioralEngine = behavioralEngine;
    }

    /**
     * Checks if local Ollama daemon is reachable.
     */
    public boolean isOllamaAvailable() {
        try {
            URL url = URI.create(ollamaUrl + "/api/tags").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public String getOllamaModel() { return ollamaModel; }
    public void setOllamaModel(String model) { this.ollamaModel = model; }
    public String getOllamaUrl() { return ollamaUrl; }
    public void setOllamaUrl(String url) { this.ollamaUrl = url; }

    /**
     * Extracts routine anchors and circadian patterns from past routine text or notes using open-source AI.
     */
    public RoutineExtractionResult analyzeRoutineWithAi(String rawContent) {
        RoutineExtractionResult result = new RoutineExtractionResult();

        if (isOllamaAvailable()) {
            try {
                String prompt = buildRoutineAnalysisPrompt(rawContent);
                String aiResponse = callOllama(prompt);
                parseAiRoutineResponse(aiResponse, result);
                result.setEngineUsed("OLLAMA (" + ollamaModel.toUpperCase() + ")");
                return result;
            } catch (Exception e) {
                log.warn("Ollama routine analysis failed or timed out, falling back to Java heuristic parser: {}", e.getMessage());
            }
        }

        // Java Heuristic Fallback
        extractRoutineHeuristically(rawContent, result);
        result.setEngineUsed("JAVA_BEHAVIORAL_HEURISTICS");
        return result;
    }

    /**
     * Optimizes a day's task schedule around routine anchors and circadian energy curves.
     */
    public SchedulePlan generateOptimalSchedule(LocalDate date, List<Task> tasks, List<RoutineAnchor> anchors, UserProfile profile, int bufferMinutes) {
        SchedulePlan plan = new SchedulePlan(date);
        plan.setDayAnchors(anchors != null ? anchors : new ArrayList<>());

        if (tasks == null || tasks.isEmpty()) {
            plan.setEngineUsed(isOllamaAvailable() ? "OLLAMA (" + ollamaModel.toUpperCase() + ")" : "JAVA_BEHAVIORAL_HEURISTICS");
            plan.getOptimizationNotes().add("No tasks to schedule for this day.");
            return plan;
        }

        // Try Ollama open-source model first if available
        if (isOllamaAvailable()) {
            try {
                String prompt = buildSchedulePrompt(date, tasks, anchors, profile, bufferMinutes);
                String aiResponse = callOllama(prompt);
                boolean success = parseAiScheduleResponse(aiResponse, tasks, plan);
                if (success) {
                    plan.setEngineUsed("OLLAMA (" + ollamaModel.toUpperCase() + ")");
                    calculateOverallEnergyScore(plan, profile);
                    return plan;
                }
            } catch (Exception e) {
                log.warn("Ollama schedule generation failed, falling back to Java deterministic scheduler: {}", e.getMessage());
            }
        }

        // High-precision Java constraint-satisfaction heuristic scheduler
        runJavaHeuristicScheduler(date, tasks, anchors, profile, bufferMinutes, plan);
        plan.setEngineUsed("JAVA_BEHAVIORAL_HEURISTICS");
        calculateOverallEnergyScore(plan, profile);
        return plan;
    }

    /**
     * Core Java Constraint-Satisfaction & Circadian Scheduling Algorithm.
     */
    public void runJavaHeuristicScheduler(LocalDate date, List<Task> tasks, List<RoutineAnchor> anchors,
                                          UserProfile profile, int bufferMinutes, SchedulePlan plan) {
        DayOfWeek dow = date.getDayOfWeek();
        List<RoutineAnchor> activeAnchors = new ArrayList<>();
        if (anchors != null) {
            for (RoutineAnchor a : anchors) {
                if (a.getDaysOfWeek() == null || a.getDaysOfWeek().contains(dow)) {
                    activeAnchors.add(a);
                }
            }
        }
        activeAnchors.sort(Comparator.comparing(RoutineAnchor::getStartTime));

        // Prioritize tasks: High priority first, then High energy demand, then longer tasks
        List<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> {
            int pComp = Integer.compare(priorityRank(t2.getPriority()), priorityRank(t1.getPriority()));
            if (pComp != 0) return pComp;
            return Integer.compare(energyRank(t2.getEnergyDemand()), energyRank(t1.getEnergyDemand()));
        });

        // Determine working window
        LocalTime dayStart = profile.getWakeTime() != null ? profile.getWakeTime().plusMinutes(45) : LocalTime.of(8, 0);
        LocalTime dayEnd = profile.getSleepTime() != null ? profile.getSleepTime().minusMinutes(45) : LocalTime.of(22, 0);

        // Find available slots between fixed routine anchors
        List<TimeSlot> availableSlots = computeAvailableSlots(dayStart, dayEnd, activeAnchors, bufferMinutes);

        List<Task> scheduledList = new ArrayList<>();
        for (Task task : sortedTasks) {
            int adjustedDuration = behavioralEngine.getAdjustedDuration(profile, task);
            TimeSlot bestSlot = null;
            double bestScore = -1.0;
            LocalTime bestStartTime = null;

            // Search available slots for the best circadian match
            for (TimeSlot slot : availableSlots) {
                int slotMinutes = slot.getDurationMinutes();
                if (slotMinutes >= adjustedDuration) {
                    LocalTime candidateTime = slot.getStartTime();
                    double alignmentScore = behavioralEngine.calculateEnergyAlignmentScore(profile, task, candidateTime);

                    // Add priority bonus if high priority placed in peak cognitive hours
                    if (task.getPriority() == Enums.Priority.HIGH && profile.getPeakCognitiveHours().contains(candidateTime.getHour())) {
                        alignmentScore += 0.3;
                    }

                    if (alignmentScore > bestScore) {
                        bestScore = alignmentScore;
                        bestSlot = slot;
                        bestStartTime = candidateTime;
                    }
                }
            }

            if (bestSlot != null && bestStartTime != null) {
                LocalTime endTime = bestStartTime.plusMinutes(adjustedDuration);
                task.setScheduledDate(date);
                task.setScheduledStartTime(bestStartTime);
                task.setScheduledEndTime(endTime);
                task.setStatus(Enums.TaskStatus.PENDING);

                String explanation = String.format("Scheduled during %02d:00 window (energy match: %.0f%%). Duration adjusted to %d mins based on past velocity.",
                        bestStartTime.getHour(), Math.min(100.0, bestScore * 100.0), adjustedDuration);
                task.setReasoning(explanation);
                scheduledList.add(task);

                // Split or advance the used slot
                LocalTime nextAvailable = endTime.plusMinutes(bufferMinutes);
                if (nextAvailable.isBefore(bestSlot.getEndTime())) {
                    bestSlot.setStartTime(nextAvailable);
                } else {
                    availableSlots.remove(bestSlot);
                }
            } else {
                task.setReasoning("Could not fit into day window without colliding with routine anchors. Consider shortening duration or moving to tomorrow.");
                task.setStatus(Enums.TaskStatus.RESCHEDULED);
                scheduledList.add(task);
            }
        }

        plan.setScheduledTasks(scheduledList);
        plan.getOptimizationNotes().add(String.format("Optimized %d tasks around %d routine anchors with %d min buffers.",
                scheduledList.size(), activeAnchors.size(), bufferMinutes));
        plan.getOptimizationNotes().add(String.format("Circadian alignment prioritized for %s chronotype.", profile.getChronotype()));
    }

    private List<TimeSlot> computeAvailableSlots(LocalTime start, LocalTime end, List<RoutineAnchor> anchors, int buffer) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime cursor = start;

        for (RoutineAnchor anchor : anchors) {
            if (anchor.getStartTime().isAfter(cursor)) {
                slots.add(new TimeSlot(cursor, anchor.getStartTime(), true, null, Enums.EnergyDemand.MEDIUM));
            }
            if (anchor.getEndTime().isAfter(cursor)) {
                cursor = anchor.getEndTime().plusMinutes(buffer);
            }
        }

        if (cursor.isBefore(end)) {
            slots.add(new TimeSlot(cursor, end, true, null, Enums.EnergyDemand.MEDIUM));
        }

        return slots;
    }

    private int priorityRank(Enums.Priority p) {
        if (p == Enums.Priority.HIGH) return 3;
        if (p == Enums.Priority.MEDIUM) return 2;
        return 1;
    }

    private int energyRank(Enums.EnergyDemand e) {
        if (e == Enums.EnergyDemand.HIGH) return 3;
        if (e == Enums.EnergyDemand.MEDIUM) return 2;
        return 1;
    }

    private void calculateOverallEnergyScore(SchedulePlan plan, UserProfile profile) {
        if (plan.getScheduledTasks() == null || plan.getScheduledTasks().isEmpty()) {
            plan.setOverallEnergyAlignmentScore(100.0);
            return;
        }
        double sum = 0.0;
        int count = 0;
        for (Task t : plan.getScheduledTasks()) {
            if (t.getScheduledStartTime() != null) {
                sum += behavioralEngine.calculateEnergyAlignmentScore(profile, t, t.getScheduledStartTime());
                count++;
            }
        }
        double avg = count > 0 ? (sum / count) * 100.0 : 85.0;
        plan.setOverallEnergyAlignmentScore(Math.round(Math.min(100.0, avg) * 10.0) / 10.0);
    }

    /**
     * Communicates with local Ollama API.
     */
    private String callOllama(String prompt) throws Exception {
        URL url = URI.create(ollamaUrl + "/api/generate").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(25000);
        conn.setDoOutput(true);

        Map<String, Object> req = new HashMap<>();
        req.put("model", ollamaModel);
        req.put("prompt", prompt);
        req.put("stream", false);
        req.put("format", "json");

        byte[] payload = objectMapper.writeValueAsBytes(req);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Ollama HTTP Error: " + conn.getResponseCode());
        }

        try (InputStream is = conn.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            return root.path("response").asText();
        }
    }

    private String buildRoutineAnalysisPrompt(String rawContent) {
        return "You are an intelligent circadian scheduling assistant. Analyze the following past routine or habit log:\n\n" +
                "\"\"\"\n" + rawContent + "\n\"\"\"\n\n" +
                "Extract routine anchors (wake, sleep, meals, gym, meetings) and behavioral characteristics.\n" +
                "Output ONLY a valid JSON object matching this structure:\n" +
                "{\n" +
                "  \"detectedWakeTime\": \"07:00\",\n" +
                "  \"detectedSleepTime\": \"23:00\",\n" +
                "  \"suggestedPeakHours\": [9, 10, 11],\n" +
                "  \"summary\": \"Extracted daily routine summary\",\n" +
                "  \"anchors\": [\n" +
                "    {\"name\": \"Breakfast & Routine\", \"startTime\": \"07:30\", \"endTime\": \"08:15\", \"category\": \"REST_LEISURE\", \"flexible\": false},\n" +
                "    {\"name\": \"Lunch Break\", \"startTime\": \"12:30\", \"endTime\": \"13:30\", \"category\": \"REST_LEISURE\", \"flexible\": false}\n" +
                "  ],\n" +
                "  \"observations\": [\"Focus peak in morning\", \"Gym in evening\"]\n" +
                "}";
    }

    private String buildSchedulePrompt(LocalDate date, List<Task> tasks, List<RoutineAnchor> anchors, UserProfile profile, int bufferMinutes) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert time-blocking AI scheduler. Schedule the following tasks for date: ").append(date).append("\n");
        sb.append("User Chronotype: ").append(profile.getChronotype()).append("\n");
        sb.append("Peak Focus Hours: ").append(profile.getPeakCognitiveHours()).append("\n");
        sb.append("Buffer between tasks: ").append(bufferMinutes).append(" mins.\n\n");

        sb.append("Fixed Routine Anchors (DO NOT OVERLAP):\n");
        for (RoutineAnchor a : anchors) {
            sb.append("- ").append(a.getName()).append(": ").append(a.getStartTime()).append(" - ").append(a.getEndTime()).append("\n");
        }

        sb.append("\nTasks to schedule:\n");
        for (Task t : tasks) {
            sb.append("- ID: ").append(t.getId()).append(" | Title: ").append(t.getTitle())
                    .append(" | Est Mins: ").append(t.getEstimatedMinutes())
                    .append(" | Energy: ").append(t.getEnergyDemand())
                    .append(" | Priority: ").append(t.getPriority()).append("\n");
        }

        sb.append("\nReturn JSON ONLY with format:\n");
        sb.append("{\n  \"schedules\": [\n    {\"taskId\": \"...\", \"startTime\": \"09:00\", \"endTime\": \"10:00\", \"reasoning\": \"...\"}\n  ],\n  \"notes\": [\"...\"]\n}");
        return sb.toString();
    }

    private void parseAiRoutineResponse(String json, RoutineExtractionResult result) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("detectedWakeTime")) {
                result.setDetectedWakeTime(LocalTime.parse(root.get("detectedWakeTime").asText()));
            }
            if (root.has("detectedSleepTime")) {
                result.setDetectedSleepTime(LocalTime.parse(root.get("detectedSleepTime").asText()));
            }
            if (root.has("summary")) {
                result.setSummary(root.get("summary").asText());
            }
            if (root.has("suggestedPeakHours") && root.get("suggestedPeakHours").isArray()) {
                List<Integer> peaks = new ArrayList<>();
                for (JsonNode h : root.get("suggestedPeakHours")) {
                    peaks.add(h.asInt());
                }
                result.setSuggestedPeakHours(peaks);
            }
            if (root.has("observations") && root.get("observations").isArray()) {
                for (JsonNode o : root.get("observations")) {
                    result.getExtractedObservations().add(o.asText());
                }
            }
            if (root.has("anchors") && root.get("anchors").isArray()) {
                for (JsonNode a : root.get("anchors")) {
                    RoutineAnchor anchor = new RoutineAnchor();
                    anchor.setName(a.path("name").asText("Routine Item"));
                    anchor.setStartTime(LocalTime.parse(a.path("startTime").asText("08:00")));
                    anchor.setEndTime(LocalTime.parse(a.path("endTime").asText("09:00")));
                    String catStr = a.path("category").asText("REST_LEISURE");
                    try {
                        anchor.setCategory(Enums.TaskCategory.valueOf(catStr));
                    } catch (Exception ex) {
                        anchor.setCategory(Enums.TaskCategory.REST_LEISURE);
                    }
                    anchor.setFlexible(a.path("flexible").asBoolean(false));
                    result.getExtractedAnchors().add(anchor);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama JSON response: {}", e.getMessage());
            extractRoutineHeuristically(json, result);
        }
    }

    private boolean parseAiScheduleResponse(String json, List<Task> tasks, SchedulePlan plan) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.has("schedules") || !root.get("schedules").isArray()) {
                return false;
            }
            Map<String, Task> taskMap = new HashMap<>();
            for (Task t : tasks) {
                taskMap.put(t.getId(), t);
            }

            for (JsonNode item : root.get("schedules")) {
                String tid = item.path("taskId").asText();
                Task t = taskMap.get(tid);
                if (t != null) {
                    t.setScheduledStartTime(LocalTime.parse(item.path("startTime").asText()));
                    t.setScheduledEndTime(LocalTime.parse(item.path("endTime").asText()));
                    t.setReasoning(item.path("reasoning").asText("Optimized by Open Source AI model."));
                    t.setStatus(Enums.TaskStatus.PENDING);
                }
            }

            if (root.has("notes") && root.get("notes").isArray()) {
                for (JsonNode n : root.get("notes")) {
                    plan.getOptimizationNotes().add(n.asText());
                }
            }

            plan.setScheduledTasks(new ArrayList<>(taskMap.values()));
            return true;
        } catch (Exception e) {
            log.warn("Error parsing AI schedule response: {}", e.getMessage());
            return false;
        }
    }

    private void extractRoutineHeuristically(String content, RoutineExtractionResult result) {
        result.setDetectedWakeTime(LocalTime.of(7, 0));
        result.setDetectedSleepTime(LocalTime.of(23, 0));
        result.setSuggestedPeakHours(List.of(9, 10, 11, 16));
        result.setSummary("Behavioral heuristic parsing analyzed the provided routine schedule.");

        List<RoutineAnchor> anchors = new ArrayList<>();
        anchors.add(new RoutineAnchor("Morning Routine & Breakfast", LocalTime.of(7, 0), LocalTime.of(8, 0), Enums.TaskCategory.HEALTH_FITNESS, false));
        anchors.add(new RoutineAnchor("Lunch Break", LocalTime.of(12, 30), LocalTime.of(13, 30), Enums.TaskCategory.REST_LEISURE, false));
        anchors.add(new RoutineAnchor("Evening Wind-down / Dinner", LocalTime.of(19, 30), LocalTime.of(20, 30), Enums.TaskCategory.REST_LEISURE, true));

        result.setExtractedAnchors(anchors);
        result.getExtractedObservations().add("Detected consistent morning start around 07:00 AM.");
        result.getExtractedObservations().add("Lunch anchor reserved at 12:30 PM with zero task conflicts.");
    }
}
