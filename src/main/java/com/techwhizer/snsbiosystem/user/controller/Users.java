package com.techwhizer.snsbiosystem.user.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.constants.KitOperationType;
import com.techwhizer.snsbiosystem.report.DownloadReport;
import com.techwhizer.snsbiosystem.user.constant.UserRole;
import com.techwhizer.snsbiosystem.user.constant.UserSearchFilters;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.PageResponse;
import com.techwhizer.snsbiosystem.user.model.UserDTO;
import com.techwhizer.snsbiosystem.util.LocalDb;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.RowPerPage;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    public ComboBox<String> filterByCom;
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
        setComboBoxData();


    }

    public void startThread(String role, OperationType operationType, Long clientId, Button button, Map<String, Object> map) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(role, operationType, clientId, button, map);
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
                startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null, null);
            }
        }
    }

    public void uploadDistributorBnClick(ActionEvent event) {
        customDialog.showFxmlFullDialog("profile/previewProfile.fxml", "PREVIEW PROFILE");
        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null, null);

            }
        }
    }

    public void refreshClick(MouseEvent mouseEvent) {
        startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null, null);
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String role;
        private OperationType operationType;
        private Long clientId;
        private Button button;
        private Map<String, Object> map;
        private Button downloadButton;

        public MyAsyncTask(String role, OperationType operationType,
                           Long clientId, Button button, Map<String, Object> map) {
            this.role = role;
            this.operationType = operationType;
            this.clientId = clientId;
            this.button = button;
            this.map = map;
            if (null != map) {
                downloadButton = (Button) map.get("button");
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
            } else if (operationType == OperationType.DOWNLOAD_REPORT) {

                if (null != map) {

                    if (null != downloadButton) {
                        ProgressIndicator pi = method.getProgressBar(25, 25);
                        pi.setStyle("-fx-progress-color: white;-fx-border-width: 2");
                        downloadButton.setGraphic(pi);
                    }
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
        public Boolean doInBackground(String... params) {

            switch (operationType) {
                case START -> getAllUser(role);
                case DELETE -> deleteUser(clientId, button);
                case DOWNLOAD_REPORT -> {
                    if (null != map) {
                        new DownloadReport().dialogController(map, OperationType.CUSTOMER_REPORT);
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

    private void getAllUser(String role) {
        if (null != userList) {
            userList.clear();
        }

        role = role.replace("ROLE_", "");

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getAllUsersUrl());
            uriBuilder.addParameter("sort", "id,desc");

            if (!role.equalsIgnoreCase(UserRole.ALL)) {
                uriBuilder.setParameter("q[role]", role);
            }

            HttpGet httpGet = new HttpGet(uriBuilder.build());

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
                    pagination.setVisible(true);
                    search_Item();
                }

            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> refreshBn.setDisable(false));
        }

    }

    private void search_Item() {
        searchTf.setText("");

        filteredData = new FilteredList<>(userList, p -> true);

        int rowsPerPage = RowPerPage.USERS_ROW_PER_PAGE;
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

            changeTableView(pagination.getCurrentPageIndex(), rowsPerPage);

        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    tableview.scrollTo(0);
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / RowPerPage.USERS_ROW_PER_PAGE));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSlNum.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableview.getItems().indexOf(cellData.getValue()) + 1));

        colClientId.setCellValueFactory(new PropertyValueFactory<>("clientID"));

        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, userList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<UserDTO> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());
        Platform.runLater(() -> {
            if (sortedData.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("No user found"));
            }
        });

        tableview.setItems(sortedData);
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

    private void setOptionalCell(){

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

                    ImageView activeIc = getImage("img/icon/admin_icon.png");
                    activeIc.setFitWidth(30);
                    activeIc.setFitHeight(30);

                    editBn.setGraphic(getImage("img/icon/update_ic.png"));
                    deleteBbn.setGraphic(getImage("img/icon/delete_ic_white.png"));
                    viewBn.setGraphic(getImage("img/icon/preview_ic.png"));
                    viewKits.setGraphic(getImage("img/menu_icon/kit_ic.png"));
                    downloadBn.setGraphic(new ImageLoader().getDownloadImage());


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
                        Map<String ,Object> map = new HashMap<>();
                        map.put("operation_type", KitOperationType.PREVIEW_INDIVIDUAL_KIT);
                        map.put("user_id",userDTO.getClientID());
                        Main.primaryStage.setUserData(map);
                        customDialog.showFxmlFullDialog("kit/kits.fxml","KIT LIST");
                    });

                    downloadBn.setOnAction((event -> {
                        method.selectTable(getIndex(), tableview);
                        Map<String, Object> map = new HashMap<>();
                        map.put("button", downloadBn);
                        map.put("customer_id", tableview.getItems().get(getIndex()).getClientID());

                        startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.DOWNLOAD_REPORT, 0L, null, map);

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
                                startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null, null);

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

                            startThread(filterByCom.getSelectionModel().getSelectedItem(),
                                    OperationType.DELETE, icm.getClientID(), deleteBbn, null);

                        } else {
                            alert.close();
                        }
                    });

                    HBox managebtn = new HBox(viewKits, downloadBn, viewBn, editBn, (signedUsername.equals(currentUsername) ? activeIc : deleteBbn));
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
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setGraphic(text);

                        } else {
                            setText("-");
                        }
                    } else {
                        setText("-");
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
                            Text text = new Text(user.getOfficeAddress());
                            text.setStyle("-fx-text-alignment:justify;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setGraphic(text);

                        } else {
                            setText("-");
                        }

                    } else {
                        setText("-");
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
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                            setGraphic(text);
                        } else {
                            setText("-");
                        }
                    } else {
                        setText("-");
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
                            setText("-");
                        }

                    } else {
                        setText("-");
                    }
                }
            }

        });
    }

    @SafeVarargs
    private void comboboxConfig(ComboBox<String>... comboBox) {

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
    }

    private void setComboBoxData() {
        filterByCom.valueProperty().addListener((observableValue, s, newValue) -> startThread(newValue, OperationType.START, 0L, null, null));
        searchByCom.valueProperty().addListener((observableValue, s, newValue) -> {
            searchTf.setPromptText(" Search By : " + newValue.toLowerCase());
        });

        filterByCom.setItems(localDb.getFilterType());
        filterByCom.getSelectionModel().selectFirst();

        searchByCom.setItems(localDb.getUserSearchType());
        searchByCom.getSelectionModel().selectFirst();

        comboboxConfig(filterByCom);
        comboboxConfig(sortByCom);
        comboboxConfig(searchByCom);
    }

    private void deleteUser(Long id, Button button) {
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
                    startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null, null);
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
