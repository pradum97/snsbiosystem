package com.techwhizer.snsbiosystem.report;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.report.constent.ReportDownloadPath;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

import java.io.FileOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DownloadReport {

    private Map<String, Object> getAlertDialog() {

        Map<String, Object> map = new HashMap<>();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ImageView iv = new ImageLoader().loadImageView("img/icon/info_ic.png");
        iv.setFitWidth(30);
        iv.setFitHeight(30);
        alert.setGraphic(iv);

        alert.setHeaderText("SELECT METHOD");
        alert.setTitle("");
        CheckBox download = new CheckBox("DOWNLOAD");
        CheckBox share = new CheckBox("SHARE");

        HBox hBox = new HBox(download, share);
        hBox.setSpacing(30);
        hBox.setStyle("-fx-alignment: center");

        VBox vBox = new VBox(hBox);
        vBox.setStyle("-fx-alignment: center");
        vBox.setSpacing(20);
        alert.getDialogPane().setPrefSize(320, 200);
        alert.getDialogPane().setContent(vBox);
        download.setSelected(true);
        map.put("alert", alert);
        map.put("share_button", share);
        map.put("download_button", download);
        return map;
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

       private Map<String, Object> map;
        private OperationType operationType;
        private Button downloadButton;

        public MyAsyncTask(Map<String, Object> map, OperationType operationType) {
            this.map = map;
            this.operationType = operationType;
        }

        @Override
        public void onPreExecute() {

            if (null != map.get("button")) {
                downloadButton = (Button) map.get("button");
                if (null != downloadButton) {
                    ProgressIndicator pi = new OptionalMethod().getProgressBar(25, 25);
                    pi.setStyle("-fx-progress-color: white;-fx-border-width: 2");
                    downloadButton.setGraphic(pi);
                }
            }

        }

        @Override
        public Boolean doInBackground(String... params) {
            downloadReport(map, operationType);
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {

            if (null != downloadButton) {
                Platform.runLater(() -> {
                    downloadButton.setGraphic(new ImageLoader().getDownloadImage());
                });
            }
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    public void dialogController(Map<String, Object> map, OperationType operationType) {

        Platform.runLater(() -> {
            Map<String, Object> alertrMap = getAlertDialog();
            map.putAll(alertrMap);

            Alert alert = (Alert) alertrMap.get("alert");
            CheckBox share = (CheckBox) alertrMap.get("share_button");

            final Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, ae -> {

                if (map.keySet().size() < 1) {
                    new OptionalMethod().show_popup("Please select method", share);
                    ae.consume();
                    return;
                }
                new MyAsyncTask(map, operationType).execute();
            });
            alert.show();
        });
    }

    private void downloadReport(Map<String, Object> map, OperationType operationType) {


        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Button downloadButton;
        CheckBox downloadCheckButton = (CheckBox) map.get("download_button");
        CheckBox shareCheckButton = (CheckBox) map.get("share_button");

        boolean isDownload = downloadCheckButton.isSelected();
        boolean isShare = shareCheckButton.isSelected();

        if (null != map.get("button")) {
            downloadButton = (Button) map.get("button");
        } else {
            downloadButton = null;
        }

        CustomDialog customDialog = new CustomDialog();
        try {

            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getKitReportUrl());

            if (operationType == OperationType.CUSTOMER_REPORT) {
                Long customerId = (Long) map.get("customer_id");
                param.setParameter("customer", String.valueOf(customerId));
            } else if (operationType == OperationType.KIT_REPORT) {
                Long kitId = (Long) map.get("kit");
                param.setParameter("kit", String.valueOf(kitId));
            }

            if (isShare) {
                param.setParameter("share", String.valueOf(true));
            }
            HttpGet httpGet = new HttpGet(param.build());
            httpGet.addHeader("Content-Type", "application/pdf");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {

                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(resEntity);
                if (statusCode == 200) {
                    String filePath = ReportDownloadPath.FILE_PATH();

                    if (isShare) {

                        Map<String, Object> reportResponse = new Gson().fromJson(body, Map.class);

                        String data = (String) reportResponse.get("data");
                        String message = (String) reportResponse.get("message");
                        String fileName = (String) reportResponse.get("file_name");
                        if (isDownload) {
                            FileOutputStream fos = new FileOutputStream(filePath + "\\" + fileName);
                            byte[] decoder = Base64.getDecoder().decode(data);
                            fos.write(decoder);
                            fos.close();
                            customDialog.showAlertBox("success", "File successfully download.\n\n" + message, filePath + "\\" + fileName);
                        } else {
                            customDialog.showAlertBox("success", message);
                        }

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

                    Map m = new Gson().fromJson(body, Map.class);
                    new CustomDialog().showAlertBox("Failed", (String) m.get("message"));
                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                }

                if (null != downloadButton) {
                    Platform.runLater(() -> {
                        downloadButton.setGraphic(new ImageLoader().getDownloadImage());
                    });
                }
            }
        } catch (Exception e) {
            if (null != downloadButton) {
                Platform.runLater(() -> {
                    downloadButton.setGraphic(new ImageLoader().getDownloadImage());
                });
            }
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        }finally {
            if (null != downloadButton) {
                Platform.runLater(() -> {
                    downloadButton.setGraphic(new ImageLoader().getDownloadImage());
                });
            }
        }
    }

}
