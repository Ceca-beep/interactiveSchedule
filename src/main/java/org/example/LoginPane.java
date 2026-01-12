package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class LoginPane extends VBox {

    // Custom interface to pass 3 values: Success, IsAdmin, FacultyName
    public interface LoginCallback {
        void onLogin(boolean success, boolean isAdmin, String faculty);
    }

    public LoginPane(JdbcStorage storage, String dataSource, LoginCallback onResult) {
        setSpacing(10);
        setPadding(new Insets(20));

        Label title = new Label("University Schedule System");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Account Name:");
        TextField nameField = new TextField();

        Label passLabel = new Label("Password (Admin only):");
        PasswordField passField = new PasswordField();

        Label facultyLabel = new Label("Faculty (Students only):");
        TextField facultyField = new TextField();

        Label status = new Label();
        status.setStyle("-fx-text-fill: red;");

        Button login = new Button("Login");
        login.setDefaultButton(true);

        // REPLACE THE login.setOnAction BLOCK IN LoginPane.java

        login.setOnAction(evt -> {
            String name = nameField.getText().trim();
            String password = passField.getText().trim();
            String faculty = facultyField.getText().trim(); // Now mandatory for Admins too!

            if (name.isEmpty()) {
                status.setText("Please enter a name.");
                return;
            }

            // 1. ADMIN CHECK (Now Faculty-Specific)
            if (name.equalsIgnoreCase("admin")) {
                if (password.equals("admin123")) {
                    if (faculty.isEmpty()) {
                        status.setText("Admins must specify a Faculty.");
                        return;
                    }
                    // Success! Log in as Admin for THAT SPECIFIC faculty
                    onResult.onLogin(true, true, faculty);
                } else {
                    status.setText("Invalid admin password.");
                }
                return;
            }

            // 2. STUDENT CHECK
            try {
                boolean ok = storage.accountExists(name, faculty.isEmpty() ? null : faculty, dataSource);
                if (ok) {
                    onResult.onLogin(true, false, faculty);
                } else {
                    status.setText("Account not found.");
                    onResult.onLogin(false, false, null);
                }
            } catch (IOException e) {
                status.setText("DB error: " + e.getMessage());
                onResult.onLogin(false, false, null);
            }
        });

        getChildren().addAll(title, nameLabel, nameField, passLabel, passField, facultyLabel, facultyField, login, status);
    }
}