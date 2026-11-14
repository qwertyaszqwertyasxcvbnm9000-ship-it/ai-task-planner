package com.taskplanner.service;

import com.taskplanner.model.Enums;
import com.taskplanner.model.RoutineAnchor;
import com.taskplanner.model.UserProfile;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserProfileService {

    private final UserProfile userProfile;
    private final Map<String, RoutineAnchor> anchorsMap = new ConcurrentHashMap<>();

    public UserProfileService() {
        this.userProfile = new UserProfile();
        initDefaultAnchors();
    }

    private void initDefaultAnchors() {
        RoutineAnchor wakeMorning = new RoutineAnchor("Morning Routine & Breakfast", LocalTime.of(7, 0), LocalTime.of(8, 0), Enums.TaskCategory.HEALTH_FITNESS, false);
        RoutineAnchor lunch = new RoutineAnchor("Lunch & Walk", LocalTime.of(12, 30), LocalTime.of(13, 30), Enums.TaskCategory.REST_LEISURE, false);
        RoutineAnchor gym = new RoutineAnchor("Evening Workout", LocalTime.of(17, 30), LocalTime.of(18, 30), Enums.TaskCategory.HEALTH_FITNESS, true);
        RoutineAnchor dinner = new RoutineAnchor("Dinner & Family", LocalTime.of(19, 30), LocalTime.of(20, 30), Enums.TaskCategory.REST_LEISURE, false);

        anchorsMap.put(wakeMorning.getId(), wakeMorning);
        anchorsMap.put(lunch.getId(), lunch);
        anchorsMap.put(gym.getId(), gym);
        anchorsMap.put(dinner.getId(), dinner);
    }

    public UserProfile getProfile() {
        return userProfile;
    }

    public UserProfile updateProfile(UserProfile updated) {
        if (updated.getWakeTime() != null) userProfile.setWakeTime(updated.getWakeTime());
        if (updated.getSleepTime() != null) userProfile.setSleepTime(updated.getSleepTime());
        if (updated.getPreferredLunchTime() != null) userProfile.setPreferredLunchTime(updated.getPreferredLunchTime());
        if (updated.getChronotype() != null) userProfile.setChronotype(updated.getChronotype());
        if (updated.getDefaultBufferMinutes() > 0) userProfile.setDefaultBufferMinutes(updated.getDefaultBufferMinutes());
        if (updated.getPeakCognitiveHours() != null) userProfile.setPeakCognitiveHours(updated.getPeakCognitiveHours());
        return userProfile;
    }

    public List<RoutineAnchor> getRoutineAnchors() {
        List<RoutineAnchor> list = new ArrayList<>(anchorsMap.values());
        list.sort(Comparator.comparing(RoutineAnchor::getStartTime));
        return list;
    }

    public RoutineAnchor saveAnchor(RoutineAnchor anchor) {
        if (anchor.getId() == null || anchor.getId().isEmpty()) {
            anchor.setId(UUID.randomUUID().toString());
        }
        anchorsMap.put(anchor.getId(), anchor);
        return anchor;
    }

    public boolean deleteAnchor(String id) {
        return anchorsMap.remove(id) != null;
    }

    public void replaceAllAnchors(List<RoutineAnchor> newAnchors) {
        anchorsMap.clear();
        if (newAnchors != null) {
            for (RoutineAnchor a : newAnchors) {
                if (a.getId() == null || a.getId().isEmpty()) {
                    a.setId(UUID.randomUUID().toString());
                }
                anchorsMap.put(a.getId(), a);
            }
        }
    }
}
