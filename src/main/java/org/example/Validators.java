package org.example;

public final class Validators {
    private Validators() {}
    public static void requireKnownStorage(String s) {
        if (!"db".equalsIgnoreCase(s))
            throw new IllegalArgumentException("Unknown storage: " + s + " — only 'db' is supported in this build.");
    }
    public static void requireNonEmptyDayIfPresent(String day) {
        if (day != null && day.isBlank()) throw new IllegalArgumentException("Day cannot be blank.");
    }
    public static void requireNonEmptyTypeIfPresent(String type) {
        if (type != null && type.isBlank()) throw new IllegalArgumentException("Type cannot be blank.");
    }
}
