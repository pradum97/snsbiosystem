package com.techwhizer.snsbiosystem.notice.controller;

import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.notice.model.NoticeBoardDTO;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class ViewNotice implements Initializable {
    public ListView roleListView;
    public Label publishDateL;
    public Label expiryDateL;
    public Label scheduleL;
    public TextArea messageTa;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageTa.setFont(Font.font("Arial", 16));

        NoticeBoardDTO noticeBoardDTO = (NoticeBoardDTO) Main.primaryStage.getUserData();

        if (null != noticeBoardDTO && Main.primaryStage.getUserData() instanceof NoticeBoardDTO ){

            setData(noticeBoardDTO);
        }

    }

    private void setData(NoticeBoardDTO notice) {

        LocalDateTime expiryDateTime = CommonUtility.getLocalDateTimeObject(notice.getExpiresOn());
        LocalDateTime publishDateTime = CommonUtility.getLocalDateTimeObject(notice.getPublishOn());

        String expiryDate = expiryDateTime.format(CommonUtility.dateTimeFormator);
        String publishDate = publishDateTime.format(CommonUtility.dateTimeFormator);

        messageTa.setText(notice.getStory());
        publishDateL.setText(publishDate);
        expiryDateL.setText(expiryDate);
        scheduleL.setText(notice.isScheduled()?"YES":"NO");

        Set<String> role = new HashSet<>();
        if (notice.isForAdmins()){role.add("ADMIN");}
        if (notice.isForDoctors()){role.add("DOCTOR");}
        if (notice.isForDealers()){role.add("DEALER");}
        if (notice.isForPatients()){role.add("PATIENT");}
        if (notice.isForGuests()){role.add("GUEST");}

        roleListView.setItems(FXCollections.observableArrayList(role));

    }
}
