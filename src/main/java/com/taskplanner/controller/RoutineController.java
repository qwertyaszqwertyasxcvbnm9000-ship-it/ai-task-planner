package com.taskplanner.controller;

import com.taskplanner.dto.RoutineExtractionResult;
import com.taskplanner.dto.RoutineUploadRequest;
import com.taskplanner.model.HabitDriftReport;
import com.taskplanner.model.RoutineAnchor;
import com.taskplanner.model.UserProfile;
import com.taskplanner.service.RoutineParserService;
import com.taskplanner.service.TaskService;
import com.taskplanner.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routine")
@CrossOrigin(origins = "*")
public class RoutineController {

    private final UserProfileService profileService;
    private final RoutineParserService parserService;
    private final TaskService taskService;

    public RoutineController(UserProfileService profileService,
                             RoutineParserService parserService,
                             TaskService taskService) {
        this.profileService = profileService;
        this.parserService = parserService;
        this.taskService = taskService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(@RequestBody UserProfile profile) {
        return ResponseEntity.ok(profileService.updateProfile(profile));
    }

    @GetMapping("/anchors")
    public ResponseEntity<List<RoutineAnchor>> getAnchors() {
        return ResponseEntity.ok(profileService.getRoutineAnchors());
    }

    @PostMapping("/anchors")
    public ResponseEntity<RoutineAnchor> saveAnchor(@RequestBody RoutineAnchor anchor) {
        return ResponseEntity.ok(profileService.saveAnchor(anchor));
    }

    @DeleteMapping("/anchors/{id}")
    public ResponseEntity<Void> deleteAnchor(@PathVariable String id) {
        boolean deleted = profileService.deleteAnchor(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<RoutineExtractionResult> uploadPastRoutine(@RequestBody RoutineUploadRequest request) {
        RoutineExtractionResult result = parserService.parseRoutine(request.getFormat(), request.getContent());

        if (request.isAutoApplyToProfile() && result.getExtractedAnchors() != null && !result.getExtractedAnchors().isEmpty()) {
            profileService.replaceAllAnchors(result.getExtractedAnchors());
            UserProfile p = profileService.getProfile();
            if (result.getDetectedWakeTime() != null) p.setWakeTime(result.getDetectedWakeTime());
            if (result.getDetectedSleepTime() != null) p.setSleepTime(result.getDetectedSleepTime());
            if (result.getSuggestedPeakHours() != null && !result.getSuggestedPeakHours().isEmpty()) {
                p.setPeakCognitiveHours(result.getSuggestedPeakHours());
            }
            if (result.getExtractedObservations() != null && !result.getExtractedObservations().isEmpty()) {
                p.getLearnedInsights().addAll(result.getExtractedObservations());
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/drift")
    public ResponseEntity<HabitDriftReport> getHabitDriftReport() {
        return ResponseEntity.ok(taskService.getHabitDriftReport());
    }
}
