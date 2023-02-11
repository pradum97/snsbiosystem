package com.techwhizer.snsbiosystem.dashboard.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.dashboard.model.DashboardModel;
import com.techwhizer.snsbiosystem.kit.model.KitPageResponse;
import com.techwhizer.snsbiosystem.kit.model.KitUsageDTO;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Home implements Initializable {

    public Label totalUsersL;
    public Label totalKitsL;
    public Label totalSterilizerL;

    public ImageView refreshBn;
    public TableView<KitUsageDTO> tableview;
    public Pagination pagination;
    public TableColumn<KitUsageDTO, String> colId;
    public TableColumn<KitUsageDTO, String> colTestDate;
    public TableColumn<KitUsageDTO, String> colKitNumber;
    public TableColumn<KitUsageDTO, String> colSterilizerId;
    public TableColumn<KitUsageDTO, String> colListNumber;
    public TableColumn<KitUsageDTO, String> colSterilizerType;
    public TableColumn<KitUsageDTO, String> colSterilizerBrand;
    public TableColumn<KitUsageDTO, String> colResult;
    public TableColumn<KitUsageDTO, String> colSerialNumber;
    public HBox paginationContainer;
    private CustomDialog customDialog;
    private ObservableList<KitUsageDTO> kitsUsagesList = FXCollections.observableArrayList();
    private OptionalMethod method;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new OptionalMethod();
        startThread(0);

        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    int pageIndex = newValue1.intValue();
                    startThread(pageIndex);
                });
    }

    public void startThread(int pageIndex) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(pageIndex);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void totalSterilizerCardClick(MouseEvent mouseEvent) {
        customDialog.showFxmlFullDialog("sterilizer/sterilizers.fxml", "ALL STERILIZERS");
    }

    public void totalKitsCardClick(MouseEvent mouseEvent) {
        customDialog.showFxmlFullDialog("kit/kits.fxml", "ALL KITS");
    }

    public void totalUserCardClick(MouseEvent mouseEvent) {

        customDialog.showFxmlFullDialog("profile/users.fxml", "ALL USERS");

    }

    public void refreshClick(MouseEvent event) {

        startThread(0);
    }

    private void getAllKitsUsages(int pageIndex) {
        paginationContainer.setDisable(true);
        if (null != kitsUsagesList) {
            kitsUsagesList.clear();
        }
        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getKitsUsagesUrl());

            Long epochMills = CommonUtility.getCurrentLocalDateTimeInEpoch();
            param.setParameter("q[from_date]", String.valueOf(epochMills));
            param.setParameter("q[to_date]", String.valueOf(epochMills));

            param.setParameter("size", String.valueOf(50));
            param.setParameter("page", String.valueOf(pageIndex));

            HttpGet httpGet = new HttpGet(param.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {

                    KitPageResponse KkitPageResponse = new Gson().fromJson(content, KitPageResponse.class);
                    List<KitUsageDTO> kds = KkitPageResponse.getKitUsages();
                    kitsUsagesList = FXCollections.observableArrayList(kds);

                    paginationContainer.setVisible(kitsUsagesList.size() > 0);
                    paginationContainer.setDisable(!(kitsUsagesList.size() > 0));
                    int totalPage = KkitPageResponse.getTotalPage();
                    changeTableView(totalPage, pageIndex);

                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {

                    Platform.runLater(() -> {
                        tableview.setPlaceholder(new Label("Kit Usage not found"));
                    });
                }

            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> {
                refreshBn.setDisable(false);
            });
        }
    }

    private void changeTableView(int totalPage, int pageIndex) {
        Platform.runLater(() -> {
            pagination.setPageCount(totalPage);
            pagination.setCurrentPageIndex(pageIndex);
        });

        setOptionalCell();
        tableview.setItems(kitsUsagesList);

        tableview.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(KitUsageDTO item, boolean empty) {
                super.updateItem(item, empty);

                if (null == item) {
                    setStyle("-fx-background-color: white");
                } else {
                    int num = getIndex();
                    if (num % 2 == 0) {
                        setStyle("-fx-background-color: white;" +
                                "-fx-border-color:white white #d5d7d9 white ;");
                    } else {
                        setStyle("-fx-background-color: #d5d7d9;-fx-border-color:transparent");
                    }

                }
            }
        });
    }

    private void setOptionalCell() {

        colId.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());
                    if (null != String.valueOf(kd.getId())) {
                        String txt = String.valueOf(kd.getId());
                        if (!String.valueOf(txt).isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colKitNumber.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getKitNumber()) {

                        String txt = String.valueOf(kd.getKitNumber());
                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colTestDate.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getTestDate()) {

                        LocalDateTime localDateTime = CommonUtility.getLocalDateTimeObject(kd.getTestDate());
                        String txt = localDateTime.format(CommonUtility.dateFormatter);

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colSterilizerId.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getSterilizerID()) {

                        String txt = String.valueOf(kd.getSterilizerID());

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colListNumber.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getSterilizerListNumber()) {

                        String txt = String.valueOf(kd.getSterilizerListNumber());

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colSterilizerType.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getSterilizerType()) {

                        String txt = String.valueOf(kd.getSterilizerType());

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colSterilizerBrand.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getSterilizerBrand()) {

                        String txt = String.valueOf(kd.getSterilizerBrand());

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colResult.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getTestResult()) {

                        String txt = String.valueOf(kd.getTestResult());

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colSerialNumber.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitUsageDTO kd = tableview.getItems().get(getIndex());
                    if (null != kd.getSterilizerSerialNumber()) {

                        String txt = String.valueOf(kd.getSterilizerSerialNumber());

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setText(null);
                            setGraphic(text);

                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private int pageIndex;

        public MyAsyncTask(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        @Override
        public void onPreExecute() {

            if (null != tableview) {
                tableview.setItems(null);
                tableview.refresh();
            }
            assert tableview != null;
            tableview.setPlaceholder(method.getProgressBar(40, 40));
        }
        @Override
        public Boolean doInBackground(String... params) {
            getAllKitsUsages(pageIndex);
            Platform.runLater(Home.this::countData);

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
