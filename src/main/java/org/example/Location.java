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
        this.x = x;
        this.y = y;
    }

    public Location(int id, String name, int x, int y) {
        this.id = String.valueOf(id);
        this.name = name;
        this.building = "TEST"; // Default for tests
        this.x = x;
        this.y = y;
    }

    public Location(String name, int x, int y) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.building = "TEST"; // Default for tests
        this.x = x;
        this.y = y;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBuilding() { return building; }
    public int getX() { return x; }
    public int getY() { return y; }


    public String getDirectionsFromEntrance() {
        StringBuilder sb = new StringBuilder();
        if (x == 0 && y == 0) {
            return "You are at the Main Entrance.";
        }

        sb.append("START: Main Entrance\n");
        sb.append("----------------------\n");


        if (this.x > 0) {
            sb.append("• Go EAST (Right) for ").append(this.x).append(" meters.\n");
        } else if (this.x < 0) {
            sb.append("• Go WEST (Left) for ").append(Math.abs(this.x)).append(" meters.\n");
        }


        if (this.y > 0) {
            sb.append("• Turn and go SOUTH (Down) for ").append(this.y).append(" meters.\n");
        } else if (this.y < 0) {
            sb.append("• Turn and go NORTH (Up) for ").append(Math.abs(this.y)).append(" meters.\n");
        }

        sb.append("----------------------\n");
        sb.append("You have arrived at ").append(this.name);
        if (this.building != null && !this.building.equals("TEST")) {
            sb.append("\n(Building Code: ").append(this.building).append(")");
        }

        return sb.toString();
    }
}