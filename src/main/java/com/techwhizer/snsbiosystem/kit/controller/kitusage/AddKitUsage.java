package com.techwhizer.snsbiosystem.kit.controller.kitusage;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.constants.TestResultOptions;
import com.techwhizer.snsbiosystem.kit.model.AddKitResponse;
import com.techwhizer.snsbiosystem.kit.model.AddKitUsagesResponse;
import com.techwhizer.snsbiosystem.kit.model.KitDTO;
import com.techwhizer.snsbiosystem.kit.model.KitUsageDTO;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.LocalDb;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddKitUsage implements Initializable {

    public Button submitBn;
    public ProgressIndicator progressbar;
    public Label titleL;
    public TextField kitNumberTf;
    public DatePicker testDateDp;
    public TextField sterilizerId;
    public TextField sterilizerType;
    public ComboBox<String> testResultCom;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private LocalDb localDb;
    private  KitUsageDTO kud;
    private Map<String,Object> sterilizerData = new HashMap<>();
    private OperationType operationType;
    private HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec("easy").build()).build();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        localDb = new LocalDb();
        method.hideElement(progressbar);
        method.convertDateFormat(testDateDp);
        callThread(null,OperationType.SORTING_LOADING);

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
                titleL.setText("ADD KIT USAGE");
                submitBn.setText("SUBMIT");
            }
            case UPDATE -> {
                titleL.setText("UPDATE KIT USAGE");
                kud = (KitUsageDTO) map.get("kits_data");
                submitBn.setText("UPDATE");
                setTextFieldData();
            }
        }
    }
    private void setTextFieldData() {

        kitNumberTf.setText(null == kud.getKitNumber() ? "" : String.valueOf(kud.getKitNumber()));
        if (null != kud.getTestDate()) {
            String date = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN).format(new Date(kud.getTestDate()));
            testDateDp.getEditor().setText(date);
        }

        if (null != kud.getSterilizerID()){
            sterilizerData.put("sterilizer_id",kud.getSterilizerID());
        }

        if (null != kud.getSterilizerListNumber()){
            sterilizerData.put("sterilizer_list_number",kud.getSterilizerListNumber());
        }

        sterilizerId.setText(null == kud.getSterilizerID() ? "" : String.valueOf(kud.getSterilizerID()));
        sterilizerType.setText(null == kud.getSterilizerType() ? "" : String.valueOf(kud.getSterilizerType()));
    }

    public void cancelBnClick(ActionEvent event) {
        Stage stage = (Stage) submitBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void submitBnClick(ActionEvent event) {
        String msg = "Please enter in number format.";

        String kitNumber = kitNumberTf.getText();
        String testDate = testDateDp.getEditor().getText();


        Long kitNumberL = null, testDateL = null;


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

        if (testDate.isEmpty()) {
            method.show_popup("Please select test date", testDateDp);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN);
        Date date;
        try {
            date = sdf.parse(testDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        testDateL = date.getTime();

        if (null == sterilizerData && null == sterilizerData.get("sterilizer_id")){
            method.show_popup("Please select sterilizer", sterilizerId);
            return;
        }else if (sterilizerType.getText().isEmpty()){
            method.show_popup("Please select sterilizer", sterilizerId);
            return;
        }

        KitUsageDTO kitDTO = new KitUsageDTO();

        kitDTO.setKitNumber(kitNumberL);
        kitDTO.setTestDate(testDateL);

        if (!testResultCom.getSelectionModel().isEmpty()){
            String str = testResultCom.getSelectionModel().getSelectedItem();
            kitDTO.setTestResult(str);
        }

        kitDTO.setSterilizerID((Long)sterilizerData.get("sterilizer_id"));
        kitDTO.setSterilizerListNumber((Integer) sterilizerData.get("sterilizer_list_number"));

       if (null != kud && null != kud.getId()){
           kitDTO.setId(kud.getId());
       }
        if (null != kud) {
            kitDTO.setId(kud.getId());
        }
        Set<KitUsageDTO> list = new HashSet<>(Arrays.asList(kitDTO));
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
            callThread(jsonList, null);
        } else {
            alert.close();
        }
    }

    private void callThread(String json, OperationType operationType) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(json, operationType);
        myAsyncTask.execute();
    }

    public void chooseSterilizerId(MouseEvent event) {

        customDialog.showFxmlDialog2("dialog/sterilizerChooser.fxml", "SELECT STERILIZER");
        if (null != Main.primaryStage.getUserData() && Main.primaryStage.getUserData() instanceof Map<?, ?>) {
            sterilizerData = (Map<String, Object>) Main.primaryStage.getUserData();

            if (null != sterilizerData.get("sterilizer_id")) {
                sterilizerId.setText(String.valueOf(sterilizerData.get("sterilizer_id")));
                sterilizerType.setText(String.valueOf(sterilizerData.get("sterilizer_type")));
            }
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String json;
        private OperationType start;

        public MyAsyncTask(String json, OperationType start) {
            this.json = json;
            this.start = start;
        }

        @Override
        public void onPreExecute() {
            progressbar.setVisible(true);
            method.hideElement(submitBn);
        }

        @Override
        public Boolean doInBackground(String... params) {

            if (null != start) {
                if (start == OperationType.SORTING_LOADING) {
                    getResultKey();
                }
            }else {

                if (operationType == OperationType.CREATE) {
                    createKitUsage(json);
                } else if (operationType == OperationType.UPDATE) {
                    updateKitUsage(json);
                }
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

    private void getResultKey() {
        testResultCom.setItems(FXCollections.observableArrayList(TestResultOptions.sortingMap.keySet()));

        Platform.runLater(() -> {
            if (operationType == OperationType.UPDATE) {
                testResultCom.getSelectionModel().select(TestResultOptions.getKeyFromValue(kud.getTestResult()));
            }
        });
    }

    private void updateKitUsage(String json) {

        try {
            String token = (String) Login.authInfo.get("token");
            HttpPut httpPut = new HttpPut(UrlConfig.getKitsUsagesUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            StringEntity se = new StringEntity(json);
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
                    customDialog.showAlertBox("Failed.", "Something went wrong. Please try again.");
                }
            }
        } catch (Exception e) {
            method.hideElement(progressbar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }

    }

    private void createKitUsage(String json) {
        try {
            HttpPost httpPost = new HttpPost(UrlConfig.getKitsUsagesUrl());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cookie", (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                AddKitUsagesResponse asr = new Gson().fromJson(content, AddKitUsagesResponse.class);
                List<KitUsageDTO> failedData = asr.getInvalidKits();

                if (statusCode == 200) {

                    if (failedData.size() > 0) {
                        for (KitUsageDTO kit : failedData) {
                            String errorMsg = kit.getErrorMessage();
                            customDialog.showAlertBox("Failed", errorMsg);
                        }
                    } else {
                        resetAllField();
                        Main.primaryStage.setUserData(true);
                        customDialog.showAlertBox("Success", "Successfully added");
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
            kitNumberTf.setText("");
            sterilizerId.setText("CHOOSE STERILIZER ID");
            sterilizerType.setText("");
            testDateDp.getEditor().setText("");
        });
    }

}
