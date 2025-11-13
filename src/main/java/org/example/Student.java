package org.example;

import java.io.Serializable;
import java.util.UUID;

public class Student implements Serializable {
    private String id;
    private String name;

    public Student() {}
    public Student(String name) { this.id = UUID.randomUUID().toString(); this.name = name; }

    public String getId() { return id; }
    public String getName() { return name; }
}
