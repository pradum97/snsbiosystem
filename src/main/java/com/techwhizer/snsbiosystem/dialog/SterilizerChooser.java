package com.techwhizer.snsbiosystem.dialog;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.pagination.PaginationUtil;
import com.techwhizer.snsbiosystem.sterilizer.constants.SterilizerSortingOptions;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerPageResponse;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerTableView;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SterilizerChooser implements Initializable {
    public ComboBox<String> sortingCom;
    public ComboBox<String> orderCom;
    public Button applySorting;
    public HBox paginationContainer;
    public ComboBox<Integer> rowSizeCom;
    public TableColumn<SterilizerTableView, Integer> colSrNo;
    public TableColumn<SterilizerTableView, String> colSerialId;
    public TableColumn<SterilizerTableView, String> colType;
    public TableColumn<SterilizerTableView, String> colAction;
    public TableView<SterilizerTableView> tableView;
    public Pagination pagination;
    private CustomDialog customDialog;
    private OptionalMethod method;
    private ObservableList<SterilizerTableView> sterilizerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        pagination.setCurrentPageIndex(0);
        callThread(null, OperationType.SORTING_LOADING);
    }

    private void comboBoxConfig() {
        pagination.setCurrentPageIndex(0);
        rowSizeCom.setItems(PaginationUtil.rowSize);
        sortingCom.setItems(FXCollections.observableArrayList(SterilizerSortingOptions.sortingMap.keySet()));

        Platform.runLater(() -> {
            orderCom.setItems(CommonUtility.orderList);
            orderCom.getSelectionModel().selectFirst();
            sortingCom.getSelectionModel().selectFirst();

            rowSizeCom.valueProperty().addListener((observableValue, integer, rowPerPage) -> {
                sortData(0, OperationType.START, null);
            });
            rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE);

            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {

                        int pageIndex = newValue1.intValue();
                        sortData(pageIndex, OperationType.START, null);

                    });
            applySorting.setDisable(false);

        });
    }

    public void applySorting(ActionEvent event) {
        sortData(0, OperationType.START, null);
    }

    private void sortData(int pageIndex, OperationType operationType, Long sterilizerId) {

        String filedName = SterilizerSortingOptions.getKeyValue(sortingCom.getSelectionModel().getSelectedItem());
        String order = CommonUtility.parserOrder(orderCom.getSelectionModel().getSelectedItem());
        int rowSize = rowSizeCom.getSelectionModel().getSelectedItem();
        String sort = filedName + "," + order;

        Map<String, Object> map = new HashMap<>();
        map.put("sort", sort);
        map.put("row_size", rowSize);
        map.put("page_index", pageIndex);

        callThread(map, operationType);
    }

    private void callThread(Map<String, Object> sortingMap, OperationType operationType) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(sortingMap, operationType);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();

        System.out.println(sortingMap);
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private Map<String, Object> sortingMap;
        OperationType operationType;

        public MyAsyncTask(Map<String, Object> sortingMap, OperationType operationType) {
            this.sortingMap = sortingMap;
            this.operationType = operationType;
        }

        @Override
        public void onPreExecute() {
            tableView.setPlaceholder(method.getProgressBar(30, 30));
        }

        @Override
        public Boolean doInBackground(String... params) {

            if (operationType == OperationType.SORTING_LOADING) {
                comboBoxConfig();
            } else {
                getAllSterilizer(sortingMap);
            }

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setPlaceholder(new Label("Sterilizer not found"));
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getAllSterilizer(Map<String, Object> sortingMap) {
        paginationContainer.setDisable(true);
        if (null != sterilizerList) {
            sterilizerList.clear();
        }
        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getAddSterilizerUrl());

            if (null != sortingMap) {
                String sort = (String) sortingMap.get("sort");
                int rowSize = (Integer) sortingMap.get("row_size");
                int pageIndex = (Integer) sortingMap.get("page_index");
                param.setParameter("sort", sort);
                param.setParameter("size", String.valueOf(rowSize));
                param.setParameter("page", String.valueOf(pageIndex));

                Long sterilizerId = (Long) sortingMap.get("sterilizer_id");

                if (null != sterilizerId) {
                    param.setParameter("sterilizer-id", String.valueOf(pageIndex));
                }
            }

            System.out.println("param: " + param);

            HttpGet httpGet = new HttpGet(param.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                String content = EntityUtils.toString(resEntity);

                System.out.println(statusCode);

                if (statusCode == 200) {

                    if (!content.isEmpty()) {
                        SterilizerPageResponse pageResponse = null;
                        List<SterilizerTableView> stvs = null;

                        try {
                            pageResponse = new Gson().fromJson(content, SterilizerPageResponse.class);
                            stvs = pageResponse.getSterilizers();

                        } catch (JsonSyntaxException e) {
                            SterilizerTableView sterilizerTableView = new Gson().fromJson(content, SterilizerTableView.class);
                            stvs.add(sterilizerTableView);
                            throw new RuntimeException(e);
                        }
                        sterilizerList = FXCollections.observableArrayList(stvs);
                        int size = sterilizerList.size();

                        paginationContainer.setVisible(size > 0);
                        paginationContainer.setDisable(!(size > 0));
                        int totalPage = pageResponse.getTotalPage();
                        changeTableView(totalPage);
                    }


                } else if (statusCode == 401) {
                    new HttpStatusHandler(401);
                } else {
                    Platform.runLater(() -> {
                        tableView.setPlaceholder(new Label(Message.SOMETHING_WENT_WRONG));
                    });
                }

            }
        } catch (IOException | URISyntaxException | InterruptedException | JsonSyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> {
                tableView.setPlaceholder(new Label("Sterilizer not found"));
            });
        }
    }

    private void changeTableView(int totalPage) {
        Platform.runLater(() -> {
            pagination.setPageCount(totalPage);
        });

        setOptionalCell();
        tableView.setItems(sterilizerList);

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SterilizerTableView item, boolean empty) {
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


        colSerialId.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerTableView stv = tableView.getItems().get(getIndex());

                    if (null != String.valueOf(stv.getId())) {

                        String txt = String.valueOf(stv.getId());

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

        colType.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerTableView stv = tableView.getItems().get(getIndex());

                    if (null != stv.getType()) {

                        String txt = stv.getType();
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

        colAction.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button selectBn = new Button();

                    ImageView iv = new ImageView(new ImageLoader().load("img/icon/rightArrow_ic_white.png"));
                    iv.setFitHeight(17);
                    iv.setFitWidth(17);

                    selectBn.setGraphic(iv);
                    selectBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");

                    selectBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableView);
                        SterilizerTableView stv = tableView.getSelectionModel().getSelectedItem();

                        if (null != stv) {

                            Map<String,Object> map = new HashMap<>();
                            map.put("sterilizer_id",stv.getId());
                            map.put("sterilizer_list_number",stv.getListNumber());
                            map.put("sterilizer_type",stv.getType());

                            Main.primaryStage.setUserData(map);
                            Stage stage = (Stage) sortingCom.getScene().getWindow();
                            if (null != stage && stage.isShowing()) {
                                stage.close();
                            }
                        }
                    });
                    HBox managebtn = new HBox(selectBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(selectBn, new Insets(0, 0, 0, 0));
                    setText(null);
                    setGraphic(managebtn);
                }
            }

        });
    }
}
