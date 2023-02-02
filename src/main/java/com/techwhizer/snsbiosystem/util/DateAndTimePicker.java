package com.techwhizer.snsbiosystem.util;

import com.techwhizer.snsbiosystem.CssLoader;
import com.techwhizer.snsbiosystem.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateAndTimePicker {
    public String pick(String defaultDate) {

        final String[] dateTime = {null};
        try {
            BorderPane root = new BorderPane();
            DatePicker dp = new DatePicker();

            if (null != defaultDate) {
                String[] s = defaultDate.split(" ");
                String date = s[0];

                if (null != date) {
                    LocalDate ld = CommonUtility.getLocalDateObject(date);
                    dp.setValue(ld);
                }
            } else {

                LocalDateTime localDateTime = CommonUtility.getCurrentUTCDateTime();
                LocalDate ld = CommonUtility.getLocalDateObject(CommonUtility.formatLocalDateTime(localDateTime));
                dp.setValue(ld);
            }

            new OptionalMethod().convertDateFormat(dp);
            Scene scene = new Scene(root, 470, 450);

            DatePickerSkin datePickerSkin = new DatePickerSkin(dp);
            Node popupContent = datePickerSkin.getPopupContent();
            popupContent.getStyleClass().add("popupContent");

            root.setCenter(popupContent);

            HBox title = new HBox(new Label("SELECT TIME"));
            title.setStyle("-fx-font-size: 15;-fx-alignment: center;-fx-font-weight: bold");

            HBox topTitle = new HBox(new Label("SELECT DATE"));
            topTitle.setStyle("-fx-font-size: 15;-fx-alignment: center;-fx-font-weight: bold");

            root.setStyle("-fx-border-color: black;-fx-border-radius: 5");

            Label labelHour = new Label("HOUR:");
            Label labelMinute = new Label("MINUTE:");
            Label labelSecond = new Label("SECOND:");
            labelHour.setMinWidth(50);
            setLabelStyle(labelHour, labelMinute, labelSecond);

            TextField hourTf = new TextField();
            TextField minuteTf = new TextField();
            TextField secondTf = new TextField();
            hourTf.setPromptText("00");
            minuteTf.setPromptText("00");
            secondTf.setPromptText("00");
            hourTf.setId("hour");
            minuteTf.setId("minute");
            secondTf.setId("second");
            setTextFieldListener(hourTf, minuteTf, secondTf);

            if (null != defaultDate) {

                try {
                    String[] s = defaultDate.split(" ");
                    String[] time = s[1].split(":");

                    String hour = time[0];
                    String minute = time[1];
                    String second = time[2];
                    hourTf.setText(null == hour || hour.equals("00") || hour.isEmpty() ? "" : hour);
                    minuteTf.setText(null == minute || minute.equals("00") || minute.isEmpty() ? "" : minute);
                    secondTf.setText(null == second || second.equals("00") || second.isEmpty() ? "" : second);

                } catch (Exception e) {
                }
            }

            HBox hBoxHour = new HBox(labelHour, hourTf);
            HBox hBoxMinute = new HBox(labelMinute, minuteTf);
            HBox hBoxSecond = new HBox(labelSecond, secondTf);
            setTfContainerStyle(hBoxHour, hBoxMinute, hBoxSecond);

            HBox fieldContainer = new HBox(hBoxHour, hBoxMinute, hBoxSecond);
            setMainContainerStyle(fieldContainer);

            Button okBn = new Button("OK");
            Button cancelBn = new Button("CANCEL");
            actionButtonStyle(cancelBn, okBn);

            cancelBn.setOnAction((event -> {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                if (null != stage && stage.isShowing()) {
                    stage.close();
                }
            }));

            okBn.setOnAction(event -> {

                String date = CommonUtility.formatLocalDate(dp.getValue());

                if (date.isEmpty()) {
                    new OptionalMethod().show_popup("Please select date and time", okBn);
                    return;
                }

                String hourI = hourTf.getText();
                String minI = minuteTf.getText();
                String secI = secondTf.getText();

                OptionalMethod method = new OptionalMethod();

                if (!hourI.isEmpty()) {
                    if (!(hourI.length() <= 24)) {
                        method.show_popup("Please enter a maximum of 24 hours.", hourTf);
                        return;
                    }
                }

                if (!minI.isEmpty()) {
                    if (!(minI.length() <= 60)) {
                        method.show_popup("Please enter a maximum of 60 minutes.", minuteTf);
                        return;
                    }
                }
                if (!secI.isEmpty()) {
                    if (!(secI.length() <= 60)) {
                        method.show_popup("Please enter a maximum of 60 seconds.", secondTf);
                        return;
                    }
                }

                String hourStr = hourI.isEmpty() ? "00" : hourI.length() < 2 ? "0" + hourI : hourI;
                String minuteString = minI.isEmpty() ? "00" : minI.length() < 2 ? "0" + minI : minI;
                String secondString = secI.isEmpty() ? "00" : secI.length() < 2 ? "0" + secI : secI;

                String time = hourStr + ":" + minuteString + ":" + secondString;
                dateTime[0] = date + " " + time;
                new OptionalMethod().closeStage(okBn);

            });

            HBox bottomContainer = new HBox(cancelBn, okBn);
            bottomContainer.setAlignment(Pos.CENTER);
            bottomContainer.setSpacing(50);

            VBox vBox = new VBox(title, new Separator(), fieldContainer, new Separator(), bottomContainer);
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10));
            vBox.setSpacing(10);

            root.setBottom(vBox);
            root.setTop(topTitle);

            new CssLoader().set(scene, "date_time_picker.css");
            Stage stage = new Stage();
            stage.initOwner(Main.primaryStage);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        return dateTime[0];
    }

    private void actionButtonStyle(Button cancelBn, Button okBn) {
        okBn.setStyle("-fx-background-color: #04238a;-fx-text-fill: white;-fx-font-weight: bold;" +
                "-fx-min-width: 100;-fx-min-height: 23;-fx-cursor: hand");

        cancelBn.setStyle("-fx-background-color: rgba(180,3,3,0.75);-fx-text-fill: white;-fx-font-weight: bold;" +
                "-fx-min-width: 100;-fx-min-height: 23;-fx-cursor: hand");
    }
    private void setTextFieldListener(Node... nodes) {
        for (Node node : nodes) {
            TextField tf = (TextField) node;
            tf.setEditable(true);
            tf.setStyle("-fx-pref-height: 30;-fx-min-height: 0;-fx-pref-width: 80;" +
                    "-fx-font-size: 16;" + "-fx-border-color: black;-fx-border-radius: 3;" +
                    "-fx-alignment: center;-fx-text-alignment: center");
            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    tf.setText(oldValue);
                } else {

                    if (!(newValue.length() < 3)) {
                        tf.setText(oldValue);
                    }
                }

            });
        }
    }
    private void setMainContainerStyle(HBox mainContainer) {
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(10);
        mainContainer.setFillHeight(false);
    }

    private void setTfContainerStyle(Node... nodes) {
        for (Node node : nodes) {
            node.setStyle(";-fx-alignment: center;-fx-spacing: 5");
        }
    }

    private void setLabelStyle(Node... nodes) {
        for (Node node : nodes) {
            node.setStyle("-fx-font-weight: bold;-fx-alignment: center");
        }
    }

}
