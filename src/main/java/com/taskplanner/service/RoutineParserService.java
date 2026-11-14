package com.taskplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskplanner.dto.RoutineExtractionResult;
import com.taskplanner.model.Enums;
import com.taskplanner.model.RoutineAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RoutineParserService {

    private static final Logger log = LoggerFactory.getLogger(RoutineParserService.class);
    private final ObjectMapper objectMapper;
    private final OpenSourceAiService aiService;

    public RoutineParserService(ObjectMapper objectMapper, OpenSourceAiService aiService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    public RoutineExtractionResult parseRoutine(String format, String content) {
        if (content == null || content.trim().isEmpty()) {
            RoutineExtractionResult empty = new RoutineExtractionResult();
            empty.setSummary("No routine content provided.");
            return empty;
        }

        String fmt = format != null ? format.toUpperCase() : "AUTO";

        if ("AUTO".equals(fmt)) {
            String trimmed = content.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                fmt = "JSON";
            } else if (trimmed.contains("BEGIN:VCALENDAR") || trimmed.contains("BEGIN:VEVENT")) {
                fmt = "ICS";
            } else if (trimmed.contains(",") && (trimmed.contains("\n") || trimmed.contains("\r"))) {
                fmt = "CSV";
            } else {
                fmt = "TEXT";
            }
        }

        switch (fmt) {
            case "CSV":
                return parseCsv(content);
            case "JSON":
                return parseJson(content);
            case "ICS":
                return parseIcs(content);
            case "TEXT":
            default:
                return aiService.analyzeRoutineWithAi(content);
        }
    }

    public RoutineExtractionResult parseCsv(String csvContent) {
        RoutineExtractionResult result = new RoutineExtractionResult();
        List<RoutineAnchor> anchors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (isHeader && (line.toLowerCase().contains("name") || line.toLowerCase().contains("start"))) {
                    isHeader = false;
                    continue;
                }
                isHeader = false;

                String[] cols = line.split(",");
                if (cols.length >= 3) {
                    String name = cols[0].trim();
                    LocalTime start = parseFlexibleTime(cols[1].trim());
                    LocalTime end = parseFlexibleTime(cols[2].trim());
                    Enums.TaskCategory category = Enums.TaskCategory.REST_LEISURE;
                    if (cols.length >= 4) {
                        try {
                            category = Enums.TaskCategory.valueOf(cols[3].trim().toUpperCase().replace(" ", "_"));
                        } catch (Exception ignored) {}
                    }
                    boolean flexible = false;
                    if (cols.length >= 5) {
                        flexible = Boolean.parseBoolean(cols[4].trim());
                    }

                    if (start != null && end != null) {
                        anchors.add(new RoutineAnchor(name, start, end, category, flexible));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse CSV routine: {}", e.getMessage());
        }

        result.setExtractedAnchors(anchors);
        result.setEngineUsed("CSV_PARSER");
        result.setSummary(String.format("Successfully parsed %d recurring routine anchors from CSV.", anchors.size()));
        detectBoundariesFromAnchors(anchors, result);
        return result;
    }

    public RoutineExtractionResult parseJson(String jsonContent) {
        RoutineExtractionResult result = new RoutineExtractionResult();
        List<RoutineAnchor> anchors = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode arrayNode = root.isArray() ? root : (root.has("anchors") ? root.get("anchors") : null);

            if (arrayNode != null && arrayNode.isArray()) {
                for (JsonNode item : arrayNode) {
                    String name = item.path("name").asText("Routine Block");
                    LocalTime start = parseFlexibleTime(item.path("startTime").asText("08:00"));
                    LocalTime end = parseFlexibleTime(item.path("endTime").asText("09:00"));
                    String catStr = item.path("category").asText("REST_LEISURE");
                    Enums.TaskCategory cat;
                    try {
                        cat = Enums.TaskCategory.valueOf(catStr.toUpperCase().replace(" ", "_"));
                    } catch (Exception ex) {
                        cat = Enums.TaskCategory.REST_LEISURE;
                    }
                    boolean flexible = item.path("flexible").asBoolean(false);
                    if (start != null && end != null) {
                        anchors.add(new RoutineAnchor(name, start, end, cat, flexible));
                    }
                }
            }

            if (root.has("wakeTime")) {
                result.setDetectedWakeTime(parseFlexibleTime(root.get("wakeTime").asText()));
            }
            if (root.has("sleepTime")) {
                result.setDetectedSleepTime(parseFlexibleTime(root.get("sleepTime").asText()));
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON routine: {}", e.getMessage());
        }

        result.setExtractedAnchors(anchors);
        result.setEngineUsed("JSON_PARSER");
        result.setSummary(String.format("Successfully parsed %d routine anchors from JSON.", anchors.size()));
        detectBoundariesFromAnchors(anchors, result);
        return result;
    }

    public RoutineExtractionResult parseIcs(String icsContent) {
        RoutineExtractionResult result = new RoutineExtractionResult();
        List<RoutineAnchor> anchors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(icsContent))) {
            String line;
            String currentSummary = null;
            LocalTime currentStart = null;
            LocalTime currentEnd = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("BEGIN:VEVENT")) {
                    currentSummary = "Calendar Event";
                    currentStart = null;
                    currentEnd = null;
                } else if (line.startsWith("SUMMARY:")) {
                    currentSummary = line.substring(8).trim();
                } else if (line.startsWith("DTSTART")) {
                    currentStart = extractTimeFromIcsDate(line);
                } else if (line.startsWith("DTEND")) {
                    currentEnd = extractTimeFromIcsDate(line);
                } else if (line.startsWith("END:VEVENT")) {
                    if (currentSummary != null && currentStart != null && currentEnd != null) {
                        Enums.TaskCategory cat = classifyEventName(currentSummary);
                        anchors.add(new RoutineAnchor(currentSummary, currentStart, currentEnd, cat, false));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse ICS routine: {}", e.getMessage());
        }

        result.setExtractedAnchors(anchors);
        result.setEngineUsed("ICS_CALENDAR_PARSER");
        result.setSummary(String.format("Successfully imported %d calendar events as routine anchors.", anchors.size()));
        detectBoundariesFromAnchors(anchors, result);
        return result;
    }

    private LocalTime extractTimeFromIcsDate(String line) {
        // e.g. DTSTART:20261114T093000Z or DTSTART;TZID=...:20261114T093000
        int colonIdx = line.lastIndexOf(':');
        if (colonIdx == -1) colonIdx = 0;
        int tIdx = line.indexOf('T', colonIdx);
        if (tIdx != -1 && line.length() >= tIdx + 5) {
            try {
                int hour = Integer.parseInt(line.substring(tIdx + 1, tIdx + 3));
                int min = Integer.parseInt(line.substring(tIdx + 3, tIdx + 5));
                return LocalTime.of(hour, min);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Enums.TaskCategory classifyEventName(String summary) {
        String s = summary.toLowerCase();
        if (s.contains("gym") || s.contains("workout") || s.contains("run") || s.contains("yoga")) return Enums.TaskCategory.HEALTH_FITNESS;
        if (s.contains("lunch") || s.contains("dinner") || s.contains("breakfast") || s.contains("coffee")) return Enums.TaskCategory.REST_LEISURE;
        if (s.contains("meeting") || s.contains("sync") || s.contains("standup") || s.contains("call")) return Enums.TaskCategory.ADMINISTRATIVE;
        if (s.contains("code") || s.contains("develop") || s.contains("write") || s.contains("study")) return Enums.TaskCategory.DEEP_WORK;
        return Enums.TaskCategory.REST_LEISURE;
    }

    private LocalTime parseFlexibleTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        String clean = str.trim().toUpperCase();

        try {
            if (clean.matches("\\d{1,2}:\\d{2}")) {
                return LocalTime.parse(clean, DateTimeFormatter.ofPattern("H:mm"));
            }
            if (clean.contains("AM") || clean.contains("PM")) {
                Pattern p = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(AM|PM)");
                Matcher m = p.matcher(clean);
                if (m.find()) {
                    int hour = Integer.parseInt(m.group(1));
                    int min = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
                    String meridiem = m.group(3);
                    if ("PM".equals(meridiem) && hour < 12) hour += 12;
                    if ("AM".equals(meridiem) && hour == 12) hour = 0;
                    return LocalTime.of(hour, min);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void detectBoundariesFromAnchors(List<RoutineAnchor> anchors, RoutineExtractionResult result) {
        if (anchors.isEmpty()) return;

        LocalTime earliest = LocalTime.of(23, 59);
        LocalTime latest = LocalTime.of(0, 0);

        for (RoutineAnchor a : anchors) {
            if (a.getStartTime().isBefore(earliest)) earliest = a.getStartTime();
            if (a.getEndTime().isAfter(latest)) latest = a.getEndTime();
        }

        if (result.getDetectedWakeTime() == null) {
            result.setDetectedWakeTime(earliest.minusMinutes(45));
        }
        if (result.getDetectedSleepTime() == null) {
            result.setDetectedSleepTime(latest.plusMinutes(90));
        }
        result.setSuggestedPeakHours(List.of(9, 10, 11, 16));
    }
}
