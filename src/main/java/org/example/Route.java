package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Route implements Serializable {
    private final List<String> steps = new ArrayList<>();
    private int estimatedMinutes;

    public List<String> getSteps() { return steps; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int m) { this.estimatedMinutes = m; }
    public void addStep(String s) { steps.add(s); }
}
