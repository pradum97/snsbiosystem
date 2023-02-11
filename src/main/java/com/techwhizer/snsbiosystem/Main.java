package com.techwhizer.snsbiosystem;

import com.techwhizer.snsbiosystem.app.AppConfig;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class Main extends Application {
    public static Stage primaryStage;
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("auth/login.fxml")));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(AppConfig.APPLICATION_ICON))));
        primaryStage.setTitle(AppConfig.APPLICATION_NAME);
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);

    }
    public void changeScene(String fxml, String title) {

        try {
            if (null != primaryStage && primaryStage.isShowing()) {
                Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
                primaryStage.getScene().setRoot(pane);
                Platform.runLater(() -> {
                    primaryStage.setTitle(AppConfig.APPLICATION_NAME + " ( " + title + " ) ");
                });
                primaryStage.show();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

}
