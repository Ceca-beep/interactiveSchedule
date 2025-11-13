package org.example;

public class SimpleNavigator implements Navigator {
    @Override
    public Route route(Location from, Location to) {
        Route r = new Route();
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();

        if (dx > 0) r.addStep("Walk east " + dx + " meters");
        if (dx < 0) r.addStep("Walk west " + (-dx) + " meters");
        if (dy > 0) r.addStep("Then walk north " + dy + " meters");
        if (dy < 0) r.addStep("Then walk south " + (-dy) + " meters");

        int manhattan = Math.abs(dx) + Math.abs(dy);
        int minutes = Math.max(1, (int)Math.round(manhattan / 80.0 * 10.0)); // scaled
        r.setEstimatedMinutes(minutes);

        if (r.getSteps().isEmpty()) {
            r.addStep("You are already at the destination.");
            r.setEstimatedMinutes(0);
        }
        return r;
    }
}
