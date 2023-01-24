package com.techwhizer.snsbiosystem.controller.profile;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.controller.auth.Login;
import com.techwhizer.snsbiosystem.model.User;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.UrlConfig;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class MyProfile implements Initializable {
    public ListView<String> roleLv;
    public Label clientIdL;
    public Label usernameL;
    public Label workPhoneNumberL;
    public Label workEmailL;
    public Label companyNameL;
    public Label officeCityL;
    public Label officeStateL;
    public Label officeZipL;
    public Label officeAddressL;
    public Label officeFaxNumberL;
    public Label homeCityL;
    public Label homeStateL;
    public Label homeZipL;
    public Label homeAddressL;
    public Label createdDateL;
    public VBox contentContainer;
    public ProgressIndicator progressBar;
    public Label fullNameL;
    private OptionalMethod method;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();

        method.hideElement(roleLv);
        contentContainer.setDisable(true);
        progressBar.setVisible(true);

      Long id = (Long) Main.primaryStage.getUserData();

        MyAsyncTask myAsyncTask = new MyAsyncTask(id);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }
    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private Long id;
        public MyAsyncTask(Long id) {
            this.id = id;
        }
        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {
            getUserProfile(id);
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            contentContainer.setDisable(false);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getUserProfile(Long id) {

       String token = (String) Login.authInfo.get("token");

        try {
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpGet httpPut = new HttpGet(UrlConfig.getUserprofileUrl().concat(String.valueOf(id)));
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                User user = new Gson().fromJson(content, User.class);
                Platform.runLater(()-> setUserDetails(user));
            }

        } catch (Exception e) {
            method.hideElement(progressBar);
            contentContainer.setDisable(false);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
            throw new RuntimeException(e);
        }

    }
    private void setUserDetails(User user) {

        String name;

        if (user.getFirstName().isEmpty() &&
                user.getLastName().isEmpty()){
            name = "";
        }else {
            name = user.getFirstName() + " " + user.getLastName();
        }

        fullNameL.setText(name);
        clientIdL.setText(String.valueOf(user.getClientID()));
        usernameL.setText(user.getRequestedLoginName());
        workPhoneNumberL.setText(user.getWorkPhoneNumber());
        workEmailL.setText(user.getWorkEmail());
        companyNameL.setText(user.getOfficeCompanyName());
        officeCityL.setText(user.getOfficeCity());
        officeStateL.setText(user.getOfficeState());
        officeZipL.setText(user.getOfficeZip());
        officeAddressL.setText(user.getOfficeAddress());
        officeFaxNumberL.setText(user.getOfficeFaxNumber());
        homeStateL.setText(user.getHomeState());
        homeCityL.setText(user.getHomeCity());
        homeZipL.setText(user.getHomeZip());
        homeAddressL.setText(user.getHomeAddress());
        createdDateL.setText(String.valueOf(user.getCreatedDate()));
    }
}
