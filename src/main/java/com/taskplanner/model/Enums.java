package com.taskplanner.model;

public class Enums {
    public enum TaskCategory {
        DEEP_WORK("Deep Work", "#6366f1", EnergyDemand.HIGH),
        CREATIVE("Creative", "#8b5cf6", EnergyDemand.HIGH),
        ADMINISTRATIVE("Administrative", "#3b82f6", EnergyDemand.LOW),
        HEALTH_FITNESS("Health & Fitness", "#10b981", EnergyDemand.MEDIUM),
        LEARNING("Learning", "#ec4899", EnergyDemand.HIGH),
        ERRAND("Errands & Chores", "#f59e0b", EnergyDemand.LOW),
        REST_LEISURE("Rest & Leisure", "#06b6d4", EnergyDemand.LOW);

        private final String displayName;
        private final String color;
        private final EnergyDemand typicalEnergy;

        TaskCategory(String displayName, String color, EnergyDemand typicalEnergy) {
            this.displayName = displayName;
            this.color = color;
            this.typicalEnergy = typicalEnergy;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
        public EnergyDemand getTypicalEnergy() { return typicalEnergy; }
    }

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public enum EnergyDemand {
        HIGH, MEDIUM, LOW
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, SKIPPED, RESCHEDULED
    }

    public enum FeedbackRating {
        FELT_RUSHED("Rushed - needed more time"),
        JUST_RIGHT("Spot on - perfect time"),
        TOOK_LONGER("Took longer than expected"),
        EASY_FAST("Finished early / easily");

        private final String label;
        FeedbackRating(String label) { this.label = label; }
        public String getLabel() { return label; }
    }
}
