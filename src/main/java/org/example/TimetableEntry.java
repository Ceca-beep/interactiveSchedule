package org.example;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TimetableEntry implements Serializable {
    private String id;
    private String courseName;
    private ClassType type;
    private String day;
    private String startTime;
    private int durationMinutes;
    private String locationId;

    public TimetableEntry() {}
    public TimetableEntry(String courseName, ClassType type, String day,
                          String startTime, int durationMinutes, String locationId) {
        this.id = UUID.randomUUID().toString();
        this.courseName = Objects.requireNonNull(courseName);
        this.type = Objects.requireNonNull(type);
        this.day = Objects.requireNonNull(day).toUpperCase();
        this.startTime = Objects.requireNonNull(startTime);
        this.durationMinutes = durationMinutes;
        this.locationId = Objects.requireNonNull(locationId);
    }

    public String getId() { return id; }
    public String getCourseName() { return courseName; }
    public ClassType getType() { return type; }
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getLocationId() { return locationId; }
}
