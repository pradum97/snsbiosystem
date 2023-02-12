package com.techwhizer.snsbiosystem.util;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OptionalMethod {


    public static void minimizedStage(Stage stage ,boolean bool){
        stage.setMaximized(bool);
    }

    public void convertDateFormat(DatePicker... date) {
        for (DatePicker datePicker : date) {
            datePicker.setConverter(new StringConverter<>() {
                private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CommonUtility.DATEPICKER_DATE_FORMAT);

                @Override
                public String toString(LocalDate localDate) {
                    if (localDate == null)
                        return "";
                    return dateTimeFormatter.format(localDate);
                }

                @Override
                public LocalDate fromString(String dateString) {
                    if (dateString == null || dateString.trim().isEmpty()) {
                        return null;
                    }
                    return LocalDate.parse(dateString, dateTimeFormatter);
                }
            });
        }
    }

    public ProgressIndicator getProgressBar(double height, double width) {
        ProgressIndicator pi = new ProgressIndicator();
        pi.indeterminateProperty();
        pi.setPrefHeight(height);
        pi.setPrefWidth(width);

        return pi;
    }

    public void hideElement(Node... node) {
       for (Node n : node){
            n.setVisible(false);
            n.managedProperty().bind(n.visibleProperty());
        }
    }
    public ContextMenu show_popup(String message, Object textField) {
        ContextMenu form_Validator = new ContextMenu();
        form_Validator.setAutoHide(true);
        form_Validator.getItems().add(new MenuItem(message));
        form_Validator.show((Node) textField, Side.RIGHT, 10, 0);
        return form_Validator;
    }

    public void closeStage(Node node) {

        Stage stage = (Stage) node.getScene().getWindow();
        if (stage.isShowing()) {
            stage.close();
        }
    }

    public void selectTable(int index, TableView tableView) {

        if (!tableView.getSelectionModel().isEmpty()) {
            tableView.getSelectionModel().clearSelection();
        }

        tableView.getSelectionModel().select(index);
    }

}
