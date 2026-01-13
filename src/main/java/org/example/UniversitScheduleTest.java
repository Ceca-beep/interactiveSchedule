package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UniversityScheduleTest {

    @Test
    void testLocationConstructor() {
        Location loc = new Location(1, "Lab 101", 100, -50);

        assertEquals("Lab 101", loc.getName());
        assertEquals(100, loc.getX());
        assertEquals(-50, loc.getY());
    }

    @Test
    void testTimetableEntryConstructor() {
        TimetableEntry entry = new TimetableEntry("Java", "LECTURE", "MONDAY", "08:00", 90, "Room 1");

        assertEquals("Java", entry.getCourseName());
        assertEquals("LECTURE", entry.getType().name());
        assertEquals("Room 1", entry.getLocationId());
    }

    @Test
    void testDirectionsEastAndSouth() {
        Location loc = new Location("Test Room", 50, 20);

        String directions = loc.getDirectionsFromEntrance();

        assertTrue(directions.contains("Go EAST (Right) for 50 meters"));
        assertTrue(directions.contains("go SOUTH (Down) for 20 meters"));
    }

    @Test
    void testDirectionsWestAndNorth() {
        Location loc = new Location("Test Room", -100, -10);
        String directions = loc.getDirectionsFromEntrance();

        assertTrue(directions.contains("Go WEST (Left) for 100 meters"));
        assertTrue(directions.contains("go NORTH (Up) for 10 meters"));
    }

    @Test
    void testDirectionsZeroCoordinates() {
        Location loc = new Location("Main Entrance", 0, 0);
        String directions = loc.getDirectionsFromEntrance();

        assertEquals("You are at the Main Entrance.", directions);
    }
}