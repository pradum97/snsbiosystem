package com.techwhizer.snsbiosystem.user.controller.auth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.common.CountryUtility;
import com.techwhizer.snsbiosystem.common.constant.CountryType;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.user.model.UserCredentials;
import com.techwhizer.snsbiosystem.user.util.CheckUsername;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class ForgotPassword extends OptionalMethod implements Initializable {
    public VBox verificationContainer;
    public TextField usernameTf;
    public VBox phoneContainer;
    public TextField phoneTf;
    public Button submit_bn;
    public ProgressIndicator progressBar;
    public Button cancelBn1;
    public ComboBox<String> countryCodeCom;
    private CustomDialog customDialog;
    private String isEmailExists = "FRESH" ;
    private OptionalMethod method;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new OptionalMethod();

        hideElement(progressBar);
        phoneContainer.setVisible(false);

        callThread("","",OperationType.FETCH_COUNTRY);
    }

    public void submit(ActionEvent event) {

        String username = usernameTf.getText();
        String phone = phoneTf.getText();

        if (username.isEmpty()) {
            show_popup("Please enter username", usernameTf);
            return;
        }

        if (isEmailExists.equals("NO")){
            if (phone.isEmpty()){
               show_popup("Please enter telephone number",phoneTf);
               return;
            }

            try {
                Long.parseLong(phone);
            } catch (NumberFormatException e) {
                show_popup("Please enter valid telephone number",phoneTf);
                return;
            }
        }
        callThread(username,phone, OperationType.START);
    }

    private void callThread(String username, String phone, OperationType operationType) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(username,phone,operationType);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void enterPress(KeyEvent key) {

        if (key.getCode() == KeyCode.ENTER){

            if (usernameTf.isFocusTraversable() || phoneTf.isFocusTraversable()){
                submit(null);
            }
        }
    }

    public void cancelClick(ActionEvent event) {

        Stage stage =(Stage) usernameTf.getScene().getWindow();

        if (stage.isShowing()){
            stage.close();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String username, phone;
        private OperationType operationType;


        public MyAsyncTask(String username, String phone, OperationType operationType) {
            this.username = username;
            this.phone = phone;
            this.operationType = operationType;
        }

        @Override
        public void onPreExecute() {
            progressBar.setVisible(true);
            hideElement(submit_bn);

        }


        @Override
        public Boolean doInBackground(String... params) {

            if (operationType == OperationType.FETCH_COUNTRY){
                getCountryData();
            }else {
                if (new CheckUsername().check(username)){
                    switch (isEmailExists){
                        case "FRESH" ->  sendResetLink(username,phone);
                        case "NO" -> usernameWithPhoneValid(username,phone);
                    }

                }else {
                    usernameTf.setText("");
                    customDialog.showAlertBox("Incorrect username","Please enter valid username");
                }
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            hideElement(progressBar);
            submit_bn.setVisible(true);
        }
        void getCountryData() {

            JsonArray jsonArray = CountryUtility.getCountryJson();
            ObservableList<String> countryPhoneCode = CountryUtility.getCountryTypeFromJsonArray(jsonArray, CountryType.COUNTRY_CODE);
            Platform.runLater(() -> {
                countryCodeCom.setItems(countryPhoneCode);
                countryCodeCom.getSelectionModel().select(CommonUtility.DEFAULT_PHONE_CODE_SELECTION);
            });
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }
    private void usernameWithPhoneValid(String username, String phone) {
        String countryCode = countryCodeCom.getSelectionModel().getSelectedItem();

        try {
            HttpClient httpClient = HttpClients.custom() .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy") .build()).build();

            HttpPut httpPut = new HttpPut(UrlConfig.getResetPasswordUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie",(String) Login.authInfo.get("token"));

            UserCredentials user = new UserCredentials();
            user.setUserName(username);
            user.setPhoneNumber(countryCode+"-"+phone);
            String json = new Gson().toJson(user);
            StringEntity se = new StringEntity(json);

            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode==200){
                    customDialog.showAlertBox("Success","Your username is your password.");

                    Platform.runLater(()->{
                        cancelClick(null);
                    });
                }else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                }else {
                    customDialog.showAlertBox("Failed",content);
                }
            }
        } catch (Exception e) {
            hideElement(progressBar);
            submit_bn.setVisible(true);
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);

        }

    }
    private void sendResetLink(String username, String phone) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {

            HttpClient httpClient = HttpClients.custom() .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy") .build()).build();

            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getPasswordResetLinkUrl());
            uriBuilder.setParameter("username", username);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie",(String) Login.authInfo.get("token"));

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
              //  String content = EntityUtils.toString(resEntity);
               int statusCode = response.getStatusLine().getStatusCode();

               switch (statusCode){

                   case 200 ->{
                       isEmailExists = "FRESH";
                       usernameTf.setText("");
                       customDialog.showAlertBox( "FORGOT PASSWORD", """
                               Link has been sent successfully.
                               Please check your email.""");
                   }
                   case 400 ->{
                       isEmailExists ="NO" ;
                       usernameTf.setEditable(false);
                       phoneContainer.setVisible(true);
                       usernameTf.setFocusTraversable(false);
                       phoneTf.setFocusTraversable(true);
                   }

                   case 401->{
                       new HttpStatusHandler(StatusCode.UNAUTHORISED);
                   }
               }
            }
        } catch (Exception e) {
            hideElement(progressBar);
            submit_bn.setVisible(true);
        }
    }
}
