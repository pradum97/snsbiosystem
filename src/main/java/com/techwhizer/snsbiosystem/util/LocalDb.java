package com.techwhizer.snsbiosystem.util;

import com.techwhizer.snsbiosystem.model.RolesModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocalDb {


    public ObservableList<String> getUserSearchType() {
        return FXCollections.observableArrayList("CLIENT ID","NAME","EMAIL","PHONE","ADDRESS");
    }
    public ObservableList<String> getFilterType() {
        return FXCollections.observableArrayList("ALL", "ROLE_ADMIN", "ROLE_DEALER", "ROLE_DOCTOR", "ROLE_PATIENT");
    }
    public ObservableList<String> getRoleType() {
        return FXCollections.observableArrayList("ROLE_ADMIN", "ROLE_DEALER", "ROLE_DOCTOR", "ROLE_PATIENT");
    }


    public ObservableList<String> getSortingType() {
        return FXCollections.observableArrayList("ASCENDING", "DESCENDING");
    }


}
