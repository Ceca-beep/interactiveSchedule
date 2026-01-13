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

-- 2. COURSES TABLE
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

-- 4. STUDENTS TABLE
CREATE TABLE IF NOT EXISTS students (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
                                        faculty VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. ADMINS TABLE
CREATE TABLE IF NOT EXISTS admins (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL UNIQUE,
                                      password VARCHAR(255) NOT NULL,
                                      faculty VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================
--                 SEED DATA
-- =============================================

-- A. Insert Locations
INSERT INTO locations (name, code, x, y) VALUES
                                             -- General / Existing
                                             ('Main Entrance', 'A', 0, 0),
                                             ('Central Library', 'L', 380, 90),
                                             ('University Admin', 'U', 40, 240),

                                             -- CS & Math
                                             ('CS Building, Room 101', 'C', 180, -60),
                                             ('CS Building, Room 305', 'C', 210, -40),
                                             ('CS Building, Lab 1', 'C', 200, -20),
                                             ('Math Building, Room 12', 'M', -420, -90),
                                             ('Math Auditorium', 'M', -460, -60),
                                             ('Physics Lab B', 'Phy', -480, -100),

                                             -- Psychology & Humanities
                                             ('Psychology Hall, Room 101', 'Psy', -300, 300),
                                             ('Psychology Lab A', 'Psy', -320, 310),
                                             ('History Wing, Room 5', 'His', 100, 520),
                                             ('Archives Room', 'His', 120, 540),
                                             ('Humanities Building, Hall H1', 'H', 100, 500),

                                             -- Medicine & Biology & Chemistry (New Wing)
                                             ('Medical School, Hall A', 'Med', 500, 400),
                                             ('Anatomy Lab', 'Med', 520, 410),
                                             ('Bio-Chem Building, Room 202', 'Bio', 450, 350),
                                             ('Chemistry Lab 4', 'Chem', 460, 370),
                                             ('Genetics Lab', 'Bio', 440, 360);

-- B. Insert Courses (Grouped by Faculty)
INSERT INTO courses (name, faculty) VALUES
                                        -- Computer Science
                                        ('Programming Fundamentals', 'Computer Science'),
                                        ('Data Structures', 'Computer Science'),
                                        ('Operating Systems', 'Computer Science'),
                                        ('Algorithms', 'Computer Science'),
                                        ('Databases', 'Computer Science'),

                                        -- Psychology
                                        ('Intro to Psychology', 'Psychology'),
                                        ('Cognitive Science', 'Psychology'),
                                        ('Behavioral Analysis', 'Psychology'),
                                        ('Clinical Psychology', 'Psychology'),
                                        ('Social Psychology', 'Psychology'),

                                        -- History
                                        ('World History I', 'History'),
                                        ('Ancient Civilizations', 'History'),
                                        ('Modern Europe', 'History'),
                                        ('History of Art', 'History'),
                                        ('Research Methods in History', 'History'),

                                        -- Medicine (NEW)
                                        ('Human Anatomy', 'Medicine'),
                                        ('Physiology', 'Medicine'),
                                        ('Medical Ethics', 'Medicine'),
                                        ('Pathology I', 'Medicine'),
                                        ('Pharmacology', 'Medicine'),

                                        -- Biology (NEW)
                                        ('Cell Biology', 'Biology'),
                                        ('Genetics', 'Biology'),
                                        ('Microbiology', 'Biology'),
                                        ('Ecology', 'Biology'),
                                        ('Evolutionary Bio', 'Biology'),

                                        -- Chemistry (NEW)
                                        ('General Chemistry', 'Chemistry'),
                                        ('Organic Chemistry', 'Chemistry'),
                                        ('Biochemistry', 'Chemistry'),
                                        ('Analytical Chem', 'Chemistry'),
                                        ('Physical Chemistry', 'Chemistry'),

                                        -- Mathematics (NEW)
                                        ('Calculus I', 'Mathematics'),
                                        ('Linear Algebra', 'Mathematics'),
                                        ('Discrete Math', 'Mathematics'),
                                        ('Probability', 'Mathematics'),
                                        ('Number Theory', 'Mathematics'),

                                        -- Physics (NEW)
                                        ('Mechanics', 'Physics'),
                                        ('Electromagnetism', 'Physics'),
                                        ('Quantum Physics', 'Physics'),
                                        ('Thermodynamics', 'Physics'),
                                        ('Astrophysics', 'Physics');

-- C. Insert Students
INSERT INTO students (name, faculty) VALUES
                                         ('Alex', 'Computer Science'),
                                         ('Sarah', 'Psychology'),
                                         ('Mike', 'History'),
                                         ('John', 'Medicine'),
                                         ('Emily', 'Biology'),
                                         ('David', 'Chemistry'),
                                         ('Lisa', 'Mathematics'),
                                         ('Tom', 'Physics');


-- D. Insert Schedule

-- ... [Keep previous CS, Psych, History entries if you want, or replace. I will include ALL below] ...

-- ================= COMPUTER SCIENCE =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    ((SELECT id FROM courses WHERE name='Programming Fundamentals'), 'LECTURE', 'MONDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Programming Fundamentals'), 'LAB', 'MONDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
                                                                                                    ((SELECT id FROM courses WHERE name='Data Structures'), 'LECTURE', 'TUESDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 305')),
                                                                                                    ((SELECT id FROM courses WHERE name='Operating Systems'), 'LECTURE', 'WEDNESDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Algorithms'), 'LECTURE', 'THURSDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 305')),
                                                                                                    ((SELECT id FROM courses WHERE name='Databases'), 'LECTURE', 'FRIDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='CS Building, Room 101'));

-- ================= PSYCHOLOGY =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    ((SELECT id FROM courses WHERE name='Intro to Psychology'), 'LECTURE', 'MONDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Cognitive Science'), 'LECTURE', 'TUESDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Behavioral Analysis'), 'LECTURE', 'WEDNESDAY', '09:50:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Clinical Psychology'), 'LECTURE', 'THURSDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101')),
                                                                                                    ((SELECT id FROM courses WHERE name='Social Psychology'), 'LECTURE', 'FRIDAY', '16:20:00', 90, (SELECT id FROM locations WHERE name='Psychology Hall, Room 101'));

-- ================= HISTORY =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    ((SELECT id FROM courses WHERE name='World History I'), 'LECTURE', 'MONDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='Ancient Civilizations'), 'LECTURE', 'TUESDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='Modern Europe'), 'LECTURE', 'WEDNESDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='History of Art'), 'LECTURE', 'THURSDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5')),
                                                                                                    ((SELECT id FROM courses WHERE name='Research Methods in History'), 'LECTURE', 'FRIDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='History Wing, Room 5'));

-- ================= MEDICINE =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    ((SELECT id FROM courses WHERE name='Human Anatomy'), 'LECTURE', 'MONDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='Medical School, Hall A')),
                                                                                                    ((SELECT id FROM courses WHERE name='Human Anatomy'), 'LAB', 'MONDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='Anatomy Lab')),
                                                                                                    ((SELECT id FROM courses WHERE name='Physiology'), 'LECTURE', 'TUESDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='Medical School, Hall A')),
                                                                                                    ((SELECT id FROM courses WHERE name='Medical Ethics'), 'SEMINAR', 'WEDNESDAY', '14:40:00', 90, (SELECT id FROM locations WHERE name='Medical School, Hall A')),
                                                                                                    ((SELECT id FROM courses WHERE name='Pathology I'), 'LECTURE', 'THURSDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='Medical School, Hall A')),
                                                                                                    ((SELECT id FROM courses WHERE name='Pharmacology'), 'LECTURE', 'FRIDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='Medical School, Hall A'));

-- ================= BIOLOGY =================
INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
                                                                                                    ((SELECT id FROM courses WHERE name='Cell Biology'), 'LECTURE', 'MONDAY', '11:20:00', 90, (SELECT id FROM locations WHERE name='Bio-Chem Building, Room 202')),
                                                                                                    ((SELECT id FROM courses WHERE name='Genetics'), 'LECTURE', 'TUESDAY', '08:00:00', 90, (SELECT id FROM locations WHERE name='Bio-Chem Building, Room 202')),
                                                                                                    ((SELECT id FROM courses WHERE name='Genetics'), 'LAB', 'TUESDAY', '14:40:00', 90, (SELECT id FROM locations WHERE name='Genetics Lab')),
                                                                                                    ((SELECT id FROM courses WHERE name='Microbiology'), 'LECTURE', 'WEDNESDAY', '09:40:00', 90, (SELECT id FROM locations WHERE name='Bio-Chem Building, Room 202')),
                                                                                                    ((SELECT id FROM courses WHERE name='Ecology'), 'LECTURE', 'THURSDAY', '13:00:00', 90, (SELECT id FROM locations WHERE name='Bio-Chem Building, Room 20'));