package org.example;

import java.io.Serializable;
import java.util.*;

public class DataSnapshot implements Serializable {
    private final List<Student> students = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();
    private final List<TimetableEntry> entries = new ArrayList<>();

    public List<Student> getStudents() {
        return students;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<TimetableEntry> getEntries() {
        return entries;
    }

    public Optional<Location> findLocationById(String id) {
        return locations.stream().filter(l -> l.getId().equals(id)).findFirst();
    }

    public Optional<Location> findLocationByName(String name) {
        return locations.stream().filter(l -> l.getName().equalsIgnoreCase(name)).findFirst();
    }
}