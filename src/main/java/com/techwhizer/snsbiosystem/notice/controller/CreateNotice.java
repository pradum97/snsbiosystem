package com.techwhizer.snsbiosystem.notice.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.notice.model.NoticeBoardDTO;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.RoleConfigModel;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.DateAndTimePicker;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class CreateNotice implements Initializable {
    public TextArea messageTa;
    public CheckBox selectAllCb;
    public CheckBox adminCb;
    public CheckBox doctorCb;
    public CheckBox dealerCb;
    public CheckBox patientCb;
    public CheckBox guestCb;
    public ComboBox<Boolean> scheduledCom;
    public Button publishBn;
    public Label titleL;
    public Button expiryBn;
    public Button submitBn;
    public VBox publishDateContainer;
    public ProgressIndicator progressBar;
    public HBox buttonContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private ObservableList<Boolean> bool = FXCollections.observableArrayList(Boolean.FALSE, Boolean.TRUE);
    private RoleConfigModel role = new RoleConfigModel();
    private Long expiryDate;
    private Long publishDate;
    public final static String SELECT_PUBLISH_DATE = "SELECT PUBLISH DATE";
    public final static String SELECT_EXPIRY_DATE = "SELECT EXPIRY DATE";
    protected String previousPublishDateTime,previousExpiryDateTime;

    private OperationType operationType;
    private NoticeBoardDTO noticeBoardDTO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        method.hideElement(progressBar);
        messageTa.setFont(Font.font("Arial", 16));
        messageTa.setStyle("-fx-text-alignment: center;-fx-alignment: center;-fx-border-color: grey;-fx-border-radius: 3");
        checkBocConfig();
        comboboxConfig();
        buttonConfig();

        Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();
        operationType = (OperationType) map.get("operation_type");

        if (null == operationType) {
            customDialog.showAlertBox("", "Something went wrong..");
            Platform.runLater(() -> {
                Stage stage = (Stage) submitBn.getScene().getWindow();
                if (null != stage && stage.isShowing()) {
                    stage.close();
                }
            });
            return;
        }
        switch (operationType) {
            case CREATE -> {
                titleL.setText("CREATE NEW NOTICE");
                submitBn.setText("SUBMIT");
            }
            case UPDATE -> {
                titleL.setText("UPDATE NOTICE");
                noticeBoardDTO = (NoticeBoardDTO) map.get("notice_date");
                submitBn.setText("UPDATE");
                setTextFieldData(noticeBoardDTO);
            }
        }
    }

    private void setTextFieldData(NoticeBoardDTO noticeBoardDTO) {

        messageTa.setText(noticeBoardDTO.getStory().isEmpty() || null == noticeBoardDTO.getStory() ? "" : noticeBoardDTO.getStory());
        adminCb.setSelected(noticeBoardDTO.isForAdmins());
        dealerCb.setSelected(noticeBoardDTO.isForDealers());
        doctorCb.setSelected(noticeBoardDTO.isForDoctors());
        patientCb.setSelected(noticeBoardDTO.isForPatients());
        guestCb.setSelected(noticeBoardDTO.isForGuests());

        LocalDateTime publicDateTime = CommonUtility.getLocalDateTimeObject(noticeBoardDTO.getPublishOn());
        LocalDateTime expiryDateTime = CommonUtility.getLocalDateTimeObject(noticeBoardDTO.getExpiresOn());

        previousPublishDateTime = publicDateTime.format(CommonUtility.dateTimeFormator);
        previousExpiryDateTime =  expiryDateTime.format(CommonUtility.dateTimeFormator);

        publishBn.setText(previousPublishDateTime);
        expiryBn.setText(previousExpiryDateTime);

        scheduledCom.getSelectionModel().select(noticeBoardDTO.isScheduled());

        publishDate = noticeBoardDTO.getPublishOn();
        expiryDate = noticeBoardDTO.getExpiresOn();

        checkAllRoleSelected();
    }

    void checkAllRoleSelected() {

        if (adminCb.isSelected() && doctorCb.isSelected() & dealerCb.isSelected() &&
                patientCb.isSelected() && guestCb.isSelected()) {
            selectAllCb.setSelected(true);
        } else {
            selectAllCb.setSelected(false);
        }

    }

    private void buttonConfig() {
        publishBn.setStyle("-fx-font-size: 14");
        expiryBn.setStyle("-fx-font-size: 14");
        publishBn.setText(SELECT_PUBLISH_DATE);
        expiryBn.setText(SELECT_EXPIRY_DATE);

        DateAndTimePicker dateAndTimePicker = new DateAndTimePicker();

        publishBn.setOnAction(event -> {

            String dateString = dateAndTimePicker.pick(previousPublishDateTime);
            if (null != dateString) {
                previousPublishDateTime = dateString;
                publishBn.setText(dateString);
                LocalDateTime localDateTime = CommonUtility.getDateTimeObject(previousPublishDateTime);
                publishDate = CommonUtility.convertToUTCMillis(localDateTime);
            }
        });

        expiryBn.setOnAction(event -> {

            String dateString = dateAndTimePicker.pick(previousExpiryDateTime);

            if (null != dateString) {
                previousExpiryDateTime = dateString;
                expiryBn.setText(dateString);
                LocalDateTime localDateTime = CommonUtility.getDateTimeObject(dateString);
                expiryDate = CommonUtility.convertToUTCMillis(localDateTime);
            }
        });
    }

    private void comboboxConfig() {
        scheduledCom.valueProperty().addListener((observableValue, aBoolean, t1) -> {
            publishDateContainer.setDisable(!t1);
        });
        scheduledCom.setItems(bool);
        scheduledCom.getSelectionModel().selectFirst();
    }

    private void checkBocConfig() {

        adminCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            role.setAdmin(t1);
            checkAllRoleSelected();
        });
        doctorCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            role.setDoctor(t1);
            checkAllRoleSelected();
        });
        dealerCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            role.setDealer(t1);
            checkAllRoleSelected();
        });
        patientCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            role.setPatient(t1);
            checkAllRoleSelected();
        });
        guestCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            role.setGuest(t1);
            checkAllRoleSelected();
        });
        selectAllCb.setOnAction(event -> {
            if (selectAllCb.isSelected()) {
                adminCb.setSelected(true);
                doctorCb.setSelected(true);
                dealerCb.setSelected(true);
                patientCb.setSelected(true);
                guestCb.setSelected(true);
            } else {
                adminCb.setSelected(false);
                doctorCb.setSelected(false);
                dealerCb.setSelected(false);
                patientCb.setSelected(false);
                guestCb.setSelected(false);
            }
        });

    }

    public void cancelBn(ActionEvent event) {
        method.closeStage(submitBn);
    }

    public void submitClick(ActionEvent event) {

        String message = messageTa.getText();
        if (message.isEmpty()) {
            method.show_popup("Please enter message", messageTa);
            return;
        }

        Set<Boolean> roleSet = new HashSet<>(5);
        if (role.isAdmin()) {
            roleSet.add(true);
        }
        if (role.isDoctor()) {
            roleSet.add(true);
        }
        if (role.isDealer()) {
            roleSet.add(true);
        }
        if (role.isPatient()) {
            roleSet.add(true);
        }
        if (role.isGuest()) {
            roleSet.add(true);
        }

        if (roleSet.size() < 1) {
            method.show_popup("Please Choose At least one role", guestCb);
            return;
        }

        if (scheduledCom.getSelectionModel().getSelectedItem()) {
            if (null == publishDate) {
                method.show_popup("Please select publish Date", publishBn);
                return;
            }
        }

        if (null == expiryDate) {
            method.show_popup("Please select expiry Date", expiryBn);
            return;
        }

        NoticeBoardDTO notice = new NoticeBoardDTO();
        notice.setExpiresOn(expiryDate);
        notice.setForAdmins(role.isAdmin());
        notice.setForDealers(role.isDealer());
        notice.setForDoctors(role.isDoctor());
        notice.setForPatients(role.isPatient());
        notice.setForGuests(role.isGuest());
        notice.setStory(message);

        if (null != noticeBoardDTO) {
            notice.setId(noticeBoardDTO.getId());
        }

        if (scheduledCom.getSelectionModel().getSelectedItem()) {
            notice.setScheduled(true);
            notice.setPublishOn(publishDate);
        }

        String json = new Gson().toJson(notice);

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText(Message.CONFIRMATION_MESSAGE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            callThread(OperationType.CREATE, json);
        } else {
            alert.close();
        }
    }

    private void callThread(OperationType operationType, String json) {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute(json);
    }

    public void keyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            submitClick(null);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        public void onPreExecute() {

            progressBar.setVisible(true);
            method.hideElement(submitBn);
        }

        @Override
        public Boolean doInBackground(String... params) {
            String json = params[0];

            if (operationType == OperationType.CREATE) {
                createNotice(json);
            } else {
                updateNotice(json);
            }

            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            submitBn.setVisible(true);
            method.hideElement(progressBar);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void updateNotice(String json) {

        try {
            String token = (String) Login.authInfo.get("token");

            HttpPut httpPut = new HttpPut(UrlConfig.getKitNoticeUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
            httpPut.setEntity(se);

            HttpResponse response = CommonUtility.httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                method.hideElement(progressBar);
                submitBn.setVisible(true);

                if (statusCode == 200) {
                    Platform.runLater(() -> {
                        resetAllField();
                        customDialog.showAlertBox("Success", "Notice successfully updated");
                        Main.primaryStage.setUserData(true);
                        cancelBn(null);
                    });
                }else {
                    customDialog.showAlertBox("Failed", content);
                }

            }
        } catch (Exception e) {
            method.hideElement(progressBar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    private void createNotice(String json) {


        try {
            Thread.sleep(100);
            HttpPost httpPost = new HttpPost(UrlConfig.getKitNoticeUrl());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cookie", (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json,StandardCharsets.UTF_8);
            httpPost.setEntity(se);
            HttpResponse response = CommonUtility.httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                method.hideElement(progressBar);
                submitBn.setVisible(true);

                Platform.runLater(() -> {
                    if (statusCode == 200) {
                        customDialog.showAlertBox("Success", "Notice successfully created");
                        resetAllField();
                    } else {
                        customDialog.showAlertBox("Failed.", content);
                    }

                });
            }
        } catch (Exception e) {
            method.hideElement(progressBar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    private void resetAllField() {
        Main.primaryStage.setUserData(true);
        messageTa.setText("");
        adminCb.setSelected(false);
        doctorCb.setSelected(false);
        dealerCb.setSelected(false);
        patientCb.setSelected(false);
        guestCb.setSelected(false);
        publishBn.setText(SELECT_PUBLISH_DATE);
        expiryBn.setText(SELECT_EXPIRY_DATE);

        expiryDate = null;
        publishDate = null;
    }
}
