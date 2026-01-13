package org.example;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JdbcStorage implements Storage {

    // --- 1. INITIALIZE DATABASE ---
    private void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS locations (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE, code VARCHAR(255), x INT, y INT)");
            st.execute("CREATE TABLE IF NOT EXISTS courses (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE, faculty VARCHAR(255))");
            st.execute("CREATE TABLE IF NOT EXISTS students (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE, faculty VARCHAR(255))");
            st.execute("CREATE TABLE IF NOT EXISTS admins (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE, password VARCHAR(255), faculty VARCHAR(255))");

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

    // --- 3. LOAD DATA (Requirement 6) ---
    @Override
    public DataSnapshot load(String dataSource) throws IOException {
        return loadByFaculty("History", dataSource); // Default load
    }

    public DataSnapshot loadByFaculty(String facultyName, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
            DataSnapshot d = new DataSnapshot();

            // Load Locations
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT name, code, x, y FROM locations")) {
                while (rs.next()) {
                    d.getLocations().add(new Location(rs.getString("name"), rs.getString("code"), rs.getInt("x"), rs.getInt("y")));
                }
            }

            // Load Entries using JOIN (Fixes "Missing Rows" issue)
            String sql = """
                SELECT c.name AS course_name, t.type, t.day, t.start_time, t.duration_minutes, l.name AS location_name
                FROM timetable_entries t
                JOIN courses c ON t.course_id = c.id
                JOIN locations l ON t.location_id = l.id
                WHERE c.faculty = ?
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, facultyName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String startStr = rs.getTime("start_time").toString();
                        if (startStr.length() > 5) startStr = startStr.substring(0, 5);

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
            }
            return d;
        } catch (SQLException e) {
            throw new IOException("Database error: " + e.getMessage(), e);
        }
    }

    // --- 4. AUTHENTICATION (Requirement 8) ---
    public boolean accountExists(String name, String faculty, String dataSource) throws IOException {
        if (faculty == null) return false;
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
            String sql = "SELECT 1 FROM students WHERE name = ? AND faculty = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, faculty);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        } catch (SQLException e) { throw new IOException(e); }
    }

    public boolean createAdmin(String name, String password, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM admins WHERE name = ?")) {
                check.setString(1, name);
                if (check.executeQuery().next()) return false;
            }
            try (PreparedStatement insert = conn.prepareStatement("INSERT INTO admins (name, password, faculty) VALUES (?, ?, ?)")) {
                insert.setString(1, name);
                insert.setString(2, password);
                insert.setString(3, faculty);
                insert.executeUpdate();
                return true;
            }
        } catch (SQLException e) { throw new IOException(e); }
    }

    public boolean authenticateAdmin(String name, String password, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM admins WHERE name = ? AND password = ? AND faculty = ?")) {
                ps.setString(1, name); ps.setString(2, password); ps.setString(3, faculty);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        } catch (SQLException e) { throw new IOException(e); }
    }

    // --- 5. MODIFICATION METHODS (Requirement 7 & 9) ---

    // 1. YOUR ORIGINAL METHOD (Fixed for DB IDs, keeps exact signature)
    public void addTimetableEntry(String courseName, String type, String day, String startTime, int duration, String locationName, int x, int y, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // A. Get or Create Course ID
            int courseId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM courses WHERE name = ?")) {
                ps.setString(1, courseName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    courseId = rs.getInt("id");
                } else {
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO courses (name, faculty) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        insert.setString(1, courseName);
                        insert.setString(2, faculty);
                        insert.executeUpdate();
                        ResultSet gen = insert.getGeneratedKeys();
                        gen.next();
                        courseId = gen.getInt(1);
                    }
                }
            }

            // B. Get or Create Location ID (AND UPDATE COORDINATES)
            int locationId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM locations WHERE name = ?")) {
                ps.setString(1, locationName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    locationId = rs.getInt("id");

                    // --- CRITICAL FIX: UPDATE coordinates for existing location ---
                    try (PreparedStatement updateLoc = conn.prepareStatement("UPDATE locations SET x = ?, y = ? WHERE id = ?")) {
                        updateLoc.setInt(1, x);
                        updateLoc.setInt(2, y);
                        updateLoc.setInt(3, locationId);
                        updateLoc.executeUpdate();
                    }
                    // -------------------------------------------------------------

                } else {
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO locations (name, code, x, y) VALUES (?, 'GEN', ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        insert.setString(1, locationName);
                        insert.setInt(2, x);
                        insert.setInt(3, y);
                        insert.executeUpdate();
                        ResultSet gen = insert.getGeneratedKeys();
                        gen.next();
                        locationId = gen.getInt(1);
                    }
                }
            }

            // C. Insert the Entry
            String sql = "INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, courseId);
                ps.setString(2, type);
                ps.setString(3, day.toUpperCase());
                ps.setString(4, startTime);
                ps.setInt(5, duration);
                ps.setInt(6, locationId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to add entry: " + e.getMessage(), e);
        }
    }

    // 2. NEW: EDIT ENTRY (Renaming & Changing Type)
    public void updateTimetableEntry(String oldCourseName, String day, String startTime,
                                     String newCourseName, String newType,
                                     String locationName, int x, int y, // NEW: Coordinates
                                     String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // 1. Get/Create Course IDs
            int oldCourseId = getOrInsertCourse(conn, oldCourseName, faculty); // Helper from before
            int newCourseId = getOrInsertCourse(conn, newCourseName, faculty);

            // 2. Update Location Coordinates (The "Directions" Fix)
            int locationId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM locations WHERE name = ?")) {
                ps.setString(1, locationName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    locationId = rs.getInt("id");
                    // Update existing location coords
                    try (PreparedStatement upLoc = conn.prepareStatement("UPDATE locations SET x = ?, y = ? WHERE id = ?")) {
                        upLoc.setInt(1, x);
                        upLoc.setInt(2, y);
                        upLoc.setInt(3, locationId);
                        upLoc.executeUpdate();
                    }
                } else {
                    // Create new if renaming location
                    locationId = getOrInsertLocation(conn, locationName, x, y);
                }
            }

            // 3. Update the Entry
            String sql = "UPDATE timetable_entries SET course_id = ?, type = ?, location_id = ? WHERE day = ? AND start_time = ? AND course_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newCourseId);
                ps.setString(2, newType);
                ps.setInt(3, locationId);
                ps.setString(4, day);
                ps.setString(5, startTime);
                ps.setInt(6, oldCourseId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IOException("Update failed: " + e.getMessage(), e);
        }
    }

    // --- NEW: DELETE ENTRY METHOD ---
    public void deleteTimetableEntry(String courseName, String day, String startTime, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // We need the course ID to delete the correct entry
            int courseId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM courses WHERE name = ?")) {
                ps.setString(1, courseName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) courseId = rs.getInt("id");
            }

            if (courseId != -1) {
                String sql = "DELETE FROM timetable_entries WHERE course_id = ? AND day = ? AND start_time = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, courseId);
                    ps.setString(2, day);
                    ps.setString(3, startTime);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Delete failed: " + e.getMessage(), e);
        }
    }

    // 3. NEW: AUTO-CREATE STUDENT (Fixes Login)
    public void createStudentIfNotExists(String name, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM students WHERE name = ?")) {
                check.setString(1, name);
                if (check.executeQuery().next()) return; // Already exists
            }
            try (PreparedStatement insert = conn.prepareStatement("INSERT INTO students (name, faculty) VALUES (?, ?)")) {
                insert.setString(1, name);
                insert.setString(2, faculty);
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IOException("Auto-create failed: " + e.getMessage(), e);
        }
    }

    // --- HELPER UPDATES ---
    private int getOrInsertCourse(Connection conn, String name, String faculty) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM courses WHERE name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        // Insert if missing
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (name, faculty) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, faculty);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    private int getOrInsertLocation(Connection conn, String name, int x, int y) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM locations WHERE name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        // Insert if missing
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO locations (name, code, x, y) VALUES (?, 'GEN', ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    public void renameCourse(String oldName, String newName, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // Simple rename of the course record cascades to entries
            try (PreparedStatement ps = conn.prepareStatement("UPDATE courses SET name = ? WHERE name = ?")) {
                ps.setString(1, newName);
                ps.setString(2, oldName);
                ps.executeUpdate();
            }
        } catch (SQLException e) { throw new IOException(e); }
    }

    public void deleteCourse(String courseName, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            // First find the ID
            int courseId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM courses WHERE name = ?")) {
                ps.setString(1, courseName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) courseId = rs.getInt("id");
            }

            if (courseId != -1) {
                // Delete entries first (constraint)
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM timetable_entries WHERE course_id = ?")) {
                    ps.setInt(1, courseId);
                    ps.executeUpdate();
                }
                // Delete course
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE id = ?")) {
                    ps.setInt(1, courseId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) { throw new IOException(e); }
    }

    @Override public void save(DataSnapshot data, String ds) throws IOException {}
}