package org.example;

import java.io.Serializable;
import java.util.UUID;

public class Student implements Serializable {
    private String id;
    private String name;
    private String faculty; // optional

    public Student() {}
    public Student(String name) { this.id = UUID.randomUUID().toString(); this.name = name; }
    public Student(String name, String faculty) { this.id = UUID.randomUUID().toString(); this.name = name; this.faculty = faculty; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFaculty() { return faculty; }
}
