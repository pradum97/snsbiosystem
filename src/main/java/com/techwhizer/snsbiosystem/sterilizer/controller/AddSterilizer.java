package com.techwhizer.snsbiosystem.sterilizer.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.sterilizer.model.AddSterilizerResponse;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerDTO;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerTableView;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
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
import java.util.*;

public class AddSterilizer implements Initializable {
    public TextField listNumberTf;
    public ComboBox<String> typeCom;
    public TextField brandTf;
    public TextField serialNumTf;
    public Button submitBn;
    public ProgressIndicator progressbar;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private LocalDb localDb;
    public Label titleL;
    private OperationType operationType;
    private  SterilizerTableView stv;
    private HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec("easy").build()).build();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        localDb = new LocalDb();
        method.hideElement(progressbar);


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
        typeCom.setItems(localDb.getSterilizerType());

        switch (operationType) {
            case CREATE -> {
                titleL.setText("ADD STERILIZER");
                submitBn.setText("SUBMIT");
            }
            case UPDATE -> {
                titleL.setText("UPDATE STERILIZER");
                stv = (SterilizerTableView) map.get("sterilizer_data");
                submitBn.setText("UPDATE");
                setTextFieldData(stv);
            }
        }


    }

    private void setTextFieldData(SterilizerTableView stv) {
        listNumberTf.setText(String.valueOf(stv.getListNumber()));
        brandTf.setText(stv.getBrand());
        typeCom.getEditor().setText(stv.getType());
        serialNumTf.setText(stv.getSerialNumber());
    }

    public void cancelBnClick(ActionEvent event) {
        Stage stage = (Stage) submitBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void submitBnClick(ActionEvent event) {

        String listNumber = listNumberTf.getText();
        String brand = brandTf.getText();
        String serialNumber = serialNumTf.getText();

        if (listNumber.isEmpty()) {
            method.show_popup("Please enter sterilizer list number", listNumberTf);
            return;
        }
        Integer listNumI = null;
        try {
            listNumI = Integer.parseInt(listNumber);
        } catch (NumberFormatException e) {
            method.show_popup("Please enter sterilizer list number in number format", listNumberTf);
            return;
        }

        String type = typeCom.getEditor().getText();

        SterilizerDTO sDto = new SterilizerDTO();
        sDto.setSterilizerListNumber(listNumI);
        sDto.setSterilizerBrand(brand);
        sDto.setSterilizerSerialNumber(serialNumber);
        sDto.setSterilizerType(type);
        sDto.setClientID((Long) Login.authInfo.get("current_id"));

        if (null != stv){
            sDto.setSterilizerID(stv.getId());
        }

        Set<SterilizerDTO> list = new HashSet<>(Arrays.asList(sDto));

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

    private void callThread(String map) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(map);
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

                createSterilizer(json);

            } else if (operationType == OperationType.UPDATE) {

                updateSterilizer(json);

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

    private void updateSterilizer(String json) {

        try {
            String token = (String) Login.authInfo.get("token");

            HttpPut httpPut = new HttpPut(UrlConfig.getAddSterilizerUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {

                    Platform.runLater(() ->
                    {
                        resetAllField();
                        customDialog.showAlertBox("Success","Sterilizer successfully updated");
                        Main.primaryStage.setUserData(true);
                        cancelBnClick(null);

                    });

                }
            }
        } catch (Exception e) {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }


    }

    private void createSterilizer(String json) {
        try {
            HttpPost httpPost = new HttpPost(UrlConfig.getAddSterilizerUrl());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cookie",  (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json,StandardCharsets.UTF_8);
            httpPost.setEntity(se);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                Platform.runLater(() -> {
                    if (statusCode == 200) {
                        AddSterilizerResponse asr = new Gson().fromJson(content, AddSterilizerResponse.class);
                        List<SterilizerDTO> failedUsers = asr.getInvalidData();
                        if (failedUsers.size() > 0) {
                            for (SterilizerDTO u : failedUsers) {
                               String errorMsg = u.getErrorMessage();
                                customDialog.showAlertBox("Failed", errorMsg);
                            }
                        } else {
                            resetAllField();
                            Main.primaryStage.setUserData(true);
                            customDialog.showAlertBox("Success","Sterilizer successfully created");
                        }

                        method.hideElement(progressbar);
                        submitBn.setVisible(true);

                    } else {
                        customDialog.showAlertBox("Failed.", content);
                    }
                });
            }

        } catch (Exception e) {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    private void resetAllField() {

        listNumberTf.setText("");
        typeCom.getSelectionModel().clearSelection();
        typeCom.getEditor().setText("");
        brandTf.setText("");
        serialNumTf.setText("");
    }

}
