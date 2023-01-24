package com.techwhizer.snsbiosystem.dialog;

import com.techwhizer.snsbiosystem.Main;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public final class Toast {
    public static void makeText( String toastMsg, int toastDelay , String backgroundColorCode) {

        int fadeInDelay =100 , fadeOutDelay  =100;
        Stage toastStage=new Stage();
        toastStage.initOwner(Main.primaryStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Text text = new Text(toastMsg);
        text.setFont(Font.font("Verdana", 25));
        text.setFill(Color.WHITE);
        StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 20;-fx-alignment: center; -fx-background-color: "+backgroundColorCode+";" +
                "-fx-text-fill: white; -fx-padding: 15px 70px 15px 70px;");
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);
        toastStage.show();

        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay), new KeyValue (toastStage.getScene().getRoot().opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey1);
        fadeInTimeline.setOnFinished((ae) -> new Thread(() -> {
            try{
                Thread.sleep(toastDelay);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            Timeline fadeOutTimeline = new Timeline();
            KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay), new KeyValue (toastStage.getScene().getRoot().opacityProperty(), 0));
            fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
            fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
            fadeOutTimeline.play();
        }).start());
        fadeInTimeline.play();
    }
}