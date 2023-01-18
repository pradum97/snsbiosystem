package com.techwhizer.snsbiosystem.controller.auth;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.UrlConfig;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangePassword implements Initializable {
    public TextField oldPasswordTf;
    public TextField newPasswordTf;
    public TextField confirmPasswordTf;
    public Button changeBn;
    public ProgressIndicator progressBar;
    private OptionalMethod method;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new OptionalMethod();
        method.hideElement(progressBar);

    }

    public void cancelBnClick(ActionEvent event) {
        Stage stage = (Stage) newPasswordTf.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void changeBnClick(ActionEvent event) {

        String oldPassword = oldPasswordTf.getText();
        String newPassword = newPasswordTf.getText();
        String confirmPassword = confirmPasswordTf.getText();

        String username = (String) Login.authInfo.get("username");

        if (oldPassword.isEmpty()) {
            method.show_popup("Please enter old password", oldPasswordTf);
            return;
        } else if (newPassword.isEmpty()) {
            method.show_popup("Please enter new password", newPasswordTf);
            return;
        } else if (confirmPassword.isEmpty()) {
            method.show_popup("Please enter confirm password", confirmPasswordTf);
            return;
        } else if (!newPassword.equals(confirmPassword)) {
            method.show_popup("Password & confirm password does not match.", confirmPasswordTf);
            return;
        }

        ChangePasswordModel cpm = new ChangePasswordModel(username,oldPassword,confirmPassword);
        MyAsyncTask myAsyncTask = new MyAsyncTask(cpm);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
       private ChangePasswordModel cpm;

        public MyAsyncTask(ChangePasswordModel cpm) {
            this.cpm = cpm;
        }

        @Override
        public void onPreExecute() {
            method.hideElement(changeBn);
            progressBar.setVisible(true);
        }

        @Override
        public Boolean doInBackground(String... params) {

            try {

                HttpClient httpClient = HttpClients.custom() .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("easy") .build()).build();

                HttpPut httpPut = new HttpPut(UrlConfig.getChangePasswordUrl());
                httpPut.addHeader("Content-Type", "application/json");

                String json = new Gson().toJson(cpm);

                StringEntity se = new StringEntity(json);
                httpPut.setEntity(se);

                HttpResponse response = httpClient.execute(httpPut);
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String content = EntityUtils.toString(resEntity);
                    int statusCode = response.getStatusLine().getStatusCode();

                    Platform.runLater(()->{
                        if (statusCode==200){

                            customDialog.showAlertBox("Success","Password successfully changed.");
                            cancelBnClick(null);

                        }else {
                            customDialog.showAlertBox("Changing Failed.",content);
                        }
                    });
                }

            } catch (Exception e) {
                method.hideElement(progressBar);
                changeBn.setVisible(true);
                customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
                e.printStackTrace();
            }

            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            changeBn.setVisible(true);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    static class ChangePasswordModel {

        private String userName;
        private String password;
        private String newPassword;

        public ChangePasswordModel(String userName, String password, String newPassword) {
            this.userName = userName;
            this.password = password;
            this.newPassword = newPassword;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

