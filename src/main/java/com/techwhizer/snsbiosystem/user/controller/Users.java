package com.techwhizer.snsbiosystem.user.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.constants.KitOperationType;
import com.techwhizer.snsbiosystem.pagination.PaginationUtil;
import com.techwhizer.snsbiosystem.report.DownloadReport;
import com.techwhizer.snsbiosystem.user.constant.RoleOption;
import com.techwhizer.snsbiosystem.user.constant.UserSearchFilters;
import com.techwhizer.snsbiosystem.user.constant.UserSortingOption;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.PageResponse;
import com.techwhizer.snsbiosystem.user.model.UserDTO;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Callback;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class Users implements Initializable {
    public ImageView refreshBn;
    public ComboBox<String> filterByRoleCom;
    public ComboBox<String> sortByCom;
    public TextField searchTf;
    public TableView<UserDTO> tableview;
    public TableColumn<UserDTO, Integer> colSlNum;
    public TableColumn<UserDTO, String> colName;
    public TableColumn<UserDTO, String> colClientId;
    public TableColumn<UserDTO, String> colOfficeAddress;
    public TableColumn<UserDTO, String> colEmail;
    public TableColumn<UserDTO, String> colPhone;
    public TableColumn<UserDTO, String> colAction;
    public ComboBox<Integer> rowSizeCom;
    public ComboBox<String> sortingCom;
    public ComboBox<String> orderCom;
    public HBox paginationContainer;
    public Button applySorting;
    private CustomDialog customDialog;
    private OptionalMethod method;
    private LocalDb localDb;
    public Pagination pagination;
    public ComboBox<String> searchByCom;
    private ObservableList<UserDTO> userList = FXCollections.observableArrayList();
    private FilteredList<UserDTO> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new OptionalMethod();
        localDb = new LocalDb();
        searchByCom.valueProperty().addListener((observableValue, s, t1) -> searchTf.setText(""));

        startThread(null, OperationType.SORTING_LOADING, 0L, null, null, null);
    }

    private void comboBoxConfig() {
        pagination.setCurrentPageIndex(0);
        rowSizeCom.setItems(PaginationUtil.rowSize);
        sortingCom.setItems(FXCollections.observableArrayList(UserSortingOption.sortingMap.keySet()));

        ObservableList<String> roleList = FXCollections.observableArrayList(CommonUtility.ALL);
        roleList.addAll(FXCollections.observableArrayList(RoleOption.sortingMap.keySet()));

        filterByRoleCom.setItems(roleList);
        orderCom.setItems(CommonUtility.orderList);

        Platform.runLater(() -> {

            rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE);
            orderCom.getSelectionModel().select(CommonUtility.ORDER_ASC);
            sortingCom.getSelectionModel().selectFirst();


            pagination.currentPageIndexProperty().addListener(
                    (observable1, oldValue1, newValue1) -> {
                        int pageIndex = newValue1.intValue();
                        String role = filterByRoleCom.getSelectionModel().getSelectedItem();
                        sortData(role, OperationType.START, 0L, null, null, pageIndex, 0);


                    });

            rowSizeCom.valueProperty().addListener((observableValue, integer, rowPerPage) -> {
                String role = filterByRoleCom.getSelectionModel().getSelectedItem();
                sortData(role, OperationType.START, 0L, null, null, pagination.getCurrentPageIndex(), 0);
            });

            filterByRoleCom.valueProperty().addListener((observableValue, s, t1) ->
                    sortData(t1, OperationType.START, 0L, null, null, pagination.getCurrentPageIndex(), 0));
            applySorting.setDisable(false);
            searchByCom.valueProperty().addListener((observableValue, s, newValue) -> {
                searchTf.setPromptText(" Search By : " + newValue.toLowerCase());
            });

            searchByCom.setItems(localDb.getUserSearchType());
            filterByRoleCom.getSelectionModel().select(CommonUtility.ALL);
            searchByCom.getSelectionModel().selectFirst();

            comboboxSetting(filterByRoleCom);
            comboboxSetting(sortByCom);
            comboboxSetting(searchByCom);
        });
    }

    public void applySorting(ActionEvent event) {
        String role = filterByRoleCom.getSelectionModel().getSelectedItem().toLowerCase();
        sortData(role, OperationType.START, 0L, null, null, pagination.getCurrentPageIndex(), 0);
    }

    private void sortData(String role, OperationType operationType, Long clientId, Button button,
                          Map<String, Object> reportMap, int pageIndex, int rowIndex) {

        String filedName = UserSortingOption.getKeyValue(sortingCom.getSelectionModel().getSelectedItem());
        String order = CommonUtility.parserOrder(orderCom.getSelectionModel().getSelectedItem());
        int rowSize = rowSizeCom.getSelectionModel().getSelectedItem();
        String sort = filedName + "," + order;

        Map<String, Object> sortedDataMap = new HashMap<>();
        sortedDataMap.put("sort", sort);
        sortedDataMap.put("row_size", rowSize);
        sortedDataMap.put("page_index", pageIndex);
        sortedDataMap.put("row_index", rowIndex);

        startThread(role, operationType, clientId, button, reportMap, sortedDataMap);
    }

    public void startThread(String role, OperationType operationType,
                            Long clientId, Button button, Map<String, Object> reportMap,
                            Map<String, Object> sortedDataMap) {

        MyAsyncTask myAsyncTask = new MyAsyncTask(role, operationType, clientId, button, reportMap, sortedDataMap);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    public void addUser(ActionEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("operation_type", OperationType.CREATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlFullDialog("profile/createProfile.fxml", "CREATE PROFILE");

        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                applySorting(null);
            }
        }
    }

    public void uploadDistributorBnClick(ActionEvent event) {
        customDialog.showFxmlFullDialog("profile/previewProfile.fxml", "PREVIEW PROFILE");
        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                applySorting(null);
            }
        }
    }

    public void refreshClick(MouseEvent mouseEvent) {

        applySorting(null);
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String role;
        private OperationType operationType;
        private Long clientId;
        private Button button;
        private Map<String, Object> reportMap;
        private Button downloadButton;
        private Map<String, Object> sortedDataMap;

        public MyAsyncTask(String role, OperationType operationType,
                           Long clientId, Button button, Map<String, Object> reportMap, Map<String, Object> sortedDataMap) {
            this.role = role;
            this.operationType = operationType;
            this.clientId = clientId;
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
        public Boolean doInBackground(String... params) {

            switch (operationType) {
                case SORTING_LOADING -> comboBoxConfig();
                case START -> getAllUser(role, sortedDataMap);
                case DELETE -> deleteUser(clientId, button, sortedDataMap);
                case DOWNLOAD_REPORT -> {
                    if (null != reportMap) {
                        new DownloadReport().dialogController(reportMap, OperationType.CUSTOMER_REPORT);
                    }
                }
            }

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            refreshBn.setDisable(false);

            if (null != downloadButton) {
                Platform.runLater(() -> {
                    downloadButton.setGraphic(new ImageLoader().getDownloadImage());
                });
            }

            if (userList.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("No user found"));
            }
        }

        @Override
        public void progressCallback(Integer... params) {
        }
    }

    private void getAllUser(String role, Map<String, Object> sortedDataMap) {
        if (null != userList) {
            userList.clear();
        }

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getAllUsersUrl());

            if (null != sortedDataMap) {
                String sort = (String) sortedDataMap.get("sort");
                int rowSize = (Integer) sortedDataMap.get("row_size");
                int pageIndex = (Integer) sortedDataMap.get("page_index");
                param.setParameter("sort", sort);
                param.setParameter("size", String.valueOf(rowSize));
                param.setParameter("page", String.valueOf(pageIndex));
            }

            if (!role.equalsIgnoreCase(CommonUtility.ALL)) {
                param.setParameter("q[role]", role);
            }

            HttpGet httpGet = new HttpGet(param.build());

            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                PageResponse pageResponse = new Gson().fromJson(content, PageResponse.class);
                List<UserDTO> users = pageResponse.getUsers();
                userList = FXCollections.observableArrayList(users);
                if (userList.size() > 0) {
                    paginationContainer.setDisable(false);
                    int totalPage = pageResponse.getTotalPage();
                    search_Item(totalPage, (Integer) sortedDataMap.get("page_index"),
                            (Integer) sortedDataMap.get("row_index"));
                }

            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> refreshBn.setDisable(false));
        }
    }

    private void search_Item(int totalPage, int pageIndex, Integer rowIndex) {
        searchTf.setText("");
        filteredData = new FilteredList<>(userList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(user -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                String searchBy = searchByCom.getSelectionModel().getSelectedItem();
                switch (searchBy) {


                    case UserSearchFilters.CLIENT_ID -> {

                        return null != user.getClientID() &&
                                String.valueOf(user.getClientID()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case UserSearchFilters.NAME -> {
                        return (null != user.getFirstName() || null != user.getLastName()) &&
                                (user.getFirstName() + user.getLastName()).toLowerCase().contains(lowerCaseFilter);
                    }
                    case UserSearchFilters.EMAIL -> {
                        return null != user.getWorkEmail() &&
                                String.valueOf(user.getWorkEmail()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case UserSearchFilters.PHONE -> {
                        return null != user.getWorkPhoneNumber() &&
                                String.valueOf(user.getWorkPhoneNumber()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case UserSearchFilters.USERNAME -> {
                        return null != user.getRequestedLoginName() &&
                                String.valueOf(user.getRequestedLoginName()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    default -> {

                        return (null != user.getOfficeAddress() || null != user.getHomeAddress()) &&
                                (user.getOfficeAddress() + user.getHomeAddress()).toLowerCase().contains(lowerCaseFilter);
                    }
                }
            });

            changeTableView(totalPage, pageIndex, rowIndex);

        });

        changeTableView(totalPage, pageIndex, rowIndex);
    }

    private void changeTableView(int totalPage, int pageIndex, int rowIndex) {

        Platform.runLater(() -> {
                    pagination.setPageCount(totalPage);
                    if (filteredData.size() > 0) {
                        tableview.setPlaceholder(method.getProgressBar(40, 40));
                    } else {
                        tableview.setPlaceholder(new Label("No user found"));
                    }

                    pagination.setCurrentPageIndex(pageIndex);
                    tableview.scrollTo(rowIndex);
                }
        );

        colSlNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));

        colClientId.setCellValueFactory(new PropertyValueFactory<>("clientID"));

        setOptionalCell();
        tableview.setItems(filteredData);
        tableview.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(UserDTO item, boolean empty) {
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

    private ImageView getImage(String path) {

        ImageView iv = new ImageView(new ImageLoader().load(path));
        iv.setFitHeight(17);
        iv.setFitWidth(17);

        return iv;
    }

    private void setOptionalCell() {

        Callback<TableColumn<UserDTO, String>, TableCell<UserDTO, String>>
                cellFactory = (TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {

                    Button editBn = new Button();
                    Button deleteBbn = new Button();
                    Button viewBn = new Button();
                    Button downloadBn = new Button();
                    Button viewKits = new Button();

                    ImageView activeIc = getImage("img/icon/active_ic.png");
                    activeIc.setFitWidth(32);
                    activeIc.setFitHeight(32);

                    editBn.setGraphic(getImage("img/icon/update_ic.png"));
                    deleteBbn.setGraphic(getImage("img/icon/delete_ic_white.png"));
                    viewBn.setGraphic(getImage("img/icon/preview_ic.png"));
                    viewKits.setGraphic(getImage("img/menu_icon/kit_ic.png"));
                    downloadBn.setGraphic(new ImageLoader().getDownloadImage());

                    CommonUtility.onHoverShowTextButton(editBn,"Update user");
                    CommonUtility.onHoverShowTextButton(deleteBbn,"Delete user");
                    CommonUtility.onHoverShowTextButton(viewBn,"View user");
                    CommonUtility.onHoverShowTextButton(viewKits,"View kits");
                    CommonUtility.onHoverShowTextButton(downloadBn,"Download kit report");

                    String signedUsername = (String) Login.authInfo.get("username");
                    String currentUsername = tableview.getItems().get(getIndex()).getRequestedLoginName();

                    if (signedUsername.equals(currentUsername)) {
                        deleteBbn.setVisible(false);
                    }

                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");
                    viewBn.setStyle("-fx-cursor: hand ; -fx-background-color: #04505e ; -fx-background-radius: 3 ");
                    deleteBbn.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3 ");
                    downloadBn.setStyle("-fx-cursor: hand ; -fx-background-color: #051b64 ; -fx-background-radius: 3 ");
                    viewKits.setStyle("-fx-cursor: hand ; -fx-background-color: #014901 ; -fx-background-radius: 3 ");

                    viewKits.setOnAction(event -> {
                        method.selectTable(getIndex(), tableview);
                        UserDTO userDTO = tableview.getSelectionModel().getSelectedItem();
                        Map<String, Object> map = new HashMap<>();
                        map.put("operation_type", KitOperationType.PREVIEW_INDIVIDUAL_KIT);
                        map.put("user_id", userDTO.getClientID());
                        Main.primaryStage.setUserData(map);
                        customDialog.showFxmlFullDialog("kit/kits.fxml", "KIT LIST");
                    });

                    downloadBn.setOnAction((event -> {
                        method.selectTable(getIndex(), tableview);
                        Map<String, Object> map = new HashMap<>();
                        map.put("button", downloadBn);
                        map.put("customer_id", tableview.getItems().get(getIndex()).getClientID());

                        String role = filterByRoleCom.getSelectionModel().getSelectedItem();
                        sortData(role, OperationType.DOWNLOAD_REPORT, 0L, null, map, pagination.getCurrentPageIndex(), getIndex());

                    }));

                    viewBn.setOnAction(event -> {
                        method.selectTable(getIndex(), tableview);
                        UserDTO icm = tableview.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(icm.getClientID());
                        customDialog.showFxmlFullDialog("profile/my_profile.fxml", "Profile");
                    });
                    editBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        UserDTO icm = tableview.getSelectionModel().getSelectedItem();
                        Map<String, Object> map = new HashMap<>();
                        map.put("operation_type", OperationType.UPDATE);
                        map.put("client_id", icm.getClientID());
                        Main.primaryStage.setUserData(map);
                        customDialog.showFxmlFullDialog("profile/createProfile.fxml", "UPDATE PROFILE");

                        if (Main.primaryStage.getUserData() instanceof Boolean) {
                            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                            if (isUpdated) {
                                String role = filterByRoleCom.getSelectionModel().getSelectedItem();
                                sortData(role, OperationType.START, 0L, null, null, pagination.getCurrentPageIndex(), getIndex());
                            }
                        }

                    });
                    deleteBbn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        UserDTO icm = tableview.getSelectionModel().getSelectedItem();
                        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
                        image.setFitWidth(45);
                        image.setFitHeight(45);
                        Alert alert = new Alert(Alert.AlertType.NONE);
                        alert.setAlertType(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Warning ");
                        alert.setGraphic(image);
                        alert.setHeaderText("Are you sure you want to delete this item?");
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.initOwner(Main.primaryStage);
                        Optional<ButtonType> result = alert.showAndWait();
                        ButtonType button = result.orElse(ButtonType.CANCEL);
                        if (button == ButtonType.OK) {

                            String role = filterByRoleCom.getSelectionModel().getSelectedItem();
                            sortData(role, OperationType.DELETE, icm.getClientID(), deleteBbn, null, pagination.getCurrentPageIndex(), getIndex());

                        } else {
                            alert.close();
                        }
                    });

                    HBox managebtn = new HBox(viewKits, downloadBn, viewBn,
                            editBn, (signedUsername.equals(currentUsername) ? activeIc : deleteBbn));
                    managebtn.setStyle("-fx-alignment:center");

                    managebtn.setSpacing(15);

                    //  HBox.setMargin(editBn, new Insets(0, 30, 0, 30));

                    setGraphic(managebtn);
                    setText(null);

                }
            }

        };

        colAction.setCellFactory(cellFactory);
        colName.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());

                    if (null != user.getFirstName() && null != user.getLastName()) {
                        if (!user.getFirstName().isEmpty() && !user.getLastName().isEmpty()) {

                            Text text = new Text(user.getFirstName() + " " + user.getLastName());
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(2));
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

        colOfficeAddress.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());

                    if (null != user.getOfficeAddress()) {
                        if (!user.getOfficeAddress().isEmpty()) {
                            Text text = new Text(CommonUtility.getCutText(user.getOfficeAddress()));
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(2));
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

        colEmail.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getWorkEmail()) {
                        if (!user.getWorkEmail().isEmpty()) {
                            Text text = new Text(user.getWorkEmail());
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(2));
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

        colPhone.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getWorkPhoneNumber()) {
                        if (!user.getWorkPhoneNumber().isEmpty()) {
                            setText(user.getWorkPhoneNumber());
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

    @SafeVarargs
    private void comboboxSetting(ComboBox<String>... comboBox) {

        Platform.runLater(() -> {

            for (ComboBox<String> com : comboBox) {
                com.setButtonCell(new ListCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                            setAlignment(Pos.CENTER);
                            Insets old = getPadding();
                            setPadding(new Insets(old.getTop(), 0, old.getBottom(), 0));
                        }
                    }
                });
            }
        });
    }

    private void deleteUser(Long id, Button button, Map<String, Object> sortedDataMap) {
        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpDelete httpMethod = new HttpDelete(UrlConfig.getUserDeleteUrl().concat(String.valueOf(id)));
            httpMethod.addHeader("Content-Type", "application/json");
            httpMethod.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpMethod);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                int statusCode = response.getStatusLine().getStatusCode();
                customDialog.showAlertBox("", content);

                if (statusCode == 200) {

                    int pageIndex = (Integer) sortedDataMap.get("page_index");
                    int rowIndex = (Integer) sortedDataMap.get("row_index");
                    String role = filterByRoleCom.getSelectionModel().getSelectedItem().toLowerCase();
                    sortData(role, OperationType.START, 0L, null, null, pageIndex, rowIndex);

                    applySorting(null);
                }

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
}
