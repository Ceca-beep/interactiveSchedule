package org.example;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JdbcStorage implements Storage {

    private void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            // 1. Create Tables (SIMPLIFIED: We deleted the numerical IDs)
            // Now we use the 'name' as the primary way to identify things.

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

            // 2. Run the Auto-Seeder
            seedDatabaseIfEmpty(conn);
        }
    }

    private void seedDatabaseIfEmpty(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            // Check if data exists
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }

            System.out.println("Seeding database (Simple Version without IDs)...");

            // --- A. INSERT LOCATIONS ---
            st.execute("INSERT INTO locations (name, code, x, y) VALUES " +
                    "('Main Entrance', 'A', 0, 0), " +
                    "('CS Building, Room 101', 'C', 180, -60), " +
                    "('CS Building, Room 305', 'C', 210, -40), " +
                    "('CS Building, Lab 1', 'C', 200, -20), " +
                    "('Math Building, Room 12', 'M', -420, -90), " +
                    "('Math Auditorium', 'M', -460, -60), " +
                    "('Psychology Hall, Room 101', 'Psy', -300, 300), " +
                    "('Psychology Lab A', 'Psy', -320, 310), " +
                    "('History Wing, Room 5', 'His', 100, 520), " +
                    "('Archives Room', 'His', 120, 540), " +
                    "('Humanities Building, Hall H1', 'H', 100, 500), " +
                    "('Central Library', 'L', 380, 90), " +
                    "('University Admin', 'U', 40, 240)");

            // --- B. INSERT COURSES ---
            st.execute("INSERT INTO courses (name, faculty) VALUES " +
                    "('Programming Fundamentals', 'Computer Science'), " +
                    "('Data Structures', 'Computer Science'), " +
                    "('Operating Systems', 'Computer Science'), " +
                    "('Algorithms', 'Computer Science'), " +
                    "('Databases', 'Computer Science'), " +
                    "('Intro to Psychology', 'Psychology'), " +
                    "('Cognitive Science', 'Psychology'), " +
                    "('Behavioral Analysis', 'Psychology'), " +
                    "('Clinical Psychology', 'Psychology'), " +
                    "('Social Psychology', 'Psychology'), " +
                    "('World History I', 'History'), " +
                    "('Ancient Civilizations', 'History'), " +
                    "('Modern Europe', 'History'), " +
                    "('History of Art', 'History'), " +
                    "('Research Methods in History', 'History')");

            // --- C. INSERT STUDENTS ---
            st.execute("INSERT INTO students (name, faculty) VALUES " +
                    "('Alex', 'Computer Science'), " +
                    "('Sarah', 'Psychology'), " +
                    "('Mike', 'History')");

            // --- D. INSERT SCHEDULE (Look how clean this is now!) ---
            // We use names directly. No more "SELECT id FROM..." subqueries.

            // CS Schedule
            st.execute("""
                INSERT INTO timetable_entries (course_name, type, day, start_time, duration_minutes, location_name) VALUES
                ('Programming Fundamentals', 'LECTURE', 'MONDAY', '08:00:00', 120, 'CS Building, Room 101'),
                ('Programming Fundamentals', 'LAB', 'MONDAY', '12:00:00', 120, 'CS Building, Lab 1'),
                ('Data Structures', 'LECTURE', 'TUESDAY', '09:00:00', 120, 'CS Building, Room 305'),
                ('Data Structures', 'LAB', 'TUESDAY', '13:00:00', 120, 'CS Building, Lab 1'),
                ('Operating Systems', 'LECTURE', 'WEDNESDAY', '08:30:00', 120, 'CS Building, Room 101'),
                ('Operating Systems', 'LAB', 'WEDNESDAY', '12:30:00', 120, 'CS Building, Lab 1'),
                ('Algorithms', 'LECTURE', 'THURSDAY', '10:00:00', 120, 'CS Building, Room 305'),
                ('Algorithms', 'SEMINAR', 'THURSDAY', '14:00:00', 90, 'CS Building, Room 101'),
                ('Databases', 'LECTURE', 'FRIDAY', '08:00:00', 120, 'CS Building, Room 101'),
                ('Databases', 'LAB', 'FRIDAY', '12:00:00', 120, 'CS Building, Lab 1')
            """);

            // Psychology Schedule
            st.execute("""
                INSERT INTO timetable_entries (course_name, type, day, start_time, duration_minutes, location_name) VALUES
                ('Intro to Psychology', 'LECTURE', 'MONDAY', '10:00:00', 90, 'Psychology Hall, Room 101'),
                ('Intro to Psychology', 'SEMINAR', 'MONDAY', '14:00:00', 60, 'Psychology Lab A'),
                ('Cognitive Science', 'LECTURE', 'TUESDAY', '11:00:00', 90, 'Psychology Hall, Room 101'),
                ('Cognitive Science', 'LAB', 'TUESDAY', '15:00:00', 120, 'Psychology Lab A'),
                ('Behavioral Analysis', 'LECTURE', 'WEDNESDAY', '09:00:00', 90, 'Psychology Hall, Room 101'),
                ('Behavioral Analysis', 'SEMINAR', 'WEDNESDAY', '13:00:00', 60, 'Central Library'),
                ('Clinical Psychology', 'LECTURE', 'THURSDAY', '10:00:00', 120, 'Psychology Hall, Room 101'),
                ('Clinical Psychology', 'SEMINAR', 'THURSDAY', '14:00:00', 90, 'University Admin'),
                ('Social Psychology', 'LECTURE', 'FRIDAY', '11:00:00', 90, 'Psychology Hall, Room 101'),
                ('Social Psychology', 'SEMINAR', 'FRIDAY', '13:00:00', 60, 'Psychology Lab A')
            """);

            // History Schedule
            st.execute("""
                INSERT INTO timetable_entries (course_name, type, day, start_time, duration_minutes, location_name) VALUES
                ('World History I', 'LECTURE', 'MONDAY', '09:00:00', 120, 'History Wing, Room 5'),
                ('World History I', 'SEMINAR', 'MONDAY', '13:00:00', 60, 'Archives Room'),
                ('Ancient Civilizations', 'LECTURE', 'TUESDAY', '10:00:00', 90, 'History Wing, Room 5'),
                ('Ancient Civilizations', 'SEMINAR', 'TUESDAY', '14:00:00', 90, 'Humanities Building, Hall H1'),
                ('Modern Europe', 'LECTURE', 'WEDNESDAY', '11:00:00', 90, 'History Wing, Room 5'),
                ('Modern Europe', 'SEMINAR', 'WEDNESDAY', '15:00:00', 60, 'Archives Room'),
                ('History of Art', 'LECTURE', 'THURSDAY', '09:00:00', 90, 'History Wing, Room 5'),
                ('History of Art', 'SEMINAR', 'THURSDAY', '11:00:00', 60, 'Central Library'),
                ('Research Methods in History', 'LECTURE', 'FRIDAY', '10:00:00', 90, 'History Wing, Room 5'),
                ('Research Methods in History', 'SEMINAR', 'FRIDAY', '14:00:00', 90, 'Archives Room')
            """);

            System.out.println("Seeding complete!");
        }
    }

    @Override
    public void save(DataSnapshot data, String dataSource) throws IOException {
        // Not used
    }

    @Override
    public DataSnapshot load(String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);

            DataSnapshot d = new DataSnapshot();

            // 1. Load Locations and Map Name -> UUID
            Map<String, String> locNameToUuid = new HashMap<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT name, code, x, y FROM locations")) {
                while (rs.next()) {
                    Location loc = new Location(rs.getString("name"), rs.getString("code"), rs.getInt("x"), rs.getInt("y"));
                    d.getLocations().add(loc);
                    // Map the Name to the Java Object's internal UUID
                    locNameToUuid.put(rs.getString("name"), loc.getId());
                }
            }

            // 2. Load Timetable Entries (Using Names!)
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT course_name, type, day, start_time, duration_minutes, location_name FROM timetable_entries")) {
                while (rs.next()) {
                    String courseName = rs.getString("course_name");
                    String startStr = rs.getTime("start_time").toString().substring(0, 5); // HH:MM

                    // Find the UUID using the Location Name
                    // Get the Location Name directly
                    String locationName = rs.getString("location_name");

// Optional: If you strictly need the valid UUID for other logic (like a map view),
// keep the check, but pass the NAME to the entry constructor for display.
                    if (!locNameToUuid.containsKey(locationName)) continue;

                    d.getEntries().add(new TimetableEntry(
                            courseName,
                            ClassType.valueOf(rs.getString("type")),
                            rs.getString("day"),
                            startStr,
                            rs.getInt("duration_minutes"),
                            locationName // <--- FIXED: Now passes "History Wing..." instead of "c8e25..."
                    ));
                }
            }
            return d;
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    public boolean accountExists(String name, String faculty, String dataSource) throws IOException {
        try (Connection conn = DriverManager.getConnection(dataSource)) {
            initSchema(conn);
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
}