// java
package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

/*
 Minimal ScheduleView implementation so MainApp can instantiate it.
 Replace the placeholder loading code with your real timetable rendering.
*/
public class ScheduleView extends VBox {
    private final JdbcStorage storage;
    private final String dataSource;

    public ScheduleView(JdbcStorage storage, String dataSource) {
        super(10);
        this.storage = storage;
        this.dataSource = dataSource;
        setPadding(new Insets(10));

        Label title = new Label("Schedule");
        ListView<String> list = new ListView<>();

        // Placeholder: load actual entries from storage here
        list.getItems().add("No schedule loaded (replace with real load)");
        // Example: list.getItems().addAll(loadScheduleStrings());

        getChildren().addAll(title, list);
    }

    // Optional: add real loading methods that use storage to populate the UI
}
