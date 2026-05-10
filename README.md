# Interactive Schedule

A desktop application that helps first-year university students navigate campus and understand their class schedule. Students can view their timetable organized by day and time, and get turn-by-turn walking directions to any classroom from the main entrance. Faculty administrators can manage timetable entries directly from the app.

---

## Authentication as a Student
> ### Login window
  
<img width="400" height="400" alt="image" src="https://github.com/user-attachments/assets/27da9319-3e0e-4850-907b-8ed50ab3a926" />

> ### If you don't have an account, it can be created very easily

<p style="flex-direction:row;">
  <img src="https://github.com/user-attachments/assets/ee30eec3-4741-4b42-9592-5782e3b72c4d" height="660" width="400" />
  <img src="https://github.com/user-attachments/assets/e84ef4d1-db0e-4332-8579-fa4d78164639" height="400" width="400" />
</p>

## Timetable
> ### After logging in, the timetable will appear and by clicking on the interval, a window with directions will appear

<img width="700" height="700" alt="Screenshot 2026-02-27 122055" src="https://github.com/user-attachments/assets/226634ac-7729-4eb4-b85b-357316830cb4" />

## Authentication as an admin
> ### Login window

<img width="400" height="400" alt="image" src="https://github.com/user-attachments/assets/07cd0f1f-a515-422e-b1cf-95fd88349822" />

## Timetable
> ### An admin can create, edit and delete a class 

<img width="700" height="700" alt="image" src="https://github.com/user-attachments/assets/ee5f5062-d580-454c-a54e-8bf830f978f8" />
<img width="600" height="600" alt="image" src="https://github.com/user-attachments/assets/ec2d3aa3-4fa0-4014-8530-d06ec3664ef8" />


---

## Features

**Students**
- Log in by name and faculty — account is created automatically on first login
- View a weekly schedule grid (Monday–Friday, 08:00–21:00)
- Click any class block to get step-by-step directions to the room
- Color-coded class types: Lecture (green), Lab (blue), Seminar (orange)

**Administrators**
- Separate admin login per faculty
- Add, edit, and delete timetable entries
- Manage campus locations with coordinate-based directions

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| GUI | JavaFX 21 |
| Database | MySQL 8.0 |
| Build | Maven 3.x |
| Testing | JUnit 5 |
| DB container | Docker Compose |

---

## Prerequisites

- JDK 25+
- Maven 3.6+
- MySQL 8.0 **or** Docker & Docker Compose

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/Ceca-beep/interactiveSchedule.git
cd interactiveSchedule
```

### 2. Set up the database

**Option A — Docker (recommended)**

```bash
docker-compose -f src/main/java/docker-compose.yaml up -d
```

This starts a MySQL 8.0 container on port `3306` with the `university_schedule` database already created.

**Option B — Existing MySQL installation**

1. Make sure MySQL 8.0 is running on `localhost:3306` with root password `root`.
2. Run the initialization script to create the schema and seed data:

```bash
mysql -u root -proot < db/init.sql
```

### 3. Build the project

```bash
mvn clean package
```

### 4. Run the application

```bash
mvn javafx:run
```

Or run the self-contained shaded JAR produced by the build:

```bash
java -jar target/interactiveSchedule-1.0-SNAPSHOT-shaded.jar
```

---

## Configuration

By default the app connects to:

```
jdbc:mysql://localhost:3306/university_schedule?user=root&password=root
```

To use a different database, set the `JDBC_URL` environment variable before launching:

```bash
# Linux / macOS
export JDBC_URL="jdbc:mysql://your-host:3306/university_schedule?user=root&password=yourpassword"

# Windows PowerShell
$env:JDBC_URL = "jdbc:mysql://your-host:3306/university_schedule?user=root&password=yourpassword"
```

---

## Running Tests

```bash
mvn test
```

---

## Project Structure

```
interactiveSchedule/
├── db/
│   └── init.sql                  # Database schema and seed data
├── src/
│   └── main/
│       └── java/org/example/
│           ├── Main.java          # JavaFX entry point
│           ├── Launcher.java      # CLI launcher
│           ├── LoginPane.java     # Login screen
│           ├── ScheduleView.java  # Weekly schedule grid + dialogs
│           ├── JdbcStorage.java   # All database operations
│           ├── Storage.java       # Storage interface
│           ├── Student.java       # Student model
│           ├── TimetableEntry.java
│           ├── Location.java      # Campus location + directions logic
│           ├── ClassType.java     # LECTURE / LAB / SEMINAR enum
│           ├── DataSnapshot.java
│           └── docker-compose.yaml
└── pom.xml
```

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Student | any name | *(no password — faculty selection only)* |
| Admin | `admin` | `admin123` |

Student accounts are created automatically the first time a name is entered for a given faculty.

---

## Supported Faculties

Computer Science · Psychology · History · Medicine · Biology · Chemistry · Mathematics · Physics
