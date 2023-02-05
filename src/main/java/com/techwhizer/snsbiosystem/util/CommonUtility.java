package com.techwhizer.snsbiosystem.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class CommonUtility {
    public final static String COMMON_DATE_PATTERN = "yyyy/MM/dd";
    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String DATEPICKER_DATE_FORMAT = "yyyy/MM/dd";

    public final static String ORDER_ASC = "ASCENDING";
    public final static String ORDER_DESC = "DESCENDING";
    public static void passwordMaskFiled(TextField textField, ImageView icon) {
        textField.setManaged(false);
        textField.setVisible(false);
        final PasswordField passwordField = new PasswordField();

        passwordField.prefHeight(textField.getPrefHeight());
        passwordField.prefWidth(textField.getPrefWidth());

        textField.managedProperty().bind(icon.pressedProperty());
        textField.visibleProperty().bind(icon.pressedProperty());

        passwordField.managedProperty().bind(icon.pressedProperty().not());
        passwordField.visibleProperty().bind(icon.pressedProperty().not());
        textField.textProperty().bindBidirectional(passwordField.textProperty());
    }


    public static void onHoverShowTextButton(Button node, String text){

        if (node.isVisible()) {
            Tooltip t = new Tooltip(text);
            node.setTooltip(t);
        }
    }

    public static void onHoverShowTextLabel(Label node, String text){

        if (node.isVisible()) {
            Tooltip t = new Tooltip(text);
            node.setTooltip(t);
        }
    }

    public final static String SUBMIT_CONFIRMATION = "Are you sure, you want to submit?";

    public static String getCutText(String text){

        int maxLength = 180;
        String str = "";

        if (text.length() > 180){
            str  = text.substring(0,maxLength)+"...";
        }else {
            str = text ;
        }

        return str;
    }

    public static ObservableList<String> orderList = FXCollections.observableArrayList(ORDER_ASC, ORDER_DESC);
    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATEPICKER_DATE_FORMAT);
    public static DateTimeFormatter dateTimeFormator = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec("easy").build()).build();

    public final static String EMPTY_LABEL_FOR_TABLE = "N/A";
    public final static String ALL = "ALL";

    public static String parserOrder(String str) {
        String order = null;

        switch (str) {
            case ORDER_ASC -> order = "asc";
            case ORDER_DESC -> order = "desc";
        }
        return order;
    }

    public static String formatLocalDate(LocalDate dateTime) {
        return dateTime.format(dateFormatter);
    }

    public static String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime.format(dateFormatter);
    }


    public static LocalDateTime getLocalDateTimeObject(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    public static long convertToUTCMillis(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    public static LocalDate getLocalDateObject(String dateTime) {
        return LocalDate.parse(dateTime, dateFormatter);
    }

    public static LocalDateTime getCurrentUTCDateTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static LocalDateTime getDateTimeObject(String dateTime) {
        LocalDateTime localDateTime = null;
        if (dateTimeFormator == null) {
            dateTimeFormator = DateTimeFormatter.ofPattern(DATE_FORMAT);
        }
        localDateTime = LocalDateTime.parse(dateTime, dateTimeFormator);
        return localDateTime;
    }
}
