package org.example;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JdbcStorage implements Storage {

    // --- 1. INITIALIZE DATABASE ---
    private void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS locations (name VARCHAR(255) PRIMARY KEY, code VARCHAR(255), x INT, y INT)");
            st.execute("CREATE TABLE IF NOT EXISTS courses (name VARCHAR(255) PRIMARY KEY, faculty VARCHAR(255))");
            st.execute("CREATE TABLE IF NOT EXISTS students (name VARCHAR(255) PRIMARY KEY, faculty VARCHAR(255))");

            st.execute("""
                CREATE TABLE IF NOT EXISTS timetable_entries (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    course_name VARCHAR(255),
                    type VARCHAR(50),
                    day VARCHAR(50),
                    start_time TIME,
                    duration_minutes INT,
                    location_name VARCHAR(255),
                    FOREIGN KEY (course_name) REFERENCES courses(name),
                    FOREIGN KEY (location_name) REFERENCES locations(name)
                )
            """);
        }
    }

    // --- 2. LOAD DATA ---
    @Override
    public DataSnapshot load(String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);

            DataSnapshot d = new DataSnapshot();

            // Load Locations
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT name, code, x, y FROM locations")) {
                while (rs.next()) {
                    d.getLocations().add(new Location(rs.getString("name"), rs.getString("code"), rs.getInt("x"), rs.getInt("y")));
                }
            }

            // Load Entries
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT course_name, type, day, start_time, duration_minutes, location_name FROM timetable_entries")) {
                while (rs.next()) {
                    String startStr = rs.getTime("start_time").toString();
                    if(startStr.length() > 5) startStr = startStr.substring(0, 5); // Trim seconds

                    d.getEntries().add(new TimetableEntry(
                            rs.getString("course_name"),
                            ClassType.valueOf(rs.getString("type")),
                            rs.getString("day"),
                            startStr,
                            rs.getInt("duration_minutes"),
                            rs.getString("location_name")
                    ));
                }
            }
            return d;
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    // --- 3. HELPER METHODS ---
    public boolean accountExists(String name, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
            if (faculty == null) return true; // Admin check

            String sql = "SELECT 1 FROM students WHERE name = ? AND faculty = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, faculty);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    public Map<String, String> getCourseFaculties(String dataSource) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT name, faculty FROM courses")) {
                while(rs.next()) {
                    map.put(rs.getString("name"), rs.getString("faculty"));
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return map;
    }

    // --- 4. MODIFICATION METHODS ---

    // *** THIS IS THE CRITICAL FIX FOR YOUR ERROR ***
    public void addTimetableEntry(String courseName, String type, String day, String startTime, int duration, String locationName, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {

            // A. Ensure COURSE exists
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM courses WHERE name = ?")) {
                check.setString(1, courseName);
                if (!check.executeQuery().next()) {
                    try (PreparedStatement insertCourse = conn.prepareStatement("INSERT INTO courses (name, faculty) VALUES (?, ?)")) {
                        insertCourse.setString(1, courseName);
                        insertCourse.setString(2, faculty);
                        insertCourse.executeUpdate();
                    }
                }
            }

            // B. Ensure LOCATION exists (Fixes the Foreign Key Error)
            try (PreparedStatement checkLoc = conn.prepareStatement("SELECT 1 FROM locations WHERE name = ?")) {
                checkLoc.setString(1, locationName);
                if (!checkLoc.executeQuery().next()) {
                    // Create new location automatically
                    try (PreparedStatement insertLoc = conn.prepareStatement("INSERT INTO locations (name, code, x, y) VALUES (?, 'GEN', 0, 0)")) {
                        insertLoc.setString(1, locationName);
                        insertLoc.executeUpdate();
                    }
                }
            }

            // C. Insert Entry
            String sql = "INSERT INTO timetable_entries (course_name, type, day, start_time, duration_minutes, location_name) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, courseName);
                ps.setString(2, type);
                ps.setString(3, day);
                ps.setString(4, startTime);
                ps.setInt(5, duration);
                ps.setString(6, locationName);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IOException("Error adding entry: " + e.getMessage(), e);
        }
    }

    public void renameCourse(String oldName, String newName, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            conn.setAutoCommit(false);
            try {
                String faculty = "General";
                try (PreparedStatement ps = conn.prepareStatement("SELECT faculty FROM courses WHERE name = ?")) {
                    ps.setString(1, oldName);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) faculty = rs.getString("faculty");
                }

                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (name, faculty) VALUES (?, ?)")) {
                    ps.setString(1, newName);
                    ps.setString(2, faculty);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("UPDATE timetable_entries SET course_name = ? WHERE course_name = ?")) {
                    ps.setString(1, newName);
                    ps.setString(2, oldName);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE name = ?")) {
                    ps.setString(1, oldName);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new IOException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    public void deleteCourse(String courseName, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM timetable_entries WHERE course_name = ?")) {
                    ps.setString(1, courseName);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE name = ?")) {
                    ps.setString(1, courseName);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new IOException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override public void save(DataSnapshot data, String dataSource) throws IOException {}
}