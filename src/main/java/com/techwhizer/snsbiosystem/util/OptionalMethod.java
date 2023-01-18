package com.techwhizer.snsbiosystem.util;

import com.techwhizer.snsbiosystem.CustomDialog;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OptionalMethod {

    public ProgressIndicator getProgressBar(double height , double width){
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

    public String getTempFile() {
        try {
            String folderLocation = System.getenv("temp");
            String fileName = "inv.pdf";
            File temp = new File(folderLocation + File.separator + fileName);
            return temp.getAbsolutePath();

        } catch (Exception e) {
            try {
                String tempPath = Files.createTempFile(null, ".pdf").toString();
                return tempPath;
            } catch (IOException ex) {

                String fileName = "inv.pdf";

                String path = System.getProperty("user.dashboardBn") + "\\invoice\\";
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }

                String fullPath = path + fileName;

                return fullPath;
            }

        }
    }

    public String removeZeroAfterDecimal(Object o) {
        return new BigDecimal(String.valueOf(o)).stripTrailingZeros().toPlainString();
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

    public String rec(String str){
        String txt = "";

        if (null == str || str.isEmpty()) {
            txt = "-";
        } else {
            txt = str;
        }
        return txt;
    }

    public String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String decimalFormatter(Object o) {
        DecimalFormat formatter = new DecimalFormat("#0.0");
        return formatter.format(o);
    }

}
