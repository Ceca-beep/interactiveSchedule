package org.example;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScheduleView extends ScrollPane {

    private final JdbcStorage storage;
    private final String dataSource;
    private final boolean isAdmin;
    private final String userFaculty;
    private final Runnable onLogout;

    private final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
    private final String[] TIME_SLOTS = {
            "08:00", "09:40", "11:20", "13:00",
            "14:40", "16:20", "18:00", "19:40", "21:10"
    };

    public ScheduleView(JdbcStorage storage, String dataSource, boolean isAdmin, String userFaculty, Runnable onLogout) {
        this.storage = storage;
        this.dataSource = dataSource;
        this.isAdmin = isAdmin;
        this.userFaculty = userFaculty;
        this.onLogout = onLogout;

        setFitToWidth(true);
        setPadding(new Insets(20));
        refreshSchedule();
    }

    private void refreshSchedule() {
        VBox mainLayout = new VBox(10);
        mainLayout.setAlignment(Pos.CENTER);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        Button btnLogout = new Button("← Logout / Go Back");
        btnLogout.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> onLogout.run());

        topBar.getChildren().add(btnLogout);
        mainLayout.getChildren().add(topBar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        setupGridStructure(grid);
        loadAndPlaceClasses(grid);

        mainLayout.getChildren().add(grid);
        setContent(mainLayout);
    }

    private void setupGridStructure(GridPane grid) {
        Label timeHeader = new Label("Time / Day");
        timeHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        grid.add(timeHeader, 0, 0);

        for (int i = 0; i < DAYS.length; i++) {
            Label dayLabel = new Label(DAYS[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            dayLabel.setTextFill(Color.DARKBLUE);
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            grid.add(dayLabel, i + 1, 0);
        }

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            Label timeLabel = new Label(TIME_SLOTS[i]);
            timeLabel.setFont(Font.font("Arial", 14));
            timeLabel.setPadding(new Insets(10));
            grid.add(timeLabel, 0, i + 1);
        }
    }

    // Inside ScheduleView.java

    private int getDayColumn(String day) {
        if (day == null) return -1;
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(day.trim())) {
                return i + 1; // +1 because Col 0 is the Time Header
            }
        }
        return -1;
    }

    private int getTimeRow(String timeStr) {
        if (timeStr == null) return -1;

        // Standardize the time string from the DB (e.g., "08:00:00" -> "08:00")
        String cleanTime = timeStr.trim();
        if (cleanTime.length() > 5) {
            cleanTime = cleanTime.substring(0, 5);
        }

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].equals(cleanTime)) {
                return i + 1; // +1 because Row 0 is the Day Header
            }
        }

        // DEBUG: If a row isn't found, print why
        System.out.println("DEBUG: Could not find row for time: [" + cleanTime + "]");
        return -1;
    }

    private void fillRemainingEmptySlots(GridPane grid) {
        for (int col = 1; col <= DAYS.length; col++) {
            for (int row = 1; row <= TIME_SLOTS.length; row++) {
                // Check if this specific cell is empty
                if (getNodeFromGridPane(grid, col, row) == null) {
                    grid.add(createEmptySlot(DAYS[col - 1], TIME_SLOTS[row - 1]), col, row);
                }
            }
        }
    }

    // Helper to check if a cell is occupied
    private javafx.scene.Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }


    private void loadAndPlaceClasses(GridPane grid) {
        Thread loadThread = new Thread(() -> {
            try {
                // 1. Fetch data in background (Requirement 3)
                DataSnapshot data = storage.loadByFaculty(userFaculty, dataSource);

                javafx.application.Platform.runLater(() -> {
                    // 2. CLEAR ONLY DATA (Keep the Time/Day headers)
                    grid.getChildren().removeIf(node -> {
                        Integer row = GridPane.getRowIndex(node);
                        Integer col = GridPane.getColumnIndex(node);
                        return row != null && col != null && row > 0 && col > 0;
                    });

                    // 3. Place the courses
                    for (TimetableEntry entry : data.getEntries()) {
                        // Use trim() and toUpperCase() to ensure exact matching
                        int col = getDayColumn(entry.getDay().trim().toUpperCase());
                        int row = getTimeRow(entry.getStartTime().trim());

                        if (col != -1 && row != -1) {
                            Location loc = findLocation(data.getLocations(), entry.getLocationId());
                            grid.add(createClassBlock(entry, loc), col, row);
                        } else {
                            // This will tell you exactly which course is failing and why
                            System.out.println("MISSING FROM UI: " + entry.getCourseName() +
                                    " at " + entry.getDay() + " " + entry.getStartTime());
                        }
                    }

                    // 4. Fill empty slots so they remain clickable
                    fillRemainingEmptySlots(grid);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private TimetableEntry findEntryForSlot(List<TimetableEntry> entries, String day, String timeStr) {
        LocalTime slotTime = LocalTime.parse(timeStr);
        for (TimetableEntry entry : entries) {
            if (!entry.getDay().equalsIgnoreCase(day)) continue;

            String entryTimeStr = entry.getStartTime();
            if (entryTimeStr.length() > 5) entryTimeStr = entryTimeStr.substring(0, 5);

            LocalTime entryTime = LocalTime.parse(entryTimeStr);

            if (!entryTime.isBefore(slotTime) && entryTime.isBefore(slotTime.plusMinutes(90))) {
                return entry;
            }
        }
        return null;
    }

    private Location findLocation(List<Location> locations, String locIdOrName) {
        return locations.stream()
                .filter(l -> l.getName().equals(locIdOrName) || l.getId().equals(locIdOrName))
                .findFirst().orElse(null);
    }

    private StackPane createEmptySlot(String day, String startTime) {
        StackPane pane = new StackPane();
        pane.setPrefSize(120, 80);

        if (isAdmin) {
            pane.setStyle("-fx-background-color: transparent; -fx-border-color: #eee; -fx-border-width: 1; -fx-cursor: hand;");
            pane.setOnMouseEntered(e -> pane.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #4CAF50;"));
            pane.setOnMouseExited(e -> pane.setStyle("-fx-background-color: transparent; -fx-border-color: #eee;"));
            pane.setOnMouseClicked(e -> showAddDialog(day, startTime));
        } else {
            pane.setStyle("-fx-border-color: #f4f4f4;");
        }
        return pane;
    }

    // Inside ScheduleView.java

    // 1. Update createClassBlock to open Edit Dialog on click
    private Button createClassBlock(TimetableEntry entry, Location location) {
        String locationDisplay = (location != null) ? location.getName() : entry.getLocationId();
        Button btn = new Button(entry.getCourseName() + "\n" + locationDisplay + "\n(" + entry.getType() + ")");
        btn.setTextAlignment(TextAlignment.CENTER);
        btn.setWrapText(true);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setMinHeight(80);
        btn.setMinWidth(120);

        String colorStyle = switch (entry.getType().name()) {
            case "LECTURE" -> "-fx-background-color: #4CAF50; -fx-text-fill: white;";
            case "LAB" -> "-fx-background-color: #2196F3; -fx-text-fill: white;";
            case "SEMINAR" -> "-fx-background-color: #FF9800; -fx-text-fill: white;";
            default -> "-fx-background-color: #9E9E9E; -fx-text-fill: white;";
        };
        btn.setStyle(colorStyle + "-fx-background-radius: 10; -fx-cursor: hand;");

        btn.setOnAction(e -> {
            if (isAdmin) {
                // OPEN EDIT DIALOG (Feature 1)
                showEditDialog(entry, location);
            } else {
                // Show Directions
                String msg = (location != null) ? location.getDirectionsFromEntrance() : "No location data";
                new Alert(Alert.AlertType.INFORMATION, msg).show();
            }
        });
        return btn;
    }

    // 2. Update Add Dialog to include Directions (X/Y)
    private void showAddDialog(String day, String startTime) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add New Class");
        dialog.setHeaderText("Add Class for " + day + " at " + startTime);
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("LECTURE", "LAB", "SEMINAR");
        typeBox.setValue("LECTURE");
        TextField locField = new TextField();
        locField.setPromptText("Location Name");

        // NEW: Coordinates for directions
        TextField xField = new TextField("0");
        xField.setPromptText("X Coord (East/West)");
        TextField yField = new TextField("0");
        yField.setPromptText("Y Coord (North/South)");

        ComboBox<String> facultyBox = new ComboBox<>();
        facultyBox.getItems().addAll("Computer Science", "History", "Math", "Psychology", "General");
        facultyBox.setValue(userFaculty);
        if (isAdmin && userFaculty != null && !userFaculty.equalsIgnoreCase("General")) {
            facultyBox.setDisable(true);
        }

        VBox layout = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Type:"), typeBox,
                new Label("Location:"), locField,
                new Label("Directions (Coordinates):"),
                new HBox(5, new Label("X:"), xField, new Label("Y:"), yField),
                new Label("Faculty:"), facultyBox
        );
        layout.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(layout);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                try {
                    // Parse coordinates
                    int x = Integer.parseInt(xField.getText());
                    int y = Integer.parseInt(yField.getText());

                    storage.addTimetableEntry(
                            nameField.getText(), typeBox.getValue(), day, startTime + ":00", 90,
                            locField.getText(), x, y,
                            facultyBox.getValue(), dataSource
                    );
                    return true;
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
                }
            }
            return false;
        });
        dialog.showAndWait().ifPresent(success -> {
            if (success) refreshSchedule();
        });
    }

    // 3. NEW: Edit Dialog (Feature 1)
    private void showEditDialog(TimetableEntry entry, Location currentLocation) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Class");
        dialog.setHeaderText("Editing: " + entry.getCourseName());

        // Define Buttons
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete Class", ButtonBar.ButtonData.RIGHT);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, deleteButtonType, ButtonType.CANCEL);

        // Fields
        TextField nameField = new TextField(entry.getCourseName());
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("LECTURE", "LAB", "SEMINAR");
        typeBox.setValue(entry.getType().name());

        TextField locField = new TextField(currentLocation != null ? currentLocation.getName() : entry.getLocationId());

        // Coordinate Fields (Pre-filled)
        String currentX = (currentLocation != null) ? String.valueOf(currentLocation.getX()) : "0";
        String currentY = (currentLocation != null) ? String.valueOf(currentLocation.getY()) : "0";

        TextField xField = new TextField(currentX);
        xField.setPromptText("X");
        TextField yField = new TextField(currentY);
        yField.setPromptText("Y");

        VBox layout = new VBox(10,
                new Label("Edit Name:"), nameField,
                new Label("Edit Type:"), typeBox,
                new Label("Edit Location:"), locField,
                new Label("Edit Coordinates (Directions):"),
                new HBox(5, new Label("X:"), xField, new Label("Y:"), yField)
        );
        layout.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(layout);

        // Style the Delete button
        final Button btDelete = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        btDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

        dialog.setResultConverter(btn -> btn);

        dialog.showAndWait().ifPresent(btn -> {
            try {
                // Formatting time for DB
                String fullStartTime = entry.getStartTime().length() == 5 ? entry.getStartTime() + ":00" : entry.getStartTime();

                if (btn == updateButtonType) {
                    // --- THE FIX IS HERE: Parse X and Y and pass them ---
                    int newX = Integer.parseInt(xField.getText());
                    int newY = Integer.parseInt(yField.getText());

                    storage.updateTimetableEntry(
                            entry.getCourseName(),      // 1. oldCourseName
                            entry.getDay(),             // 2. day
                            fullStartTime,              // 3. startTime
                            nameField.getText(),        // 4. newCourseName
                            typeBox.getValue(),         // 5. newType
                            locField.getText(),         // 6. locationName
                            newX,                       // 7. X (Added)
                            newY,                       // 8. Y (Added)
                            userFaculty,                // 9. faculty
                            dataSource                  // 10. dataSource
                    );
                    refreshSchedule();

                } else if (btn == deleteButtonType) {
                    // Delete Logic
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + entry.getCourseName() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.YES) {
                            try {
                                storage.deleteTimetableEntry(entry.getCourseName(), entry.getDay(), fullStartTime, dataSource);
                                refreshSchedule();
                            } catch (IOException e) {
                                new Alert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage()).show();
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            }
        });
    }
}