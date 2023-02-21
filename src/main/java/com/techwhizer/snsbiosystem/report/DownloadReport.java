package com.techwhizer.snsbiosystem.report;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.report.constent.ReportDownloadPath;
import com.techwhizer.snsbiosystem.report.constent.ReportType;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Map;

public class DownloadReport {


    public void getShareOption(Map<String, Object> previousMap) {
        previousMap.put("report_type", ReportType.FETCH_SHARING_OPTION);

      /*  Map<String, Object> previousMap = new HashMap<>();
        previousMap.put("reportType", reportType);
        previousMap.put("actionButton", actionButton);
        previousMap.put("kit_id", kitId);
        previousMap.put("customer_id", customerId);*/

        action(null, previousMap);
    }

    public void alertDialog(Map<String, Object> previousMap, Map<String, Boolean> option) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setTitle("Share To");
        ImageView downloadIcon = ReportIcon.getDownloadIcon();
        ImageView emailIcon = ReportIcon.getEmailIcon();
        ImageView faxIcon = ReportIcon.getFaxIcon();
        ImageView smsIcon = ReportIcon.getSmsIcon();


        Button downloadButton = new Button();
        downloadButton.setId("download");
        Button emailButton = new Button();
        emailButton.setId("email");
        Button faxButton = new Button();
        faxButton.setId("fax");
        Button smsButton = new Button();
        smsButton.setId("sms");

        buttonCustomize(downloadButton, emailButton, faxButton, smsButton);

        downloadButton.setGraphic(downloadIcon);
        emailButton.setGraphic(emailIcon);
        faxButton.setGraphic(faxIcon);
        smsButton.setGraphic(smsIcon);

        buttonClickEvent(downloadButton, emailButton, faxButton, smsButton, alert, previousMap);

        Label downloadL = new Label("SAVE");
        Label emailL = new Label("EMAIL");
        Label faxL = new Label("FAX");
        Label smsL = new Label("SMS");

        labelCustomize(downloadL, emailL, faxL, smsL);

        VBox downloadContainer = new VBox(downloadButton, downloadL);
        VBox emailContainer = new VBox(emailButton, emailL);
        VBox faxContainer = new VBox(faxButton, faxL);
        VBox smsContainer = new VBox(smsButton, smsL);

        faxContainer.setDisable(!option.get("FAX"));
        emailContainer.setDisable(!option.get("EMAIL"));
        smsContainer.setDisable(!option.get("SMS"));

        subContainerCustomize(downloadContainer, emailContainer, faxContainer, smsContainer);

        HBox mainContainer = new HBox(downloadContainer, emailContainer, faxContainer, smsContainer);
        mainContainer.setSpacing(20);
        mainContainer.setAlignment(Pos.CENTER);
        alert.getDialogPane().setContent(mainContainer);

        alert.getDialogPane().setPrefSize(350, 250);

        final Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        final Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);

        new OptionalMethod().hideElement(okButton, cancelButton);
        alert.initOwner(Main.primaryStage);

        alert.showAndWait();
    }

    private void buttonClickEvent(Button downloadButton, Button emailButton,
                                  Button faxButton, Button smsButton, Alert alert,
                                  Map<String, Object> prevousMap) {
        prevousMap.put("report_type", ReportType.REPORT_PROCESSED);
        downloadButton.setOnAction(event -> {
            prevousMap.put("is_share", false);
            action(alert, prevousMap);
        });

        emailButton.setOnAction(event -> {
            prevousMap.put("is_share", true);
            prevousMap.put("via", "email");
            action(alert, prevousMap);
        });

        faxButton.setOnAction(event -> {
            prevousMap.put("is_share", true);
            prevousMap.put("via", "fax");
            action(alert, prevousMap);
        });

        smsButton.setOnAction(event -> {
            prevousMap.put("is_share", true);
            prevousMap.put("via", "sms");
            action(alert, prevousMap);
        });
    }

    private void action(Alert alert, Map<String, Object> previousMap) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(alert, previousMap);
        myAsyncTask.execute();
    }


    private void labelCustomize(Label... labels) {

        for (Label label : labels) {

            label.setStyle("-fx-font-size: 13;-fx-font-weight: bold");

        }
    }

    private void subContainerCustomize(VBox... arrayContainer) {

        for (VBox v : arrayContainer) {
            v.setAlignment(Pos.CENTER);
            v.setSpacing(5);
        }
    }

    private void buttonCustomize(Button... buttons) {

        for (Button button : buttons) {
            button.setMinHeight(50);
            button.setMinWidth(50);

            String id = button.getId();

            switch (id) {
                case "download" -> buttonStyle(button, "#006666");
                case "email" -> buttonStyle(button, "rgba(180,3,3,0.75)");
                case "fax" -> buttonStyle(button, "#04238a");
                case "sms" -> buttonStyle(button, "#1611a4");
            }
        }
    }

    private void buttonStyle(Button button, String backgroundColor) {

        button.setStyle("-fx-background-radius:50;-fx-border-radius: 50" +
                ";-fx-border-color: transparent;-fx-cursor: hand;-fx-background-color:" + backgroundColor);
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private Button actionButton;
        private Alert alert;
        private Map<String, Object> previousMap;

        public MyAsyncTask(Alert alert, Map<String, Object> previousMap) {
            this.alert = alert;
            this.previousMap = previousMap;

            if (null != previousMap) {
                actionButton = (Button) previousMap.get("action_button");
            }
        }

        @Override
        public void onPreExecute() {
            if (null != alert && alert.isShowing()) {
                alert.close();
            }

        }

        @Override
        public Boolean doInBackground(String... params) {
            ProgressIndicator pi = new OptionalMethod().getProgressBar(25, 25);
            pi.setStyle("-fx-progress-color: white;-fx-border-width: 2");
            Platform.runLater(() -> {
                actionButton.setGraphic(pi);
            });
            if (null != previousMap && previousMap.get("report_type") == ReportType.FETCH_SHARING_OPTION) {
                getOption(previousMap);
            } else {
                downloadReport(previousMap, actionButton);
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            resetActionButton(actionButton);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getOption(Map<String, Object> previousMap) {
        Long customerId = (Long) previousMap.get("customer_id");
        Map<String, Boolean> getOption = ReportShareOption.getOption(customerId);

        Platform.runLater(() -> {
            alertDialog(previousMap, getOption);
        });
    }

    private void resetActionButton(Button actionButton) {
        if (null != actionButton) {
            Platform.runLater(() -> {
                actionButton.setGraphic(new ImageLoader().getShareIcon());
            });
        }
    }

    private void downloadReport(Map<String, Object> map, Button actionButton) {
        boolean isShare = (Boolean) map.get("is_share");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CustomDialog customDialog = new CustomDialog();
        try {

            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getKitReportUrl());

            param.setParameter("share", String.valueOf(isShare));
            Long customerId = (Long) map.get("customer_id");
            Long kitId = (Long) map.get("kit_id");

            if (null == kitId) {
                param.setParameter("customer", String.valueOf(customerId));
            } else {
                param.setParameter("kit", String.valueOf(kitId));
            }

            if (isShare) {
                String via = map.get("via").toString().toLowerCase();
                param.setParameter("via", via);
            }

            HttpGet httpGet = new HttpGet(param.build());
            httpGet.addHeader("Content-Type", "application/pdf");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();

            if (resEntity != null) {

                if (statusCode == 200) {
                    String filePath = ReportDownloadPath.FILE_PATH();

                    if (isShare) {
                        String body = EntityUtils.toString(resEntity);
                        Map<String, Object> reportResponse = new Gson().fromJson(body, Map.class);
                        String message = (String) reportResponse.get("message");
                        customDialog.showAlertBox("success", message);

                    } else {
                        Header[] h = response.getHeaders("Content-Disposition");
                        String fileName = h[0].getValue().replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
                        String path = filePath + "\\" + fileName;

                        boolean isSaved = new SaveReport().save(resEntity.getContent(), path);
                        if (isSaved) {
                            customDialog.showAlertBox("success", "Successfully download");
                        } else {
                            customDialog.showAlertBox("success", "Something went wrong!");
                        }
                    }
                } else if (statusCode == 400) {
                    String body = EntityUtils.toString(resEntity);
                    Map m = new Gson().fromJson(body, Map.class);

                    new CustomDialog().showAlertBox("Failed", (String) m.get("message"));
                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                }

            }
        } catch (Exception e) {
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        }finally {
            if (null != actionButton) {
                resetActionButton(actionButton);
            }
        }
    }

}
