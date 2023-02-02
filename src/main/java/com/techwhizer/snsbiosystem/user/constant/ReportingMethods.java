package com.techwhizer.snsbiosystem.user.constant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReportingMethods {
    public final static String FAX = "FAX";
    public final static String EMAIL ="EMAIL";
    public final static String MAIL = "MAIL";


    public static ObservableList<String> reportShareMethod = FXCollections.observableArrayList(FAX,EMAIL,MAIL);


}
