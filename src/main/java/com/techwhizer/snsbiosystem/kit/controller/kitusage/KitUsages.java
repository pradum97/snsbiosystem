package com.techwhizer.snsbiosystem.kit.controller.kitusage;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.constants.KitUsageSearchType;
import com.techwhizer.snsbiosystem.kit.constants.KitUsageSortingOptions;
import com.techwhizer.snsbiosystem.kit.model.KitPageResponse;
import com.techwhizer.snsbiosystem.kit.model.KitUsageDTO;
import com.techwhizer.snsbiosystem.pagination.PaginationUtil;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.*;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class KitUsages implements Initializable {
    public ImageView refreshBn;
    public ComboBox<String> searchByCom;
    public TextField searchTf;
    public TableView<KitUsageDTO> tableview;
    public TableColumn<KitUsageDTO, String> colAction;
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
    public ComboBox<String> orderCom;
    public ComboBox<String> sortingCom;
    public ComboBox<Integer> rowSizeCom;
    public Button applySorting;
    public HBox paginationContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    @FXML
    public HBox buttonContainer;
    private ObservableList<KitUsageDTO> kitsUsagesList = FXCollections.observableArrayList();
    private FilteredList<KitUsageDTO> filteredData;

    private OperationType operationType = OperationType.ALL;
    private Long kitId = 0L;
    private Map<String, Object> data;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        searchByCom.setItems(new LocalDb().getKitUsageSearchType());
        searchByCom.getSelectionModel().select(KitUsageSearchType.KIT_NUMBER);
        searchByCom.valueProperty().addListener((observableValue, s, t1) -> searchTf.setText(""));

        Platform.runLater(()->{
            OptionalMethod.minimizedStage((Stage) buttonContainer.getScene().getWindow(),true);
        });

        if (Main.primaryStage.getUserData() instanceof Map<?, ?>) {

            data = (Map<String, Object>) Main.primaryStage.getUserData();

            if (null != data.get("operation_type")) {
                operationType = (OperationType) data.get("operation_type");

                if (operationType == OperationType.SINGLE_KIT_USAGE) {
                    kitId = (Long) data.get("kit_id");
                    method.hideElement(buttonContainer);
                }

                startThread(OperationType.SORTING_LOADING, kitId, null, null);

            } else {
                startThread(OperationType.SORTING_LOADING, 0L, null, null);
            }

        } else {
            startThread(OperationType.SORTING_LOADING, 0L, null, null);
        }
    }

    private void comboBoxConfig(Long kitId) {
        pagination.setCurrentPageIndex(0);
        rowSizeCom.setItems(PaginationUtil.rowSize);
        sortingCom.setItems(FXCollections.observableArrayList(KitUsageSortingOptions.sortingMap.keySet()));

        Platform.runLater(()->{
            orderCom.setItems(CommonUtility.orderList);
            orderCom.getSelectionModel().selectFirst();
            sortingCom.getSelectionModel().selectFirst();
            rowSizeCom.valueProperty().addListener((observableValue, integer, rowPerPage) -> {
                sortData(0, 0, OperationType.START, 0L, null);
            });
            rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE);
            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        int pageIndex = newValue1.intValue();
                        sortData(pageIndex, 0, OperationType.START, kitId, null);
                    });
            applySorting.setDisable(false);
        });
    }
    public void applySorting(ActionEvent event) {
        sortData(pagination.getCurrentPageIndex(), 0, OperationType.START, kitId, null);
    }

    private void sortData(int pageIndex, int rowIndex, OperationType operationType, Long kitId, Button button) {

        String filedName = KitUsageSortingOptions.getKeyValue(sortingCom.getSelectionModel().getSelectedItem());
        String order = CommonUtility.parserOrder(orderCom.getSelectionModel().getSelectedItem());
        int rowSize = rowSizeCom.getSelectionModel().getSelectedItem();
        String sort = filedName + "," + order;

        Map<String, Object> sortedDataMap = new HashMap<>();
        sortedDataMap.put("sort", sort);
        sortedDataMap.put("row_size", rowSize);
        sortedDataMap.put("page_index", pageIndex);
        sortedDataMap.put("row_index", rowIndex);
        startThread(operationType, kitId, button, sortedDataMap);
    }

    public void startThread(OperationType operationType, Long kitId, Button button, Map<String, Object> sortedDataMap) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(operationType, kitId, button, sortedDataMap);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<Object, Integer, Boolean> {
        private OperationType operationType;
        private Long kitId;
        private Button button;
        private Map<String, Object> sortedDataMap;

        public MyAsyncTask(OperationType operationType, Long kitId, Button button,
                           Map<String, Object> sortedDataMap) {
            this.operationType = operationType;
            this.kitId = kitId;
            this.button = button;
            this.sortedDataMap = sortedDataMap;
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

            if (operationType == OperationType.SORTING_LOADING){
                comboBoxConfig(kitId);
            }else {
                getAllKitsUsages(sortedDataMap);
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
            tableview.setPlaceholder(new Label("Kit Usage not found"));

            if (kitsUsagesList.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("Kit Usage not found"));
            }
        }

        @Override
        public void progressCallback(Integer... params) {
        }
    }

    private void getAllKitsUsages(Map<String, Object> sortedDataMap) {
        if (null != kitsUsagesList) {
            kitsUsagesList.clear();
        }
        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getKitsUsagesUrl());
            if (null != sortedDataMap) {
                String sort = (String) sortedDataMap.get("sort");
                int rowSize = (Integer) sortedDataMap.get("row_size");
                int pageIndex = (Integer) sortedDataMap.get("page_index");
                param.setParameter("sort", sort);
                param.setParameter("size", String.valueOf(rowSize));
                param.setParameter("page", String.valueOf(pageIndex));
            }

            if (operationType == OperationType.SINGLE_KIT_USAGE) {
                param.addParameter("kit-id", String.valueOf(kitId));
            }

            HttpGet httpGet = new HttpGet(param.build());

            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                int statusCode = response.getStatusLine().getStatusCode();

                if ( statusCode == 200) {

                    KitPageResponse KkitPageResponse = new Gson().fromJson(content, KitPageResponse.class);
                    List<KitUsageDTO> kds = KkitPageResponse.getKitUsages();
                    kitsUsagesList = FXCollections.observableArrayList(kds);
                    if (kitsUsagesList.size() > 0) {
                        paginationContainer.setDisable(false);
                        int totalPage = KkitPageResponse.getTotalPage();
                        search_Item(totalPage,(Integer) sortedDataMap.get("page_index"),
                                (Integer) sortedDataMap.get("row_index"));
                    }
                }else if (statusCode == StatusCode.UNAUTHORISED) {
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

    private ImageView getImage(String path) {

        ImageView iv = new ImageView(new ImageLoader().load(path));
        iv.setFitHeight(17);
        iv.setFitWidth(17);

        return iv;
    }

    private void search_Item(int totalPage,int pageIndex, Integer rowIndex) {
        searchTf.setText("");

        filteredData = new FilteredList<>(kitsUsagesList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(kit -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String searchBy = searchByCom.getSelectionModel().getSelectedItem();

                switch (searchBy) {

                    case KitUsageSearchType.ID -> {

                        return null != kit.getId() &&
                                String.valueOf(kit.getId()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case KitUsageSearchType.KIT_NUMBER -> {
                        return null != kit.getKitNumber() &&
                                String.valueOf(kit.getKitNumber()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    //
                    case KitUsageSearchType.STERILIZER_ID -> {
                        return null != kit.getSterilizerID() &&
                                String.valueOf(kit.getSterilizerID()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case KitUsageSearchType.STERILIZER_LIST_NUMBER -> {
                        return null != kit.getSterilizerListNumber() &&
                                String.valueOf(kit.getSterilizerListNumber()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case KitUsageSearchType.STERILIZER_TYPE -> {
                        return null != kit.getSterilizerType() &&
                                String.valueOf(kit.getSterilizerType()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }

                    default -> {

                        return null != kit.getSterilizerSerialNumber() &&
                                String.valueOf(kit.getSterilizerSerialNumber()).toLowerCase().equals(lowerCaseFilter);
                    }
                }
            });

            if (filteredData.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("Kit Usage not found"));
            }

        });

        changeTableView(totalPage, pageIndex, rowIndex);
    }

    private void changeTableView(int totalPage, int pageIndex, int rowIndex) {
        Platform.runLater(() -> {
            pagination.setPageCount(totalPage);

            pagination.setCurrentPageIndex(pageIndex);
            tableview.scrollTo(rowIndex);
        });

        setOptionalCell();
        tableview.setItems(filteredData);

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

        colAction.setCellFactory((TableColumn<KitUsageDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Label editBn = new Label();
                    editBn.setGraphic(getImage("img/icon/update_ic.png"));

                    ImageView activeIc = getImage("img/icon/admin_icon.png");
                    activeIc.setFitWidth(30);
                    activeIc.setFitHeight(30);

                    CommonUtility.onHoverShowTextLabel(editBn,"Click to update kit usage");

                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3;-fx-padding: 2 10 2 10 ");
                    editBn.setOnMouseClicked((event) -> {
                        method.selectTable(getIndex(), tableview);
                        KitUsageDTO kd = tableview.getSelectionModel().getSelectedItem();
                        Map<String, Object> map = new HashMap<>();
                        map.put("operation_type", OperationType.UPDATE);
                        map.put("kits_data", kd);
                        Main.primaryStage.setUserData(map);
                        customDialog.showFxmlFullDialog("kit/kitUsages/addKitUsage.fxml", "UPDATE KIT USAGE");
                        if (Main.primaryStage.getUserData() instanceof Boolean) {

                            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                            if (isUpdated) {
                                int rowPosition = getIndex();
                                int paginationIndex = pagination.getCurrentPageIndex();
                                sortData(paginationIndex, rowPosition, operationType, kitId, null);
                            }
                        }

                    });

                    HBox managebtn = new HBox(editBn);
                    managebtn.setStyle("-fx-alignment:center");
                    managebtn.setSpacing(20);
                    setGraphic(managebtn);
                    setText(null);
                }
            }

        });


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

                        String txt = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN)
                                .format(new Date(kd.getTestDate()));

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

    public void refreshClick(MouseEvent mouseEvent) {
        applySorting(null);
    }

    public void uploadKitsUsageCsvBnClick(ActionEvent event) {
        File file = new ChooseFile().chooseCSVFile();
        if (null != file) {
            Main.primaryStage.setUserData(file);
            customDialog.showFxmlFullDialog("kit/kitUsages/previewKitUsage.fxml", "KITS USAGE LIST");
            if (Main.primaryStage.getUserData() instanceof Boolean) {

                boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                if (isUpdated) {
                    applySorting(null);
                }
            }
        }
    }

    public void addKitUsageBnClick(ActionEvent event) {


        Map<String, Object> map = new HashMap<>();
        map.put("operation_type", OperationType.CREATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlFullDialog("kit/kitUsages/addKitUsage.fxml", "CREATE KIT USAGE");
        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                applySorting(null);
            }
        }
    }
}
