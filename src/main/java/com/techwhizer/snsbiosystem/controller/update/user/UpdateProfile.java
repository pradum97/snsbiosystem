package com.techwhizer.snsbiosystem.controller.update.user;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateProfile implements Initializable {

    public ListView<CheckBox> roleLv;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<CheckBox> list = FXCollections.observableArrayList(new CheckBox("item-1"),
                new CheckBox("item-2"),new CheckBox("item-3"));

        roleLv.setItems(list);

    }

    public void updateBnClick(ActionEvent event) {

        System.out.println( roleLv.getSelectionModel().getSelectedItems().size());

       ;
    }

    public void cancelBnClick(ActionEvent event) {
    }
}
