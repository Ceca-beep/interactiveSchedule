package org.example;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Location implements Serializable {
    private String id;
    private String name;
    private String building;
    private int x;
    private int y;

    public Location() {}
    public Location(String name, String building, int x, int y) {
        this.id = UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.building = Objects.requireNonNull(building);
        this.x = x; this.y = y;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBuilding() { return building; }
    public int getX() { return x; }
    public int getY() { return y; }
}
