package com.techwhizer.snsbiosystem.controller.auth;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.model.UserCredentials;
import com.techwhizer.snsbiosystem.util.AppConfig;
import com.techwhizer.snsbiosystem.util.CheckUsername;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.UrlConfig;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
    private CustomDialog customDialog;
    private String isEmailExists = "FRESH" ;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();

      hideElement(progressBar);
        phoneContainer.setVisible(false);
    }

    public void submit(ActionEvent event) {

        String username = usernameTf.getText();
        String phone = phoneTf.getText();

        if (username.isEmpty()){
            show_popup("Please enter username",usernameTf);
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
        callThread(username,phone);
    }

    private void callThread(String username, String phone) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(username,phone);
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
        String username, phone;

        public MyAsyncTask(String username, String phone) {
            this.username = username;
            this.phone = phone;
        }

        @Override
        public void onPreExecute() {
            progressBar.setVisible(true);
            hideElement(submit_bn);

        }

        @Override
        public Boolean doInBackground(String... params) {
            if (new CheckUsername().check(username)){
                switch (isEmailExists){
                    case "FRESH" ->  sendResetLink(username,phone);
                    case "NO" -> usernameWithPhoneValid(username,phone);
                }

            }else {
                usernameTf.setText("");
             customDialog.showAlertBox("Incorrect username","Please enter valid username");
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            hideElement(progressBar);
            submit_bn.setVisible(true);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }
    private void usernameWithPhoneValid(String username, String phone) {

        try {

            HttpClient httpClient = HttpClients.custom() .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy") .build()).build();

            HttpPut httpPut = new HttpPut(UrlConfig.getResetPasswordUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie",(String) Login.authInfo.get("token"));

            UserCredentials user = new UserCredentials();
            user.setUserName(username);
            user.setPhoneNumber(phone);
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
                }else {
                    customDialog.showAlertBox("Failed",content);
                }
            }
        } catch (Exception e) {
            hideElement(progressBar);
            submit_bn.setVisible(true);
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
               }
            }
        } catch (Exception e) {
            hideElement(progressBar);
            submit_bn.setVisible(true);
        }
    }
}
