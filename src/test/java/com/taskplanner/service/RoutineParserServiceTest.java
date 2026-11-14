package com.taskplanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskplanner.dto.RoutineExtractionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoutineParserServiceTest {

    private RoutineParserService parserService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        BehavioralEngine engine = new BehavioralEngine();
        OpenSourceAiService aiService = new OpenSourceAiService(mapper, engine);
        parserService = new RoutineParserService(mapper, aiService);
    }

    @Test
    void testParseCsvRoutine() {
        String csv = "Name,StartTime,EndTime,Category,Flexible\n" +
                "Morning Jog,07:00,07:45,HEALTH_FITNESS,false\n" +
                "Lunch Break,12:30,13:30,REST_LEISURE,false\n" +
                "Evening Reading,20:00,21:00,LEARNING,true\n";

        RoutineExtractionResult result = parserService.parseRoutine("CSV", csv);
        assertNotNull(result);
        assertEquals(3, result.getExtractedAnchors().size());
        assertEquals("Morning Jog", result.getExtractedAnchors().get(0).getName());
        assertEquals(7, result.getExtractedAnchors().get(0).getStartTime().getHour());
    }

    @Test
    void testParseIcsRoutine() {
        String ics = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "BEGIN:VEVENT\n" +
                "SUMMARY:Engineering Standup\n" +
                "DTSTART:20261114T093000\n" +
                "DTEND:20261114T100000\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";

        RoutineExtractionResult result = parserService.parseRoutine("ICS", ics);
        assertNotNull(result);
        assertEquals(1, result.getExtractedAnchors().size());
        assertEquals("Engineering Standup", result.getExtractedAnchors().get(0).getName());
        assertEquals(9, result.getExtractedAnchors().get(0).getStartTime().getHour());
        assertEquals(30, result.getExtractedAnchors().get(0).getStartTime().getMinute());
    }
}
