package com.techwhizer.snsbiosystem.controller.dashboard;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.Dashboard;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.model.DashboardModel;
import com.techwhizer.snsbiosystem.util.UrlConfig;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startThread();
    }

    public void startThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
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
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                DashboardModel dash = new Gson().fromJson(content, DashboardModel.class);

                totalUsersL.setText(String.valueOf(dash.getTotalUsers()));
                totalKitsL.setText(String.valueOf(dash.getTotalKits()));
                totalSterilizerL.setText(String.valueOf(dash.getTotalSterilizers()));
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
