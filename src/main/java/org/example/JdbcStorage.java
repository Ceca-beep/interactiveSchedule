package org.example;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JdbcStorage implements Storage {

    // 1. NEW METHOD: Automatically creates all required tables if they are missing
    private void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            // Create LOCATIONS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS locations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    code VARCHAR(255),
                    x INT,
                    y INT
                )
            """);

            // Create COURSES table
            st.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) UNIQUE
                )
            """);

            // Create STUDENTS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    faculty VARCHAR(255)
                )
            """);

            // Create TIMETABLE_ENTRIES table (Links to courses and locations)
            st.execute("""
                CREATE TABLE IF NOT EXISTS timetable_entries (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    course_id INT,
                    type VARCHAR(50),
                    day VARCHAR(50),
                    start_time TIME,
                    duration_minutes INT,
                    location_id INT,
                    FOREIGN KEY (course_id) REFERENCES courses(id),
                    FOREIGN KEY (location_id) REFERENCES locations(id)
                )
            """);
        }
    }

    @Override
    public void save(DataSnapshot data, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            conn.setAutoCommit(false);

            // 2. CALL THE INIT METHOD HERE
            // This guarantees tables exist before we try to delete/insert
            initSchema(conn);

            try (Statement st = conn.createStatement()) {
                // Clear tables (respecting FK order - delete children first)
                st.executeUpdate("DELETE FROM timetable_entries");
                st.executeUpdate("DELETE FROM students");
                st.executeUpdate("DELETE FROM courses"); // Delete courses after entries
                st.executeUpdate("DELETE FROM locations"); // Delete locations after entries

                // Insert locations and keep mapping from generated int id -> String id
                Map<String, Integer> locIdMap = new HashMap<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO locations (name, code, x, y) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    for (Location L : data.getLocations()) {
                        ps.setString(1, L.getName());
                        ps.setString(2, L.getBuilding());
                        ps.setInt(3, L.getX());
                        ps.setInt(4, L.getY());
                        ps.executeUpdate();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) {
                                int gen = gk.getInt(1);
                                locIdMap.put(L.getId(), gen);
                            }
                        }
                    }
                }

                // Insert courses and map by name to id
                Map<String, Integer> courseIdMap = new HashMap<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO courses (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    for (TimetableEntry e : data.getEntries()) {
                        String course = e.getCourseName();
                        if (courseIdMap.containsKey(course)) continue;
                        ps.setString(1, course);
                        ps.executeUpdate();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) courseIdMap.put(course, gk.getInt(1));
                        }
                        ps.clearParameters();
                    }
                }

                // Insert students
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO students (name, faculty) VALUES (?, ?)")) {
                    for (Student s : data.getStudents()) {
                        ps.setString(1, s.getName());
                        ps.setString(2, s.getFaculty());
                        ps.executeUpdate();
                        ps.clearParameters();
                    }
                }

                // Insert timetable entries
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES (?, ?, ?, ?, ?, ?)") ) {
                    for (TimetableEntry e : data.getEntries()) {
                        Integer cid = courseIdMap.get(e.getCourseName());
                        Integer lid = locIdMap.get(e.getLocationId());
                        // Skip if course or location wasn't found (prevents FK errors)
                        if (cid == null || lid == null) continue;

                        ps.setInt(1, cid);
                        ps.setString(2, e.getType().name());
                        ps.setString(3, e.getDay());
                        // Add :00 because SQL Time expects HH:MM:SS
                        ps.setTime(4, Time.valueOf(e.getStartTime() + ":00"));
                        ps.setInt(5, e.getDurationMinutes());
                        ps.setInt(6, lid);
                        ps.executeUpdate();
                        ps.clearParameters();
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public DataSnapshot load(String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // Also init schema on load just in case save() was never called
            initSchema(conn);

            DataSnapshot d = new DataSnapshot();

            // Load locations
            Map<Integer, String> locIdToUuid = new HashMap<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, name, code, x, y FROM locations")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String code = rs.getString("code");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    Location loc = new Location(name, code, x, y);
                    d.getLocations().add(loc);
                    locIdToUuid.put(id, loc.getId());
                }
            }

            // Load courses into a map id->name
            Map<Integer, String> courses = new HashMap<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, name FROM courses")) {
                while (rs.next()) courses.put(rs.getInt("id"), rs.getString("name"));
            }

            // Load timetable entries
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT course_id, type, day, start_time, duration_minutes, location_id FROM timetable_entries")) {
                while (rs.next()) {
                    int courseId = rs.getInt("course_id");
                    String courseName = courses.getOrDefault(courseId, "Unknown");
                    String type = rs.getString("type");
                    String day = rs.getString("day");
                    Time start = rs.getTime("start_time");
                    int dur = rs.getInt("duration_minutes");
                    int locId = rs.getInt("location_id");

                    String startStr = String.format("%02d:%02d", start.toLocalTime().getHour(), start.toLocalTime().getMinute());
                    String locUuid = locIdToUuid.get(locId);
                    if (locUuid == null) continue;
                    TimetableEntry e = new TimetableEntry(courseName, ClassType.valueOf(type), day, startStr, dur, locUuid);
                    d.getEntries().add(e);
                }
            }

            // Load students
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT name, faculty FROM students")) {
                while (rs.next()) d.getStudents().add(new Student(rs.getString("name"), rs.getString("faculty")));
            }

            if (d.getLocations().isEmpty() || d.getEntries().isEmpty()) return DataSnapshot.seed();
            return d;
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    public boolean accountExists(String name, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // Ensure table exists before checking
            initSchema(conn);

            String sql = "SELECT 1 FROM students WHERE name = ?" + (faculty == null || faculty.isBlank() ? " LIMIT 1" : " AND faculty = ? LIMIT 1");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                if (faculty != null && !faculty.isBlank()) ps.setString(2, faculty);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}