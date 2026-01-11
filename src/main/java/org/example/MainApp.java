package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Determine data source (JDBC_URL env or default)
        String envUrl = System.getenv("JDBC_URL");
        String dataSource = envUrl != null && !envUrl.isBlank()
                ? envUrl
                : "jdbc:mysql://localhost:3306/university_schedule?user=root&password=root";

        JdbcStorage storage = new JdbcStorage();

        LoginPane login = new LoginPane(storage, dataSource, success -> {
            if (success) {
                ScheduleView sv = new ScheduleView(storage, dataSource);
                Scene s = new Scene(sv, 1000, 700);
                primaryStage.setTitle("Faculty Schedule");
                primaryStage.setScene(s);
                primaryStage.show();
            }
        });

        Scene scene = new Scene(login, 400, 200);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

