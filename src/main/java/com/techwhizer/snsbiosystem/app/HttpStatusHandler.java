package com.techwhizer.snsbiosystem.app;

import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.util.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

public class HttpStatusHandler {

    private CustomDialog customDialog;

    {
        customDialog = new CustomDialog();
    }

    public HttpStatusHandler(int statusCode) {
        handle(statusCode);
    }

    private void handle(int statusCode) {
        if (statusCode == 401) {
            Platform.runLater(this::unauthorisedHandel);
        }
    }

    private void unauthorisedHandel() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Session expired");
        alert.setHeaderText("");
        Label msg = new Label(Message.UNAUTHORISED_MESSAGE);

        msg.setStyle("-fx-font-size: 16;-fx-font-weight: bold;");

        ImageView iv = new ImageLoader().loadImageView("img/icon/unauthorized_ic.png");
        iv.setFitHeight(70);
        iv.setFitWidth(70);


        VBox vBox = new VBox(iv,msg,new Separator());
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);

        alert.setGraphic(null);
        alert.getDialogPane().setContent(vBox);
        alert.getDialogPane().setPrefSize(380, 300);
        final Button exitButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        final Button loginButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);

        alert.getDialogPane().setStyle("-fx-border-radius: 10;-fx-border-color: red;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
        loginButton.setText("LOGIN");
        exitButton.setText("EXIT");

        HBox.setMargin(exitButton,new Insets(0,50,0,0));
        HBox.setMargin(loginButton,new Insets(0,70,0,0));

        loginButton.setStyle("-fx-cursor: hand;-fx-background-color: #006666;" +
                "-fx-text-fill: white;-fx-font-weight: bold");

        exitButton.setStyle("-fx-cursor: hand;-fx-background-color: red;" +
                "-fx-text-fill: white;-fx-font-weight: bold");

        loginButton.addEventFilter(ActionEvent.ACTION, ae -> {
            new Main().changeScene("auth/login.fxml", "LOGIN HERE");
        });
        exitButton.addEventFilter(ActionEvent.ACTION, ae -> System.exit(0));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setOnCloseRequest(new EventHandler<>() {
            @Override
            public void handle(DialogEvent dialogEvent) {

                new Main().changeScene("auth/login.fxml", "LOGIN HERE");
            }
        });
        alert.showAndWait();
    }


}

