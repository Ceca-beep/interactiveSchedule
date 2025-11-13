package org.example;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        Map<String, String> kv = parseArgs(args);


        String storageType = kv.getOrDefault("storage", "json").toLowerCase();
        String dataPath    = kv.getOrDefault("data", "schedule.json");
        String dayFilter   = kv.get("day");
        String typeFilter  = kv.get("type");
        String classId     = kv.get("classId");
        String fromName    = kv.getOrDefault("from", "Main Entrance");
        boolean benchmark  = Boolean.parseBoolean(kv.getOrDefault("benchmark", "false"));

        Validators.requireKnownStorage(storageType);
        Validators.requireNonEmptyDayIfPresent(dayFilter);
        Validators.requireNonEmptyTypeIfPresent(typeFilter);

        try {
            Storage storage = switch (storageType) {
                case "json" -> new JsonStorage();
                case "obj"  -> new ObjectStreamStorage();
                default     -> throw new IllegalArgumentException("Unknown storage: " + storageType);
            };

            Path path = Path.of(dataPath);
            DataSnapshot data = new File(dataPath).exists() ? storage.load(path) : DataSnapshot.seed();
            if (!new File(dataPath).exists()) {
                storage.save(data, path);
                System.out.println("Created seed data at: " + path.toAbsolutePath());
            }

            if (classId != null && !classId.isBlank()) {
                // Simulate "click": show details + route
                TimetableEntry entry = data.getEntries().stream()
                        .filter(e -> e.getId().equals(classId))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("Class not found: " + classId));

                Location from = data.findLocationByName(fromName)
                        .orElseThrow(() -> new NoSuchElementException("Start location not found: " + fromName));

                Location to = data.findLocationById(entry.getLocationId())
                        .orElseThrow(() -> new NoSuchElementException("Class location not found: " + entry.getLocationId()));

                System.out.println("\n=== Class Details ===");
                printEntry(entry, data);

                Navigator nav = new SimpleNavigator();
                Route route = nav.route(from, to);
                printRoute(route, from, to);
            } else {
                // List schedule with optional filters
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
                    for (TimetableEntry e : filtered) { printEntry(e, data); }
                }
            }

            if (benchmark) runBenchmark(data);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
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
    private static void runBenchmark(DataSnapshot data) {
        try {
            Path jsonPath = Path.of("schedule.json");
            Path objPath  = Path.of("schedule.bin");
            Storage json = new JsonStorage();
            Storage obj  = new ObjectStreamStorage();

            long t1 = System.nanoTime(); json.save(data, jsonPath);
            long t2 = System.nanoTime(); obj.save(data, objPath);
            long t3 = System.nanoTime(); json.load(jsonPath);
            long t4 = System.nanoTime(); obj.load(objPath);
            long t5 = System.nanoTime();

            System.out.printf("\n=== Benchmark ===%n");
            System.out.printf("JSON save: %.2f ms, size: %d bytes%n",
                    (t2 - t1) / 1e6, jsonPath.toFile().length());
            System.out.printf("OBJ  save: %.2f ms, size: %d bytes%n",
                    (t3 - t2) / 1e6, objPath.toFile().length());
            System.out.printf("JSON load: %.2f ms%n", (t4 - t3) / 1e6);
            System.out.printf("OBJ  load: %.2f ms%n", (t5 - t4) / 1e6);
        } catch (Exception e) {
            System.err.println("Benchmark error: " + e.getMessage());
        }
    }
}
