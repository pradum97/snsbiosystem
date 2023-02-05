package com.techwhizer.snsbiosystem.kit.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.constants.KitOperationType;
import com.techwhizer.snsbiosystem.kit.constants.KitSearchFilters;
import com.techwhizer.snsbiosystem.kit.constants.KitSortingOptions;
import com.techwhizer.snsbiosystem.kit.model.KitDTO;
import com.techwhizer.snsbiosystem.kit.model.KitPageResponse;
import com.techwhizer.snsbiosystem.pagination.PaginationUtil;
import com.techwhizer.snsbiosystem.report.DownloadReport;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.ChooseFile;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.LocalDb;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class Kits implements Initializable {
    public ImageView refreshBn;
    public ComboBox<String> searchByCom;
    public TextField searchTf;
    public TableView<KitDTO> tableview;
    public TableColumn<KitDTO, Integer> colSlNum;
    public TableColumn<KitDTO, String> colClientId;
    public TableColumn<KitDTO, String> colKitId;
    public TableColumn<KitDTO, String> colDealerId;
    public TableColumn<KitDTO, String> colKitNumber;
    public TableColumn<KitDTO, String> colExpiryDate;
    public TableColumn<KitDTO, String> colTestUsed;
    public TableColumn<KitDTO, String> colLotNumber;
    public TableColumn<KitDTO, String> colAction;
    public Pagination pagination;
    public HBox topButtonContainer;

    public ComboBox<String> orderCom;
    public ComboBox<String> sortingCom;
    public ComboBox<Integer> rowSizeCom;
    public Button applySorting;
    public HBox paginationContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;

    private ObservableList<KitDTO> kitsList = FXCollections.observableArrayList();
    private FilteredList<KitDTO> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();

        if (null != Main.primaryStage.getUserData() && Main.primaryStage.getUserData() instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();
            if (map.get("operation_type") == KitOperationType.PREVIEW_INDIVIDUAL_KIT) {
                method.hideElement(topButtonContainer);
            } else {
                topButtonContainer.setVisible(true);
            }
        }

        searchByCom.setItems(new LocalDb().getKitsSearchType());
        searchByCom.getSelectionModel().select(KitSearchFilters.KIT_NUMBER);
        searchByCom.valueProperty().addListener((observableValue, s, t1) -> searchTf.setText(""));

        startThread(OperationType.SORTING_LOADING, 0L, null, null, null);
    }

    public void kitUsages(ActionEvent event) {

        Map<String, Object> data = new HashMap<>();

        data.put("operation_type", OperationType.ALL);
        Main.primaryStage.setUserData(data);
        customDialog.showFxmlFullDialog("kit/kitUsages/kitUsages.fxml", "KIT USAGE LIST");
    }

    private void comboBoxConfig() {
        pagination.setCurrentPageIndex(0);
        rowSizeCom.setItems(PaginationUtil.rowSize);
        sortingCom.setItems(FXCollections.observableArrayList(KitSortingOptions.sortingMap.keySet()));

        Platform.runLater(()->{
            orderCom.setItems(CommonUtility.orderList);
            orderCom.getSelectionModel().selectFirst();
            sortingCom.getSelectionModel().selectFirst();

            rowSizeCom.valueProperty().addListener((observableValue, integer, rowPerPage) -> {
                sortData(0, 0, OperationType.START, 0L, null, null);
            });
            rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE);
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        int pageIndex = newValue1.intValue();
                        sortData(pageIndex, 0, OperationType.START, 0L, null, null);
                    });
            applySorting.setDisable(false);
        });
    }
    public void applySorting(ActionEvent event) {
        sortData(pagination.getCurrentPageIndex(), 0, OperationType.START, 0L, null, null);
    }

    private void sortData(int pageIndex, int tableRowIndex, OperationType operationType, Long kitId, Button button,
                          Map<String, Object> reportMap) {

        String filedName = KitSortingOptions.getKeyValue(sortingCom.getSelectionModel().getSelectedItem());
        String order = CommonUtility.parserOrder(orderCom.getSelectionModel().getSelectedItem());
        int rowSize = rowSizeCom.getSelectionModel().getSelectedItem();
        String sort = filedName + "," + order;

        Map<String, Object> sortedDataMap = new HashMap<>();
        sortedDataMap.put("sort", sort);
        sortedDataMap.put("row_size", rowSize);
        sortedDataMap.put("page_index", pageIndex);
        sortedDataMap.put("row_index", tableRowIndex);

        startThread(operationType, kitId, button, reportMap, sortedDataMap);
    }

    public void startThread(OperationType operationType, Long kitId, Button button, Map<String, Object> reportMap,
                            Map<String, Object> sortedDataMap) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(operationType, kitId, button, reportMap, sortedDataMap);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<Object, Integer, Boolean> {
        private OperationType operationType;
        private Long kitId;
        private Button button;
        private Map<String, Object> reportMap;
        private Button downloadButton;
        private Map<String, Object> sortedDataMap;

        public MyAsyncTask(OperationType operationType, Long kitId, Button button, Map<String,
                Object> reportMap, Map<String, Object> sortedDataMap) {
            this.operationType = operationType;
            this.kitId = kitId;
            this.button = button;
            this.reportMap = reportMap;
            this.sortedDataMap = sortedDataMap;
            if (null != reportMap) {
                downloadButton = (Button) reportMap.get("button");
            }
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
            } else if (operationType != OperationType.DOWNLOAD_REPORT) {
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
                case SORTING_LOADING -> comboBoxConfig();
                case START -> getAllKits(sortedDataMap);
                case DELETE -> deleteKit(kitId, button, sortedDataMap);
                case DOWNLOAD_REPORT -> {
                    if (null != reportMap) {
                        new DownloadReport().dialogController(reportMap, OperationType.KIT_REPORT);
                    }
                }
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
            tableview.setPlaceholder(new Label("kit not found"));

            if (kitsList.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("kit not found"));
            }

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

    private void deleteKit(Long kitsId, Button button, Map<String, Object> sortingMap) {

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpDelete httpMethod = new HttpDelete(UrlConfig.getDeleteKitUrl().concat(String.valueOf(kitsId)));
            httpMethod.addHeader("Content-Type", "application/json");
            httpMethod.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpMethod);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    int pageIndex = (Integer) sortingMap.get("page_index");
                    int rowIndex = (Integer) sortingMap.get("row_index");
                    sortData(pageIndex, rowIndex, OperationType.START, 0L, null, null);

                }

                customDialog.showAlertBox("", content);
            }
        } catch (IOException | InterruptedException e) {
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

    private void getAllKits(Map<String, Object> sortedDataMap) {

        if (null != kitsList) {
            kitsList.clear();
        }

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getGetKitsUrl());

            if (null != sortedDataMap) {
                String sort = (String) sortedDataMap.get("sort");
                int rowSize = (Integer) sortedDataMap.get("row_size");
                int pageIndex = (Integer) sortedDataMap.get("page_index");
                param.setParameter("sort", sort);
                param.setParameter("size", String.valueOf(rowSize));
                param.setParameter("page", String.valueOf(pageIndex));
            }

            if (null != Main.primaryStage.getUserData() && Main.primaryStage.getUserData() instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();
                if (map.get("operation_type") == KitOperationType.PREVIEW_INDIVIDUAL_KIT) {
                    Long userId = (Long) map.get("user_id");
                    param.setParameter("q[user_id]", String.valueOf(userId));
                    System.out.println(userId);
                } else {
                }
            }
            HttpGet httpGet = new HttpGet(param.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                KitPageResponse KkitPageResponse = new Gson().fromJson(content, KitPageResponse.class);
                List<KitDTO> kds = KkitPageResponse.getKits();

                kitsList = FXCollections.observableArrayList(kds);
                if (kitsList.size() > 0) {
                    paginationContainer.setVisible(true);
                    int totalPage = KkitPageResponse.getTotalPage();

                    assert sortedDataMap != null;
                    search_Item(totalPage, (Integer) sortedDataMap.get("page_index"), (Integer) sortedDataMap.get("row_index"));

                }

            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(() -> {
            refreshBn.setDisable(false);
        });

    }

    private ImageView getImage(String path) {

        ImageView iv = new ImageView(new ImageLoader().load(path));
        iv.setFitHeight(17);
        iv.setFitWidth(17);

        return iv;
    }

    private void search_Item(int totalPage, int pageIndex, Integer rowIndex) {
        searchTf.setText("");

        filteredData = new FilteredList<>(kitsList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(kit -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String searchBy = searchByCom.getSelectionModel().getSelectedItem();

                switch (searchBy) {

                    case KitSearchFilters.DEALER_ID -> {

                        return null != kit.getDealerID() &&
                                String.valueOf(kit.getDealerID()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case KitSearchFilters.CLIENT_ID -> {
                        return null != kit.getClientID() &&
                                String.valueOf(kit.getClientID()).toLowerCase().equals(lowerCaseFilter);
                    }

                    case KitSearchFilters.KIT_ID -> {
                        return null != kit.getId() &&
                                String.valueOf(kit.getId()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }

                    default -> {

                        return null != kit.getKitNumber() &&
                                String.valueOf(kit.getKitNumber()).toLowerCase().contains(lowerCaseFilter);
                    }
                }
            });

            changeTableView(0, 0, rowIndex);

        });
        changeTableView(totalPage, pageIndex, rowIndex);
    }

    private void changeTableView(int totalPage, int pageIndex, int rowIndex) {

        Platform.runLater(() -> {
            pagination.setPageCount(totalPage);

            if (filteredData.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("Kit not found"));
            }

            pagination.setCurrentPageIndex(pageIndex);
            tableview.scrollTo(rowIndex);

        });

        colSlNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));
        setOptionalCell();

        tableview.setItems(filteredData);

        tableview.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(KitDTO item, boolean empty) {
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

        colAction.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button editBn = new Button();
                    Button deleteBbn = new Button();

                    Button viewUsageBn = new Button();
                    Button downloadBn = new Button();

                    editBn.setGraphic(getImage("img/icon/update_ic.png"));
                    deleteBbn.setGraphic(getImage("img/icon/delete_ic_white.png"));

                    viewUsageBn.setGraphic(getImage("img/icon/preview_ic.png"));
                    downloadBn.setGraphic(getImage("img/icon/download_ic.png"));

                    ImageView activeIc = getImage("img/icon/admin_icon.png");
                    activeIc.setFitWidth(30);
                    activeIc.setFitHeight(30);

                    CommonUtility.onHoverShowTextButton(editBn,"Update kit");
                    CommonUtility.onHoverShowTextButton(deleteBbn,"Delete kit");
                    CommonUtility.onHoverShowTextButton(viewUsageBn,"View kit");
                    CommonUtility.onHoverShowTextButton(downloadBn,"Download kit report");

                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");
                    deleteBbn.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3 ");
                    downloadBn.setStyle("-fx-cursor: hand ; -fx-background-color: #051b64 ; -fx-background-radius: 3 ");
                    viewUsageBn.setStyle("-fx-cursor: hand ; -fx-background-color: #04505e ; -fx-background-radius: 3 ");

                    downloadBn.setOnAction((event -> {
                        method.selectTable(getIndex(), tableview);

                        Map<String, Object> map = new HashMap<>();
                        map.put("button", downloadBn);
                        map.put("kit", tableview.getItems().get(getIndex()).getId());

                        int rowPosition = getIndex();
                        int paginationIndex = pagination.getCurrentPageIndex();
                        sortData(paginationIndex, rowPosition, OperationType.DOWNLOAD_REPORT, 0L, null, map);
                    }));
                    viewUsageBn.setOnAction(event -> {
                        method.selectTable(getIndex(), tableview);
                        KitDTO kd = tableview.getSelectionModel().getSelectedItem();

                        Map<String, Object> data = new HashMap<>();

                        data.put("operation_type", OperationType.SINGLE_KIT_USAGE);
                        data.put("kit_id", kd.getId());

                        Main.primaryStage.setUserData(data);

                        customDialog.showFxmlFullDialog("kit/kitUsages/kitUsages.fxml", "KIT USAGE LIST");
                    });

                    editBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        KitDTO kd = tableview.getSelectionModel().getSelectedItem();
                        Map<String, Object> map = new HashMap<>();
                        map.put("operation_type", OperationType.UPDATE);
                        map.put("kits_data", kd);
                        Main.primaryStage.setUserData(map);

                        customDialog.showFxmlFullDialog("kit/addKit.fxml", "UPDATE KIT");

                        if (Main.primaryStage.getUserData() instanceof Boolean) {
                            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                            if (isUpdated) {
                                int rowPosition = getIndex();
                                int paginationIndex = pagination.getCurrentPageIndex();
                                sortData(paginationIndex, rowPosition, OperationType.START, 0L, null, null);
                            }
                        }

                    });

                    deleteBbn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        KitDTO kd = tableview.getSelectionModel().getSelectedItem();
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
                            sortData(paginationIndex, rowPosition, OperationType.DELETE, kd.getId(), deleteBbn, null);

                        } else {
                            alert.close();
                        }
                    });

                    HBox managebtn = new HBox(viewUsageBn, downloadBn, editBn, deleteBbn);
                    managebtn.setStyle("-fx-alignment:center");
                    managebtn.setSpacing(20);
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        });


        colKitId.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());
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

        colClientId.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getClientID()) {

                        String txt = String.valueOf(kd.getClientID());
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

        colDealerId.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getDealerID()) {

                        String txt = String.valueOf(kd.getDealerID());

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

        colKitNumber.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());

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
        colTestUsed.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getTestUsed()) {

                        String txt = String.valueOf(kd.getTestUsed());

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

        colExpiryDate.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getExpiryDate()) {

                        String txt = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN)
                                .format(new java.util.Date(kd.getExpiryDate()));

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

        colLotNumber.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    KitDTO kd = tableview.getItems().get(getIndex());

                    if (null != kd.getLotNumber()) {

                        String txt = String.valueOf(kd.getLotNumber());

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

    public void refreshClick(MouseEvent mouseEvent) {
        applySorting(null);
    }

    public void uploadKitsCsvBnClick(ActionEvent event) {
        File file = new ChooseFile().chooseCSVFile();
        if (null != file) {
            Main.primaryStage.setUserData(file);
            customDialog.showFxmlFullDialog("kit/previewKits.fxml", "KITS LIST");
            if (Main.primaryStage.getUserData() instanceof Boolean) {

                boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                if (isUpdated) {
                    applySorting(null);
                }
            }
        }
    }

    public void addKitsBnClick(ActionEvent event) {

        Map<String, Object> map = new HashMap<>();
        map.put("operation_type", OperationType.CREATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlFullDialog("kit/addKit.fxml", "CREATE NEW KIT");
        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                applySorting(null);
            }
        }
    }

}
