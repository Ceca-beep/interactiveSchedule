package org.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        String envUrl = System.getenv("JDBC_URL");
        String dataSource = envUrl != null && !envUrl.isBlank()
                ? envUrl
                : "jdbc:mysql://localhost:3306/university_schedule?user=root&password=root";

        JdbcStorage storage = new JdbcStorage();

        showLoginScreen(primaryStage, storage, dataSource);
    }

    private void showLoginScreen(Stage stage, JdbcStorage storage, String dataSource) {

        LoginPane login = new LoginPane(storage, dataSource, (success, isAdmin, userFaculty) -> {
            if (success) {
                ScheduleView sv = new ScheduleView(storage, dataSource, isAdmin, userFaculty, () -> {
                    showLoginScreen(stage, storage, dataSource);
                });

                javafx.scene.Scene s = new javafx.scene.Scene(sv, 1000, 700);
                String title = "Faculty Schedule";
                if (isAdmin) title += " (ADMIN MODE)";
                else title += " - " + userFaculty + " Department";

                stage.setTitle(title);
                stage.setScene(s);
                stage.show();
            }
        });

        javafx.scene.Scene scene = new javafx.scene.Scene(login, 400, 350);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

