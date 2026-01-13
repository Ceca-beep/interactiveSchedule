package org.example;

import java.io.Serializable;


public class Student implements Serializable {
    private String name;
    private String faculty;

    public Student() {}
    public Student(String name) {this.name = name; }
    public Student(String name, String faculty) {this.name = name; this.faculty = faculty; }


    public String getName() { return name; }
    public String getFaculty() { return faculty; }
}
