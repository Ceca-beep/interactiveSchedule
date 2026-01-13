package org.example;

import java.io.Serializable;
import java.util.Objects;


public class TimetableEntry implements Serializable {
    private String courseName;
    private ClassType type;
    private String day;
    private String startTime;
    private int durationMinutes;
    private String locationId;

    public TimetableEntry() {}

    // used in JdbcStorage.java and ScheduleView.java
    public TimetableEntry(String courseName, ClassType type, String day,
                          String startTime, int durationMinutes, String locationId) {

        this.courseName = Objects.requireNonNull(courseName);
        this.type = Objects.requireNonNull(type);
        this.day = Objects.requireNonNull(day).toUpperCase();
        this.startTime = Objects.requireNonNull(startTime);
        this.durationMinutes = durationMinutes;
        this.locationId = Objects.requireNonNull(locationId);
    }

    //used in UniversityScheduleTest.java
    public TimetableEntry(String courseName, String typeStr, String day,
                          String startTime, int durationMinutes, String locationId) {
        this(courseName, ClassType.valueOf(typeStr.toUpperCase()), day, startTime, durationMinutes, locationId);
    }


    public String getCourseName() { return courseName; }

    public ClassType getType() { return type; } // Returns ClassType (Required by ScheduleView)

    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getLocationId() { return locationId; }
}