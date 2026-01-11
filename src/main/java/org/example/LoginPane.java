package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class LoginPane extends VBox {
    public LoginPane(JdbcStorage storage, String dataSource, Consumer<Boolean> onResult) {
        setSpacing(10);
        setPadding(new Insets(10));

        Label title = new Label("Please sign in");
        Label nameLabel = new Label("Account name:");
        TextField nameField = new TextField();
        Label facultyLabel = new Label("Faculty:");
        TextField facultyField = new TextField();
        Label status = new Label();

        Button login = new Button("Login");
        login.setOnAction(evt -> {
            String name = nameField.getText().trim();
            String faculty = facultyField.getText().trim();
            if (name.isEmpty()) {
                status.setText("Please enter a name.");
                return;
            }
            try {
                boolean ok = storage.accountExists(name, faculty.isEmpty() ? null : faculty, dataSource);
                if (ok) {
                    status.setText("Login successful.");
                    onResult.accept(true);
                } else {
                    status.setText("Account not found. Check name/faculty.");
                    onResult.accept(false);
                }
            } catch (IOException e) {
                status.setText("DB error: " + e.getMessage());
                onResult.accept(false);
            }
        });

        getChildren().addAll(title, nameLabel, nameField, facultyLabel, facultyField, login, status);
    }
}

