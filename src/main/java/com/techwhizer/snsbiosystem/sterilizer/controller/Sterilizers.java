package com.techwhizer.snsbiosystem.sterilizer.controller;

import com.google.gson.Gson;
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
import com.techwhizer.snsbiosystem.util.*;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class Sterilizers implements Initializable {
    public ImageView refreshBn;
    public TableView<SterilizerTableView> tableview;
    public TableColumn<SterilizerTableView, Integer> colSlNum;
    public TableColumn<SterilizerTableView, String> colSterilizerId;
    public TableColumn<SterilizerTableView, String> colSterilizerType;
    public TableColumn<SterilizerTableView, String> colSterilizerBrand;
    public TableColumn<SterilizerTableView, String> colSterilizerSerialNumber;
    public TableColumn<SterilizerTableView, String> colSterilizerListNumber;
    public TableColumn<SterilizerTableView, String> colAction;
    public Pagination pagination;
    public ComboBox<Integer> rowSizeCom;
    public ComboBox<String> sortingCom;
    public ComboBox<String> orderCom;
    public Button applySorting;
    public HBox paginationContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private int currentPageRowCount;
    private boolean isCrud = false;

    private ObservableList<SterilizerTableView> sterilizerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        startThread(OperationType.SORTING_LOADING, 0L, null, null);
    }

    private void comboBoxConfig() {
        pagination.setCurrentPageIndex(0);
        rowSizeCom.setItems(PaginationUtil.rowSize);
        sortingCom.setItems(FXCollections.observableArrayList(SterilizerSortingOptions.sortingMap.keySet()));

        Platform.runLater(() -> {
            orderCom.setItems(CommonUtility.orderList);
            orderCom.getSelectionModel().selectFirst();
            sortingCom.getSelectionModel().selectFirst();
        });

        rowSizeCom.valueProperty().addListener((observableValue, integer, rowPerPage) -> {
            sortData(0, 0, OperationType.START, 0L, null);
        });

        Platform.runLater(() -> rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE));

        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    if (!isCrud) {
                        int pageIndex = newValue1.intValue();
                        sortData(pageIndex, 0, OperationType.START, 0L, null);
                    }

                });
        applySorting.setDisable(false);
    }

    public void applySorting(ActionEvent event) {
        sortData(0, 0,
                OperationType.START, 0L, null);
    }

    private void sortData(int pageIndex, int tableRowIndex, OperationType operationType, Long sterilizerId, Long searchSterilizerId) {

        String filedName = SterilizerSortingOptions.getKeyValue(sortingCom.getSelectionModel().getSelectedItem());
        String order = CommonUtility.parserOrder(orderCom.getSelectionModel().getSelectedItem());
        int rowSize = rowSizeCom.getSelectionModel().getSelectedItem();
        String sort = filedName + "," + order;

        Map<String, Object> map = new HashMap<>();
        map.put("sort", sort);
        map.put("row_size", rowSize);
        map.put("page_index", pageIndex);
        map.put("row_index", tableRowIndex);

        startThread(operationType, sterilizerId, null, map);
    }

    public void refreshClick(MouseEvent mouseEvent) {
        applySorting(null);
    }

    public void uploadSterilizerBnClick(ActionEvent event) {

        File file = new ChooseFile().chooseCSVFile();
        if (null != file) {
            Main.primaryStage.setUserData(file);
            customDialog.showFxmlFullDialog("sterilizer/previewSterilizer.fxml", "STERILIZER LIST");
            if (Main.primaryStage.getUserData() instanceof Boolean) {

                boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                if (isUpdated) {
                    isCrud = true;
                    sortData(pagination.getCurrentPageIndex(), 0, OperationType.START, 0L, null);
                }
            }
        }
    }

    private class MyAsyncTask extends AsyncTask<Object, Integer, Boolean> {
        private OperationType operationType;
        private Long sterilizerId;
        private Button button;
        private Map<String, Object> sortingMap;

        public MyAsyncTask(OperationType operationType, Long sterilizerId,
                           Button button, Map<String, Object> sortingMap) {
            this.operationType = operationType;
            this.sterilizerId = sterilizerId;
            this.button = button;
            this.sortingMap = sortingMap;
        }

        @Override
        public void onPreExecute() {

            refreshBn.setDisable(true);
            applySorting.setDisable(true);

            if (operationType == OperationType.DELETE) {
                if (null != button) {
                    ProgressIndicator pi = method.getProgressBar(25, 25);
                    pi.setStyle("-fx-progress-color: white;-fx-border-width: 2");
                    button.setGraphic(pi);
                }
            } else {
                if (null != tableview) {
                    tableview.setItems(null);
                    tableview.refresh();
                }
                assert tableview != null;
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            }
        }

        @Override
        public Boolean doInBackground(Object... params) {

            switch (operationType) {
                case SORTING_LOADING -> {
                    comboBoxConfig();
                }
                case START -> getAllSterilizer(sortingMap);
                case DELETE -> deleteSterilizer(sterilizerId, button, sortingMap);
            }
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            refreshBn.setDisable(false);
            applySorting.setDisable(false);
            if (null != button) {
                button.setGraphic(getImage("img/icon/delete_ic_white.png"));
            }
            tableview.setPlaceholder(new Label("Sterilizer not found"));

            if (sterilizerList.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("Sterilizer not found"));
            }
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
            }

            HttpGet httpGet = new HttpGet(param.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {

                    SterilizerPageResponse pageResponse = new Gson().fromJson(content, SterilizerPageResponse.class);
                    List<SterilizerTableView> stvs = pageResponse.getSterilizers();
                    sterilizerList = FXCollections.observableArrayList(stvs);

                    currentPageRowCount = sterilizerList.size();
                    int totalPage = pageResponse.getTotalPage();

                    paginationContainer.setVisible(currentPageRowCount > 0);
                    paginationContainer.setDisable(!(currentPageRowCount > 0));
                    changeTableView(totalPage, (Integer) sortingMap.get("page_index"),
                            (Integer) sortingMap.get("row_index"));

                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                }
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> {
                refreshBn.setDisable(false);
            });
        }
    }

    public void startThread(OperationType operationType, Long sterilizerId, Button button, Map<String, Object> map) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(operationType, sterilizerId, button, map);
        myAsyncTask.setDaemon(true);
        myAsyncTask.execute();
    }

    private void deleteSterilizer(Long sterilizerId, Button button, Map<String, Object> sortingMap) {
        int pageIndex = (Integer) sortingMap.get("page_index");
        int rowIndex = (Integer) sortingMap.get("row_index");

        if (pagination.getPageCount() > 1) {
            if (currentPageRowCount < 2) {
                pageIndex = pageIndex - 1;
                rowIndex = 0;
            }
        }

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpDelete httpMethod = new HttpDelete(UrlConfig.getDeleteSterilizerUrl().concat(String.valueOf(sterilizerId)));
            httpMethod.addHeader("Content-Type", "application/json");
            httpMethod.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpMethod);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    isCrud = true;
                    sortData(pageIndex, rowIndex, OperationType.START, 0L, null);
                    customDialog.showAlertBox("", content);
                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    customDialog.showAlertBox("", content);
                }
            }
        } catch (IOException | InterruptedException e) {
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> {
                if (null != button) {
                    button.setGraphic(getImage("img/icon/delete_ic_white.png"));
                }
                refreshBn.setDisable(false);
            });
        }
    }

    private ImageView getImage(String path) {

        ImageView iv = new ImageView(new ImageLoader().load(path));
        iv.setFitHeight(17);
        iv.setFitWidth(17);

        return iv;
    }

    private void changeTableView(int totalPage, int pageIndex, int rowIndex) {

        Platform.runLater(() -> {
            pagination.setPageCount(totalPage);
            pagination.setCurrentPageIndex(pageIndex);
            tableview.scrollTo(rowIndex);

            isCrud = false;
        });

        colSlNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));

        colSterilizerListNumber.setCellValueFactory(new PropertyValueFactory<>("listNumber"));

        setOptionalCell();

        tableview.setItems(sterilizerList);

        tableview.setRowFactory(tv -> new TableRow<>() {
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

        colAction.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button editBn = new Button();
                    Button deleteBbn = new Button();

                    CommonUtility.onHoverShowTextButton(editBn, "Update sterilizer");
                    CommonUtility.onHoverShowTextButton(deleteBbn, "Delete Sterilizer");

                    ImageView activeIc = getImage("img/icon/admin_icon.png");
                    activeIc.setFitWidth(30);
                    activeIc.setFitHeight(30);

                    editBn.setGraphic(getImage("img/icon/update_ic.png"));
                    deleteBbn.setGraphic(getImage("img/icon/delete_ic_white.png"));


                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3;-fx-padding: 4 ");
                    deleteBbn.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3;-fx-padding: 4 ");


                    editBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        SterilizerTableView stv = tableview.getSelectionModel().getSelectedItem();
                        Map<String, Object> map = new HashMap<>();
                        map.put("operation_type", OperationType.UPDATE);
                        map.put("sterilizer_data", stv);
                        Main.primaryStage.setUserData(map);

                        customDialog.showFxmlFullDialog("sterilizer/addSterilizer.fxml", "UPDATE STERILIZER");
                        if (Main.primaryStage.getUserData() instanceof Boolean) {

                            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                            if (isUpdated) {
                                int rowPosition = getIndex();
                                int paginationIndex = pagination.getCurrentPageIndex();
                                sortData(paginationIndex, rowPosition, OperationType.START, stv.getId(), null);
                            }
                        }

                    });

                    deleteBbn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        SterilizerTableView stv = tableview.getSelectionModel().getSelectedItem();
                        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
                        image.setFitWidth(45);
                        image.setFitHeight(45);
                        Alert alert = new Alert(Alert.AlertType.NONE);
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Warning ");
                        alert.setGraphic(image);
                        alert.setHeaderText("Are you sure you want to delete this sterilizer?");
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.initOwner(Main.primaryStage);
                        Optional<ButtonType> result = alert.showAndWait();
                        ButtonType button = result.orElse(ButtonType.CANCEL);
                        if (button == ButtonType.OK) {

                            int rowPosition = getIndex() - 1;
                            int paginationIndex = pagination.getCurrentPageIndex();
                            sortData(paginationIndex, rowPosition, OperationType.DELETE, stv.getId(), null);

                        } else {
                            alert.close();
                        }
                    });
                    HBox managebtn = new HBox(editBn, deleteBbn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(editBn, new Insets(0, 30, 0, 30));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        });


        colSterilizerId.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerTableView stv = tableview.getItems().get(getIndex());

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

        colSterilizerType.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerTableView stv = tableview.getItems().get(getIndex());

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

        colSterilizerBrand.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerTableView stv = tableview.getItems().get(getIndex());

                    if (null != stv.getBrand()) {

                        String txt = stv.getBrand();

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

        colSterilizerSerialNumber.setCellFactory((TableColumn<SterilizerTableView, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerTableView stv = tableview.getItems().get(getIndex());
                    String txt = stv.getSerialNumber();
                    if (null != txt && !txt.isEmpty()) {
                        Text text = new Text(txt);
                        text.setStyle("-fx-text-alignment:center;");
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                        setGraphic(text);
                        setText(null);
                    } else {
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        setGraphic(null);
                    }
                }
            }

        });
    }
    public void addSterilizerBnClick(ActionEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("operation_type", OperationType.CREATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlFullDialog("sterilizer/addSterilizer.fxml", "CREATE NEW STERILIZER");
        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                isCrud = true;
                sortData(pagination.getCurrentPageIndex(), 0, OperationType.START, 0L, null);
            }
        }
    }
}
