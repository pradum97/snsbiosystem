package com.techwhizer.snsbiosystem.kit.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.model.AddKitResponse;
import com.techwhizer.snsbiosystem.kit.model.KitDTO;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.LocalDb;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddKit implements Initializable {

    public Button submitBn;
    public ProgressIndicator progressbar;
    public TextField clientIdTf;
    public TextField dealerIdTf;
    public TextField kitNumberTf;
    public DatePicker expiryDateDp;
    public TextField testUsedTf;
    public TextField lotNumberTf;
    public Label titleL;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private LocalDb localDb;
    private  KitDTO kd;
    private OperationType operationType;
    private HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec("easy").build()).build();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        localDb = new LocalDb();
        method.hideElement(progressbar);
        method.convertDateFormat(expiryDateDp);

        Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();
        operationType = (OperationType) map.get("operation_type");

        if (null == operationType && null == Main.primaryStage.getUserData()) {
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
                titleL.setText("ADD KIT");
                submitBn.setText("SUBMIT");
            }
            case UPDATE -> {
                titleL.setText("UPDATE KIT");
                kd = (KitDTO) map.get("kits_data");
                submitBn.setText("UPDATE");
                setTextFieldData();
            }
        }
    }

    private void setTextFieldData() {

        clientIdTf.setText(kd.getClientID() == null ? "" : String.valueOf(kd.getClientID()));
        dealerIdTf.setText(null == kd.getDealerID() ? "" : String.valueOf(kd.getDealerID()));
        kitNumberTf.setText(null == kd.getKitNumber() ? "" : String.valueOf(kd.getKitNumber()));
        testUsedTf.setText(null == kd.getTestUsed() ? "" : String.valueOf(kd.getTestUsed()));
        lotNumberTf.setText(null == kd.getLotNumber() ? "" : String.valueOf(kd.getLotNumber()));

        if (null != kd.getExpiryDate()) {
            String date = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN).format(new java.util.Date(kd.getExpiryDate()));
            expiryDateDp.getEditor().setText(date);
        }
    }

    public void cancelBnClick(ActionEvent event) {
        Stage stage = (Stage) submitBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void submitBnClick(ActionEvent event) {
        String msg = "Please enter valid number or number format.";

        String clientId = clientIdTf.getText();
        String dealerId = dealerIdTf.getText();
        String kitNumber = kitNumberTf.getText();
        String expiryDate = expiryDateDp.getEditor().getText();
        String testUsed = testUsedTf.getText();
        String lotNumber = lotNumberTf.getText();

        Long clientIdL = null, dealerIdL = null,
                kitNumberL = null, lotNumberL = null, expiryDateL = null;
        Integer testUsedI = null;

        if (clientId.isEmpty()) {
            method.show_popup("Please enter client id", clientIdTf);
            return;
        }
        try {
            clientIdL = Long.parseLong(clientId);
        } catch (NumberFormatException e) {
            method.show_popup(msg, clientIdTf);
            return;
        }

        if (!dealerId.isEmpty()) {
            try {
                dealerIdL = Long.parseLong(dealerId);
            } catch (NumberFormatException e) {
                method.show_popup(msg, dealerIdTf);
                return;
            }
        }

        if (kitNumber.isEmpty()) {
            method.show_popup("Please enter kit number", kitNumberTf);
            return;
        }

        try {
            kitNumberL = Long.parseLong(kitNumber);
        } catch (NumberFormatException e) {
            method.show_popup(msg, kitNumberTf);
            throw new RuntimeException(e);
            //  return;
        }

        if (expiryDate.isEmpty()) {
            method.show_popup("Please select expiry date", expiryDateDp);
            return;
        }

        if (!expiryDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN);
            Date date;
            try {
                date = sdf.parse(expiryDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            expiryDateL = date.getTime();
        }

        if (testUsed.isEmpty()) {
            method.show_popup("Please enter test used", testUsedTf);
            return;
        }

        try {
            testUsedI = Integer.parseInt(testUsed);
        } catch (NumberFormatException e) {
            method.show_popup(msg, testUsedTf);
            return;
        }


        if (lotNumber.isEmpty()) {
            method.show_popup("Please enter lot number", lotNumberTf);
            return;
        }

        try {
            lotNumberL = Long.parseLong(lotNumber);
        } catch (NumberFormatException e) {
            method.show_popup(msg, lotNumberTf);
            return;
        }

        KitDTO kitDTO = new KitDTO();

        kitDTO.setClientID(clientIdL);
        kitDTO.setDealerID(dealerIdL);
        kitDTO.setKitNumber(kitNumberL);
        kitDTO.setExpiryDate(expiryDateL);
        kitDTO.setLotNumber(lotNumberL);
        kitDTO.setTestUsed(testUsedI);

        if (null != kd) {
            kitDTO.setId(kd.getId());
        }

        Set<KitDTO> list = new HashSet<>(Arrays.asList(kitDTO));
        String jsonList = new Gson().toJson(list);

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
            callThread(jsonList);

        } else {
            alert.close();
        }
    }

    private void callThread(String json) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(json);
        myAsyncTask.execute();
    }

    public void keyPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER){
            submitBnClick(null);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        String json;

        public MyAsyncTask(String json) {
            this.json = json;
        }

        @Override
        public void onPreExecute() {

            progressbar.setVisible(true);
            method.hideElement(submitBn);
        }

        @Override
        public Boolean doInBackground(String... params) {

            if (operationType == OperationType.CREATE) {
                createKit(json);

            } else if (operationType == OperationType.UPDATE) {
                updateKit(json);
            }

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void updateKit(String json) {

        try {
            String token = (String) Login.authInfo.get("token");
            HttpPut httpPut = new HttpPut(UrlConfig.getGetKitsUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                AddKitResponse asr = new Gson().fromJson(content, AddKitResponse.class);
                List<KitDTO> failedData = asr.getInvalidKits();

                if (statusCode == 200) {

                    if (failedData.size() > 0) {
                        for (KitDTO kit : failedData) {
                            String errorMsg = kit.getErrorMessage();
                            customDialog.showAlertBox("Failed", errorMsg);
                        }
                    } else {
                        resetAllField();
                        Main.primaryStage.setUserData(true);
                        customDialog.showAlertBox("Success", "Kit successfully updated");

                        Platform.runLater(() -> cancelBnClick(null));
                    }

                } else {
                    customDialog.showAlertBox("Failed.", content);
                }
            }
        } catch (Exception e) {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }


    }

    private void createKit(String json) {
        try {
            HttpPost httpPost = new HttpPost(UrlConfig.getGetKitsUrl());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cookie", (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json,StandardCharsets.UTF_8);
            httpPost.setEntity(se);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                AddKitResponse asr = new Gson().fromJson(content, AddKitResponse.class);
                List<KitDTO> failedData = asr.getInvalidKits();

                if (statusCode == 200) {

                    if (failedData.size() > 0) {
                        for (KitDTO kit : failedData) {
                            String errorMsg = kit.getErrorMessage();
                            customDialog.showAlertBox("Failed", errorMsg);
                        }
                    } else {
                        resetAllField();
                        Main.primaryStage.setUserData(true);
                        customDialog.showAlertBox("Success", "Kit successfully added");
                    }

                } else {
                    customDialog.showAlertBox("Failed.", content);
                }

                Platform.runLater(() -> {

                });
            }

        } catch (Exception e) {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        } finally {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
        }
    }

    private void resetAllField() {
        Platform.runLater(() -> {
            clientIdTf.setText("");
            dealerIdTf.setText("");
            kitNumberTf.setText("");
            testUsedTf.setText("");
            lotNumberTf.setText("");
            expiryDateDp.getEditor().setText("");
        });
    }

}
