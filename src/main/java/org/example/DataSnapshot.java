package org.example;

import java.io.Serializable;
import java.util.*;

public class DataSnapshot implements Serializable {
    private final List<Student> students = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();
    private final List<TimetableEntry> entries = new ArrayList<>();

    public List<Student> getStudents() { return students; }
    public List<Location> getLocations() { return locations; }
    public List<TimetableEntry> getEntries() { return entries; }

    public Optional<Location> findLocationById(String id) {
        return locations.stream().filter(l -> l.getId().equals(id)).findFirst();
    }
    public Optional<Location> findLocationByName(String name) {
        return locations.stream().filter(l -> l.getName().equalsIgnoreCase(name)).findFirst();
    }


    public static DataSnapshot seed() {
        DataSnapshot d = new DataSnapshot();

        Location mainEntrance = new Location("Main Entrance", "A", 0, 0);
        Location csBuilding   = new Location("CS Building, Room 101", "C", 80, -60);
        Location mathRoom     = new Location("Math Building, Room 12", "M", -200, -100);
        Location northGate    = new Location("North Gate",    "A", 50, 1100);
        Location southGate    = new Location("South Gate",    "A", -40, -1000);
        Location eastGate     = new Location("East Gate",     "A", 1200, 120);
        Location westGate     = new Location("West Gate",     "A", -1200, -150);

        Location library      = new Location("Central Library", "L", 380, 90);
        Location canteen      = new Location("Student Canteen", "F", 220, -180);

        Location cs101        = new Location("CS Building, Room 101", "C", 180, -60);
        Location cs305        = new Location("CS Building, Room 305", "C", 210, -40);
        Location csLab1       = new Location("CS Building, Lab 1",    "C", 200, -20);

        Location math12       = new Location("Math Building, Room 12", "M", -420, -90);
        Location mathAud      = new Location("Math Auditorium",        "M", -460, -60);

        Location physics201   = new Location("Physics Building, Room 201", "P", -300, 300);
        Location humanitiesH1 = new Location("Humanities Building, Hall H1", "H", 100, 520);

        Location admin        = new Location("University Admin", "U", 40, 240);
        Location dormA        = new Location("Dorm A", "D", -800, 650);
        Location dormB        = new Location("Dorm B", "D", 760, -620);

        d.locations.addAll(List.of(
                mainEntrance, northGate, southGate, eastGate, westGate,
                library, canteen,
                cs101, cs305, csLab1,
                math12, mathAud,
                physics201, humanitiesH1,
                admin, dormA, dormB
        ));

        d.students.add(new Student("Student"));

        // MONDAY
        d.entries.add(new TimetableEntry("Programming Fundamentals",
                ClassType.LECTURE, "MONDAY", "08:00", 120, cs101.getId()));

        d.entries.add(new TimetableEntry("Programming Fundamentals",
                ClassType.LAB, "MONDAY", "12:00", 120, csLab1.getId()));

        d.entries.add(new TimetableEntry("Linear Algebra",
                ClassType.LECTURE, "MONDAY", "10:00", 90, mathAud.getId()));

        d.entries.add(new TimetableEntry("Campus Orientation",
                ClassType.SEMINAR, "MONDAY", "16:00", 90, admin.getId()));

        // TUESDAY
        d.entries.add(new TimetableEntry("Data Structures",
                ClassType.LECTURE, "TUESDAY", "09:00", 120, cs305.getId()));

        d.entries.add(new TimetableEntry("Data Structures",
                ClassType.LAB, "TUESDAY", "13:00", 120, csLab1.getId()));

        d.entries.add(new TimetableEntry("Academic Writing",
                ClassType.SEMINAR, "TUESDAY", "15:00", 90, humanitiesH1.getId()));

        // WEDNESDAY
        d.entries.add(new TimetableEntry("Operating Systems",
                ClassType.LECTURE, "WEDNESDAY", "08:30", 120, cs101.getId()));

        d.entries.add(new TimetableEntry("Operating Systems",
                ClassType.LAB, "WEDNESDAY", "12:30", 120, csLab1.getId()));

        d.entries.add(new TimetableEntry("Discrete Math",
                ClassType.LECTURE, "WEDNESDAY", "10:30", 90, math12.getId()));

        d.entries.add(new TimetableEntry("Physics Seminar",
                ClassType.SEMINAR, "WEDNESDAY", "15:30", 90, physics201.getId()));

        // THURSDAY
        d.entries.add(new TimetableEntry("Algorithms",
                ClassType.LECTURE, "THURSDAY", "09:00", 120, cs305.getId()));

        d.entries.add(new TimetableEntry("Algorithms",
                ClassType.LAB, "THURSDAY", "13:00", 120, csLab1.getId()));

        d.entries.add(new TimetableEntry("Ethics in IT",
                ClassType.SEMINAR, "THURSDAY", "16:00", 90, humanitiesH1.getId()));

        // FRIDAY
        d.entries.add(new TimetableEntry("Databases",
                ClassType.LECTURE, "FRIDAY", "08:00", 120, cs101.getId()));

        d.entries.add(new TimetableEntry("Databases",
                ClassType.LAB, "FRIDAY", "12:00", 120, csLab1.getId()));

        d.entries.add(new TimetableEntry("Probability",
                ClassType.LECTURE, "FRIDAY", "10:00", 90, mathAud.getId()));

        d.entries.add(new TimetableEntry("Research Seminar",
                ClassType.SEMINAR, "FRIDAY", "15:00", 90, library.getId()));
        return d;
    }
}
