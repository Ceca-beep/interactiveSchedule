package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        Map<String, String> kv = parseArgs(args);

        // Default storage changed to db so the app uses MySQL by default.
        String storageType = kv.getOrDefault("storage", "db").toLowerCase();
        // Prefer JDBC_URL env var if present, otherwise fall back to provided --data or a sensible local default
        String envUrl = System.getenv("JDBC_URL");
        String dataSource  = kv.getOrDefault("data", envUrl != null && !envUrl.isBlank()
                ? envUrl
                : "jdbc:mysql://localhost:3306/university_schedule?user=root&password=root");
        String dayFilter   = kv.get("day");
        String typeFilter  = kv.get("type");
        String fromName    = kv.getOrDefault("from", "Main Entrance");

        Validators.requireKnownStorage(storageType);
        Validators.requireNonEmptyDayIfPresent(dayFilter);
        Validators.requireNonEmptyTypeIfPresent(typeFilter);

        try (Scanner scanner = new Scanner(System.in)) {
            try {
                // Always use JDBC storage in DB mode.
                final Storage storage;
                if ("db".equalsIgnoreCase(storageType)) {
                    storage = new JdbcStorage();
                } else {
                    throw new IllegalArgumentException("Only 'db' storage is supported in this build: " + storageType);
                }

                DataSnapshot data;
                try {
                    data = storage.load(dataSource);
                    // If load returned null or empty, seed
                    if (data == null) data = DataSnapshot.seed();
                } catch (Exception ex) {
                    // If loading failed for DB because DB missing or empty, seed and save
                    data = DataSnapshot.seed();
                    try {
                        storage.save(data, dataSource);
                        System.out.println("Created seed data at: " + dataSource);
                    } catch (Exception sx) {
                        System.err.println("Failed to save seed data: " + sx.getMessage());
                    }
                }

                // Prompt for day at runtime if not provided via args
                if (dayFilter == null || dayFilter.isBlank()) {
                    System.out.print("Enter day to filter (e.g. MONDAY) or press ENTER to show all: ");
                    String inputDay = scanner.nextLine().trim();
                    if (!inputDay.isEmpty()) {
                        dayFilter = inputDay;
                    }
                }

                // Always show the list of matched classes and allow user to pick by the printed number
                List<TimetableEntry> filtered = data.getEntries();

                if (dayFilter != null && !dayFilter.isBlank()) {
                    String df = dayFilter.trim().toUpperCase();
                    filtered = filtered.stream()
                            .filter(e -> e.getDay().equalsIgnoreCase(df))
                            .collect(Collectors.toList());
                }
                if (typeFilter != null && !typeFilter.isBlank()) {
                    String tf = typeFilter.trim().toUpperCase();
                    filtered = filtered.stream()
                            .filter(e -> e.getType().name().equalsIgnoreCase(tf))
                            .collect(Collectors.toList());
                }

                if (filtered.isEmpty()) {
                    System.out.println("No classes match your filters.");
                } else {
                    System.out.println("\n=== Matched Classes ===");
                    for (int i = 0; i < filtered.size(); i++) {
                        System.out.printf("%d) ", i + 1);
                        printEntry(filtered.get(i), data);
                    }

                    // Allow user to pick a class to view details and route
                    System.out.print("\nEnter class number to view details and route (or press ENTER to skip): ");
                    String pick = scanner.nextLine().trim();
                    if (!pick.isEmpty()) {
                        try {
                            int idx = Integer.parseInt(pick) - 1;
                            if (idx < 0 || idx >= filtered.size()) {
                                System.out.println("Invalid selection.");
                            } else {
                                TimetableEntry entry = filtered.get(idx);
                                System.out.println("\n=== Class Details ===");
                                printEntry(entry, data);

                                // Ask for start location (default to provided --from)
                                System.out.print("Start location (press ENTER for '" + fromName + "'): ");
                                String startInput = scanner.nextLine().trim();
                                String startName = startInput.isEmpty() ? fromName : startInput;

                                Location from = data.findLocationByName(startName)
                                        .orElseThrow(() -> new NoSuchElementException("Start location not found: " + startName));
                                Location to = data.findLocationById(entry.getLocationId())
                                        .orElseThrow(() -> new NoSuchElementException("Class location not found: " + entry.getLocationId()));

                                Navigator nav = new SimpleNavigator();
                                Route route = nav.route(from, to);
                                printRoute(route, from, to);
                            }
                        } catch (NumberFormatException nfe) {
                            System.out.println("Invalid number.");
                        }
                    }
                }

            } catch (Exception e) {
                // Print concise error message. Avoid using printStackTrace() to keep logs tidy.
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printEntry(TimetableEntry e, DataSnapshot data) {
        String loc = data.findLocationById(e.getLocationId()).map(Location::getName).orElse("?");
        System.out.printf("- [%s] %s • %s %s • %d min • at: %s • id=%s%n",
                e.getType(), e.getCourseName(), e.getDay(), e.getStartTime(),
                e.getDurationMinutes(), loc, e.getId());
    }
    private static void printRoute(Route route, Location from, Location to) {
        System.out.printf("\n=== Route from \"%s\" to \"%s\" ===%n", from.getName(), to.getName());
        for (String step : route.getSteps()) System.out.println("• " + step);
        System.out.printf("Estimated time: %d min%n", route.getEstimatedMinutes());
    }
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        if (args == null) return map;
        for (String a : args) {
            if (a.startsWith("--") && a.contains("=")) {
                int i = a.indexOf('=');
                map.put(a.substring(2, i), a.substring(i + 1));
            } else if (a.startsWith("--")) {
                map.put(a.substring(2), "true");
            }
        }
        return map;
    }
}
