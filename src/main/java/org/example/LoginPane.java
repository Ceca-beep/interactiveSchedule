package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class LoginPane extends VBox {


    public interface LoginCallback {
        void onLogin(boolean success, boolean isAdmin, String faculty);
    }


    private final JdbcStorage storage;
    private final String dataSource;
    private final LoginCallback callback;

    public LoginPane(JdbcStorage storage, String dataSource, LoginCallback onResult) {
        this.storage = storage;
        this.dataSource = dataSource;
        this.callback = onResult;

        setSpacing(10);
        setPadding(new Insets(20));

        Label title = new Label("University Schedule System");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Account Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your username");

        Label passLabel = new Label("Password (Admin only):");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Leave blank if student");

        Label facultyLabel = new Label("Faculty (Optional for login):");
        TextField facultyField = new TextField();
        facultyField.setPromptText("e.g. History");

        Label status = new Label();
        status.setStyle("-fx-text-fill: red;");

        Button login = new Button("Login");
        login.setDefaultButton(true);

        login.setOnAction(evt -> {
            String name = nameField.getText().trim();
            String password = passField.getText().trim();
            String faculty = facultyField.getText().trim();

            if (name.isEmpty()) {
                status.setText("Please enter a name.");
                return;
            }


            if (name.equalsIgnoreCase("admin")) {
                if (password.equals("admin123")) {
                    if (faculty.isEmpty()) {
                        status.setText("Admins must specify a Faculty.");
                        return;
                    }
                    callback.onLogin(true, true, faculty);
                } else {
                    status.setText("Invalid admin password.");
                }
                return;
            }


            try {

                boolean exists = storage.accountExists(name, faculty.isEmpty() ? null : faculty, dataSource);

                if (exists) {

                    callback.onLogin(true, false, faculty);
                } else {

                    showCreateAccountDialog(name, faculty);
                }

            } catch (IOException e) {
                status.setText("DB error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        getChildren().addAll(title, nameLabel, nameField, passLabel, passField, facultyLabel, facultyField, login, status);
    }


    private void showCreateAccountDialog(String initialName, String initialFaculty) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Account Not Found");
        dialog.setHeaderText("User '" + initialName + "' does not exist.\nDo you want to create a new account?");

        ButtonType createButton = new ButtonType("Create & Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);


        TextField nameField = new TextField(initialName);
        nameField.setPromptText("Your Name");

        ComboBox<String> facultyBox = new ComboBox<>();
        facultyBox.getItems().addAll(
                "Computer Science", "History", "Math", "Psychology", "General",
                "Medicine", "Biology", "Chemistry", "Physics", "Mathematics"
        );
        facultyBox.setEditable(true); // Allow typing custom faculties if needed
        if (initialFaculty != null && !initialFaculty.isEmpty()) {
            facultyBox.setValue(initialFaculty);
        } else {
            facultyBox.setPromptText("Select Faculty");
        }

        VBox content = new VBox(10,
                new Label("Confirm Name:"), nameField,
                new Label("Select Faculty:"), facultyBox
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);


        dialog.setResultConverter(btn -> {
            if (btn == createButton) {
                String newName = nameField.getText().trim();
                String newFaculty = facultyBox.getValue();

                if (newName.isEmpty() || newFaculty == null || newFaculty.trim().isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Name and Faculty are required.").show();
                    return null;
                }

                try {

                    storage.createStudentIfNotExists(newName, newFaculty, dataSource);
                    callback.onLogin(true, false, newFaculty);
                    return true;
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to create account: " + e.getMessage()).show();
                }
            }
            return false;
        });

        dialog.showAndWait();
    }
}