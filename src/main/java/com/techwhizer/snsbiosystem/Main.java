package com.techwhizer.snsbiosystem;

import com.techwhizer.snsbiosystem.app.AppConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

public class Main extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {

        primaryStage = stage;
        //    Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("testPagination.fxml")));
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("auth/login.fxml")));
        //    stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(AppConfig.APPLICATION_ICON))));
        stage.setTitle(AppConfig.APPLICATION_NAME);
        stage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
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

    public static void main(String[] args) throws ParseException, IOException {
       launch(args);

    }

}