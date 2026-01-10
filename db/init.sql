-- sql
CREATE DATABASE IF NOT EXISTS university_schedule
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
USE university_schedule;

CREATE TABLE IF NOT EXISTS locations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  code VARCHAR(10),
  x INT,
  y INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS courses (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS timetable_entries (
  id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  type ENUM('LECTURE','LAB','SEMINAR') NOT NULL,
  day ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
  start_time TIME NOT NULL,
  duration_minutes INT NOT NULL,
  location_id INT NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS students (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO locations (name, code, x, y) VALUES
  ('Main Entrance', 'A', 0, 0),
  ('CS Building, Room 101', 'C', 180, -60),
  ('Math Building, Room 12', 'M', -420, -90),
  ('North Gate', 'A', 50, 1100),
  ('South Gate', 'A', -40, -1000),
  ('East Gate', 'A', 1200, 120),
  ('West Gate', 'A', -1200, -150),
  ('Central Library', 'L', 380, 90),
  ('Student Canteen', 'F', 220, -180),
  ('CS Building, Room 305', 'C', 210, -40),
  ('CS Building, Lab 1', 'C', 200, -20),
  ('Math Auditorium', 'M', -460, -60),
  ('Physics Building, Room 201', 'P', -300, 300),
  ('Humanities Building, Hall H1', 'H', 100, 520),
  ('University Admin', 'U', 40, 240),
  ('Dorm A', 'D', -800, 650),
  ('Dorm B', 'D', 760, -620);

INSERT INTO courses (name) VALUES
  ('Programming Fundamentals'),
  ('Linear Algebra'),
  ('Campus Orientation'),
  ('Data Structures'),
  ('Academic Writing'),
  ('Operating Systems'),
  ('Discrete Math'),
  ('Physics Seminar'),
  ('Algorithms'),
  ('Ethics in IT'),
  ('Databases'),
  ('Probability'),
  ('Research Seminar');

INSERT INTO timetable_entries (course_id, type, day, start_time, duration_minutes, location_id) VALUES
  ((SELECT id FROM courses WHERE name='Programming Fundamentals'), 'LECTURE', 'MONDAY', '08:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
  ((SELECT id FROM courses WHERE name='Programming Fundamentals'), 'LAB', 'MONDAY', '12:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
  ((SELECT id FROM courses WHERE name='Linear Algebra'), 'LECTURE', 'MONDAY', '10:00:00', 90, (SELECT id FROM locations WHERE name='Math Auditorium')),
  ((SELECT id FROM courses WHERE name='Campus Orientation'), 'SEMINAR', 'MONDAY', '16:00:00', 90, (SELECT id FROM locations WHERE name='University Admin')),

  ((SELECT id FROM courses WHERE name='Data Structures'), 'LECTURE', 'TUESDAY', '09:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Room 305')),
  ((SELECT id FROM courses WHERE name='Data Structures'), 'LAB', 'TUESDAY', '13:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
  ((SELECT id FROM courses WHERE name='Academic Writing'), 'SEMINAR', 'TUESDAY', '15:00:00', 90, (SELECT id FROM locations WHERE name='Humanities Building, Hall H1')),

  ((SELECT id FROM courses WHERE name='Operating Systems'), 'LECTURE', 'WEDNESDAY', '08:30:00', 120, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
  ((SELECT id FROM courses WHERE name='Operating Systems'), 'LAB', 'WEDNESDAY', '12:30:00', 120, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
  ((SELECT id FROM courses WHERE name='Discrete Math'), 'LECTURE', 'WEDNESDAY', '10:30:00', 90, (SELECT id FROM locations WHERE name='Math Building, Room 12')),
  ((SELECT id FROM courses WHERE name='Physics Seminar'), 'SEMINAR', 'WEDNESDAY', '15:30:00', 90, (SELECT id FROM locations WHERE name='Physics Building, Room 201')),

  ((SELECT id FROM courses WHERE name='Algorithms'), 'LECTURE', 'THURSDAY', '09:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Room 305')),
  ((SELECT id FROM courses WHERE name='Algorithms'), 'LAB', 'THURSDAY', '13:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
  ((SELECT id FROM courses WHERE name='Ethics in IT'), 'SEMINAR', 'THURSDAY', '16:00:00', 90, (SELECT id FROM locations WHERE name='Humanities Building, Hall H1')),

  ((SELECT id FROM courses WHERE name='Databases'), 'LECTURE', 'FRIDAY', '08:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Room 101')),
  ((SELECT id FROM courses WHERE name='Databases'), 'LAB', 'FRIDAY', '12:00:00', 120, (SELECT id FROM locations WHERE name='CS Building, Lab 1')),
  ((SELECT id FROM courses WHERE name='Probability'), 'LECTURE', 'FRIDAY', '10:00:00', 90, (SELECT id FROM locations WHERE name='Math Auditorium')),
  ((SELECT id FROM courses WHERE name='Research Seminar'), 'SEMINAR', 'FRIDAY', '15:00:00', 90, (SELECT id FROM locations WHERE name='Central Library'));

INSERT INTO students (name) VALUES ('Student');