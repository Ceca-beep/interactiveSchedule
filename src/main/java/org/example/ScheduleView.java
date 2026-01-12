package org.example;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

public class ScheduleView extends ScrollPane {

    private final JdbcStorage storage;
    private final String dataSource;

    // 1. Define the Days (Columns)
    private final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};

    // 2. Define the Time Slots (Rows) - 1.5 Hour Intervals
    private final String[] TIME_SLOTS = {
            "08:00", "09:40", "11:20", "13:00",
            "14:40", "16:20", "18:00", "19:40", "21:10"
    };

    public ScheduleView(JdbcStorage storage, String dataSource) {
        this.storage = storage;
        this.dataSource = dataSource;

        // Basic Styling for the scroll pane
        setFitToWidth(true);
        setPadding(new Insets(20));

        // Create the main grid
        GridPane grid = new GridPane();
        grid.setHgap(10); // Gap between days
        grid.setVgap(10); // Gap between times
        grid.setAlignment(Pos.CENTER);

        // Build the Headers (Days and Times)
        setupGridStructure(grid);

        // Load and Place the Classes
        loadAndPlaceClasses(grid);

        setContent(grid);
    }

    private void setupGridStructure(GridPane grid) {
        // A. Add "Time" Header at top-left
        Label timeHeader = new Label("Time / Day");
        timeHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        grid.add(timeHeader, 0, 0);

        // B. Add Day Headers (Row 0)
        for (int i = 0; i < DAYS.length; i++) {
            Label dayLabel = new Label(DAYS[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            dayLabel.setTextFill(Color.DARKBLUE);
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            // Column index is i + 1 (because column 0 is for the time labels)
            grid.add(dayLabel, i + 1, 0);
        }

        // C. Add Time Row Headers (Column 0)
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            Label timeLabel = new Label(TIME_SLOTS[i]);
            timeLabel.setFont(Font.font("Arial", 14));
            timeLabel.setPadding(new Insets(10));
            // Row index is i + 1 (because row 0 is for day headers)
            grid.add(timeLabel, 0, i + 1);
        }
    }

    private void loadAndPlaceClasses(GridPane grid) {
        try {
            // Load data from DB
            DataSnapshot data = storage.load(dataSource);
            if (data == null) return;

            List<TimetableEntry> entries = data.getEntries();

            for (TimetableEntry entry : entries) {
                // 1. Find the Column (Day)
                int colIndex = getDayColumnIndex(entry.getDay());
                if (colIndex == -1) continue; // Skip weekends if not in array

                // 2. Find the Row (Time)
                int rowIndex = getTimeRowIndex(entry.getStartTime());
                if (rowIndex == -1) continue; // Skip odd times not in our slots

                // 3. Create the Rectangle (Button)
                Button classBlock = createClassBlock(entry);

                // 4. Add to Grid
                grid.add(classBlock, colIndex, rowIndex);
            }

        } catch (IOException e) {
            grid.add(new Label("Error loading schedule: " + e.getMessage()), 1, 1);
        }
    }

    private Button createClassBlock(TimetableEntry entry) {
        // Create a button that looks like a rectangle card
        Button btn = new Button();
        btn.setText(entry.getCourseName() + "\n(" + entry.getType() + ")\n" + entry.getLocationId()); // You might want to lookup location name later
        btn.setTextAlignment(TextAlignment.CENTER);
        btn.setWrapText(true);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setMinHeight(80); // Make sure it's tall enough
        btn.setMinWidth(120);

        // Different colors for Lecture vs Lab vs Seminar
        String colorStyle = switch (entry.getType().name()) {
            case "LECTURE" -> "-fx-background-color: #4CAF50; -fx-text-fill: white;"; // Green
            case "LAB" -> "-fx-background-color: #2196F3; -fx-text-fill: white;";     // Blue
            case "SEMINAR" -> "-fx-background-color: #FF9800; -fx-text-fill: white;"; // Orange
            default -> "-fx-background-color: #9E9E9E; -fx-text-fill: white;";
        };

        btn.setStyle(colorStyle + "-fx-background-radius: 10; -fx-cursor: hand;");

        // Click Action (Placeholder for your Navigation Window)
        btn.setOnAction(e -> {
            System.out.println("You clicked on: " + entry.getCourseName());
            // TODO: Open your navigation window here later!
        });

        return btn;
    }

    private int getDayColumnIndex(String day) {
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(day)) {
                return i + 1; // +1 because col 0 is Time
            }
        }
        return -1;
    }

    private int getTimeRowIndex(String startTime) {
        // Parse the "HH:MM" string to compare times
        LocalTime entryTime = LocalTime.parse(startTime);

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            LocalTime slotTime = LocalTime.parse(TIME_SLOTS[i]);
            // Use a small buffer (e.g., if class is 08:05, put it in 08:00 slot)
            if (!entryTime.isBefore(slotTime) && entryTime.isBefore(slotTime.plusMinutes(90))) {
                return i + 1; // +1 because row 0 is Header
            }
        }
        return -1;
    }
}