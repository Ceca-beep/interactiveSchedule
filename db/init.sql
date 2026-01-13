CREATE DATABASE IF NOT EXISTS university_schedule
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;
USE university_schedule;

-- 1. LOCATIONS TABLE
CREATE TABLE IF NOT EXISTS locations (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL,
                                         code VARCHAR(10),
                                         x INT,
                                         y INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. COURSES TABLE (Now with 'faculty' column!)
CREATE TABLE IF NOT EXISTS courses (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL UNIQUE,
                                       faculty VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. TIMETABLE ENTRIES TABLE
CREATE TABLE IF NOT EXISTS timetable_entries (
                                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                                 course_id INT NOT NULL,
                                                 type ENUM('LECTURE','LAB','SEMINAR') NOT NULL,
                                                 day ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY') NOT NULL,
                                                 start_time TIME NOT NULL,
                                                 duration_minutes INT NOT NULL,
                                                 location_id INT NOT NULL,
                                                 FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                                                 FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. STUDENTS TABLE (Now with 'faculty' column!)
CREATE TABLE IF NOT EXISTS students (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
                                        faculty VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================
--                 SEED DATA
-- =============================================

-- A. Insert Locations
INSERT INTO locations (name, code, x, y) VALUES
                                             ('Main Entrance', 'A', 0, 0),
                                             ('CS Building, Room 101', 'C', 180, -60),
                                             ('CS Building, Room 305', 'C', 210, -40),
                                             ('CS Building, Lab 1', 'C', 200, -20),
                                             ('Math Building, Room 12', 'M', -420, -90),
                                             ('Math Auditorium', 'M', -460, -60),
                                             ('Psychology Hall, Room 101', 'Psy', -300, 300),
                                             ('Psychology Lab A', 'Psy', -320, 310),
                                             ('History Wing, Room 5', 'His', 100, 520),
                                             ('Archives Room', 'His', 120, 540),
                                             ('Humanities Building, Hall H1', 'H', 100, 500),
                                             ('Central Library', 'L', 380, 90),
                                             ('University Admin', 'U', 40, 240);

-- B. Insert Courses (Grouped by Faculty)
INSERT INTO courses (name, faculty) VALUES
                                        -- Computer Science Courses
                                        ('Programming Fundamentals', 'Computer Science'),
                                        ('Data Structures', 'Computer Science'),
                                        ('Operating Systems', 'Computer Science'),
                                        ('Algorithms', 'Computer Science'),
                                        ('Databases', 'Computer Science'),

                                        -- Psychology Courses
                                        ('Intro to Psychology', 'Psychology'),
                                        ('Cognitive Science', 'Psychology'),
                                        ('Behavioral Analysis', 'Psychology'),
                                        ('Clinical Psychology', 'Psychology'),
                                        ('Social Psychology', 'Psychology'),

                                        -- History Courses
                                        ('World History I', 'History'),
                                        ('Ancient Civilizations', 'History'),
                                        ('Modern Europe', 'History'),
                                        ('History of Art', 'History'),
                                        ('Research Methods in History', 'History');

-- C. Insert Students (The 3 Users you requested)
INSERT INTO students (name, faculty) VALUES
                                         ('Alex', 'Computer Science'),
                                         ('Sarah', 'Psychology'),
                                         ('Mike', 'History');


-- D. Insert Schedule (5 Lectures + 5 Labs/Seminars per Faculty)

-- ================= COMPUTER SCIENCE SCHEDULE =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    -- 1. Programming Fundamentals
                                                                                                    ((SELECT id FROM courses WHERE name='Programming Fundamentals'), 'LECTURE', 'MONDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Programming Fundamentals'), 'LAB', 'MONDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
                                                                                                    -- 2. Data Structures
                                                                                                    ((SELECT id FROM courses WHERE name='Data Structures'), 'LECTURE', 'TUESDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 305')),
                                                                                                    ((SELECT id FROM courses WHERE name='Data Structures'), 'LAB', 'TUESDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
                                                                                                    -- 3. Operating Systems
                                                                                                    ((SELECT id FROM courses WHERE name='Operating Systems'), 'LECTURE', 'WEDNESDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Operating Systems'), 'LAB', 'WEDNESDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
                                                                                                    -- 4. Algorithms
                                                                                                    ((SELECT id FROM courses WHERE name='Algorithms'), 'LECTURE', 'THURSDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 305')),
                                                                                                    ((SELECT id FROM courses WHERE name='Algorithms'), 'SEMINAR', 'THURSDAY', '14:40:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
                                                                                                    -- 5. Databases
                                                                                                    ((SELECT id FROM courses WHERE name='Databases'), 'LECTURE', 'FRIDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Databases'), 'LAB', 'FRIDAY', '16:20:00', 90, (SELECT id FROM locations WHERE name='CS Building, Lab 1'));


-- ================= PSYCHOLOGY SCHEDULE =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    -- 1. Intro to Psychology
                                                                                                    ((SELECT id FROM courses WHERE name='Intro to Psychology'), 'LECTURE', 'MONDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Intro to Psychology'), 'SEMINAR', 'MONDAY', '14:40:00', 90, (SELECT id FROM locations WHERE name='Psychology Lab A')),
                                                                                                    -- 2. Cognitive Science
                                                                                                    ((SELECT id FROM courses WHERE name='Cognitive Science'), 'LECTURE', 'TUESDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Cognitive Science'), 'LAB', 'TUESDAY', '16:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Lab A')),
                                                                                                    -- 3. Behavioral Analysis
                                                                                                    ((SELECT id FROM courses WHERE name='Behavioral Analysis'), 'LECTURE', 'WEDNESDAY', '09:50:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Behavioral Analysis'), 'SEMINAR', 'WEDNESDAY', '18:00:00', 90, (SELECT id FROM locations WHERE name='Central Library')),
                                                                                                    -- 4. Clinical Psychology
                                                                                                    ((SELECT id FROM courses WHERE name='Clinical Psychology'), 'LECTURE', 'THURSDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Clinical Psychology'), 'SEMINAR', 'THURSDAY', '14:40:00', 90, (SELECT id FROM locations WHERE name='University Admin')),
                                                                                                    -- 5. Social Psychology
                                                                                                    ((SELECT id FROM courses WHERE name='Social Psychology'), 'LECTURE', 'FRIDAY', '16:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Social Psychology'), 'SEMINAR', 'FRIDAY', '18:00:00', 90, (SELECT id FROM locations WHERE name='Psychology Lab A'));


-- ================= HISTORY SCHEDULE =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    -- 1. World History I
                                                                                                    ((SELECT id FROM courses WHERE name='World History I'), 'LECTURE', 'MONDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='World History I'), 'SEMINAR', 'MONDAY', '14:40:00', 90, (SELECT id FROM locations WHERE name='Archives Room')),
                                                                                                    -- 2. Ancient Civilizations
                                                                                                    ((SELECT id FROM courses WHERE name='Ancient Civilizations'), 'LECTURE', 'TUESDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='Ancient Civilizations'), 'SEMINAR', 'TUESDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='Humanities Building, Hall H1')),
                                                                                                    -- 3. Modern Europe
                                                                                                    ((SELECT id FROM courses WHERE name='Modern Europe'), 'LECTURE', 'WEDNESDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='Modern Europe'), 'SEMINAR', 'WEDNESDAY', '18:00:00', 90, (SELECT id FROM locations WHERE name='Archives Room')),
                                                                                                    -- 4. History of Art
                                                                                                    ((SELECT id FROM courses WHERE name='History of Art'), 'LECTURE', 'THURSDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='History of Art'), 'SEMINAR', 'THURSDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='Central Library')),
                                                                                                    -- 5. Research Methods
                                                                                                    ((SELECT id FROM courses WHERE name='Research Methods in History'), 'LECTURE', 'FRIDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='Research Methods in History'), 'SEMINAR', 'FRIDAY', '18:00:00', 90, (SELECT id FROM locations WHERE name='Archives Room'));