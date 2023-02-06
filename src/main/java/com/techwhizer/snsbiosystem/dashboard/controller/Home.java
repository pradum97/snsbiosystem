package com.techwhizer.snsbiosystem.dashboard.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.dashboard.model.DashboardModel;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Home implements Initializable {

    public Label totalUsersL;
    public Label totalKitsL;
    public Label totalSterilizerL;
    private CustomDialog customDialog;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        startThread();
    }

    public void startThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void totalSterilizerCardClick(MouseEvent mouseEvent) {
        customDialog.showFxmlFullDialog("sterilizer/sterilizers.fxml", "ALL STERILIZERS");
    }

    public void totalKitsCardClick(MouseEvent mouseEvent) {
        customDialog.showFxmlFullDialog("kit/kits.fxml","ALL KITS");
    }

    public void totalUserCardClick(MouseEvent mouseEvent) {

        customDialog.showFxmlFullDialog("profile/users.fxml","ALL USERS");

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        public void onPreExecute() {
        }

        @Override
        public Boolean doInBackground(String... params) {
            /* Background Thread is running */
            Platform.runLater(()->{
                countData();
            });

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
        }

        @Override
        public void progressCallback(Integer... params) {
        }
    }

    private void countData() {

        try {

            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpGet httpPut = new HttpGet(UrlConfig.getDashboardUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    DashboardModel dash = null;
                    try {
                        dash = new Gson().fromJson(content, DashboardModel.class);
                        totalUsersL.setText(null == dash.getTotalUsers() ? "0" : String.valueOf(dash.getTotalUsers()));
                        totalKitsL.setText(null == dash.getTotalKits() ? "0" : String.valueOf(dash.getTotalKits()));
                        totalSterilizerL.setText(null == dash.getTotalSterilizers() ? "0" : String.valueOf(dash.getTotalSterilizers()));
                    } catch (JsonSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    customDialog.showAlertBox("", content);
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Failed");
            alert.setHeaderText(e.getCause().getMessage());
            alert.initOwner(Main.primaryStage);

            Optional<ButtonType> result = alert.showAndWait();
            ButtonType button = result.orElse(ButtonType.CANCEL);

            if (button == ButtonType.OK) {
                new Main().changeScene("auth/login.fxml", "LOGIN");
            }

            alert.showAndWait();
        }
    }
}
