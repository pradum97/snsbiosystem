package com.techwhizer.snsbiosystem.controller.dashboard;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.controller.auth.Login;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.dialog.Toast;
import com.techwhizer.snsbiosystem.model.PageResponse;
import com.techwhizer.snsbiosystem.model.UserDTO;
import com.techwhizer.snsbiosystem.util.LocalDb;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.UrlConfig;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.stage.FileChooser;
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

import java.io.File;
import java.net.URL;
import java.util.*;

public class Users implements Initializable {
    private int rowsPerPage = 25;
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
        pagination.setVisible(false);
        setComboBoxData();


    }

    public void startThread(String role, OperationType operationType, Long clientId, Button button) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(role, operationType, clientId,button);
        myAsyncTask.setDaemon(true);
        myAsyncTask.execute();
    }

    public void addUser(ActionEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("operation_type",OperationType.CREATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlFullDialog("profile/createProfile.fxml", "CREATE PROFILE");
    }

    public void uploadDistributorBnClick(ActionEvent event) {

        String path = "D:\\sns\\Sorted Data\\distributorSortData.csv";

        /*FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("SELECT EXCEL FILE");
        File selectedFile = fileChooser.showOpenDialog(Main.primaryStage);

        if (null != selectedFile) {
            Main.primaryStage.setUserData(selectedFile.getAbsolutePath());
            customDialog.showFxmlFullDialog("profile/previewProfile.fxml", "PREVIEW PROFILE");
        }*/

        Main.primaryStage.setUserData(path);
        customDialog.showFxmlFullDialog("profile/previewProfile.fxml", "PREVIEW PROFILE");
    }

    public void refreshClick(MouseEvent mouseEvent) {
        startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null);
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String role;
        private OperationType operationType;
        private Long clientId;
        private Button button;

        public MyAsyncTask(String role, OperationType operationType,
                           Long clientId, Button button) {
            this.role = role;
            this.operationType = operationType;
            this.clientId = clientId;
            this.button = button;
        }

        @Override
        public void onPreExecute() {
            if (tableview.getItems().isEmpty()) {
                tableview.getItems().clear();
                tableview.refresh();
            }
            if (operationType == OperationType.DELETE) {

                if (null != button){
                    ProgressIndicator pi = method.getProgressBar(25, 25);
                    pi.setStyle("-fx-progress-color: white;-fx-border-width: 2");
                    button.setGraphic(pi);
                    Toast.makeText("Please wait..", 1500, "#006666");
                }
            }

            assert tableview != null;
            tableview.setPlaceholder(method.getProgressBar(40, 40));
        }

        @Override
        public Boolean doInBackground(String... params) {

            switch (operationType) {
                case START -> getAllUser(role);
                case DELETE -> deleteUser(clientId,button);
            }

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {

            tableview.setPlaceholder(new Label("No user found"));
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

            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getAllUsersUrl());

            if (!role.equalsIgnoreCase("all")) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void search_Item() {
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

                    case "CLIENT ID" -> {

                        return null != user.getClientID() &&
                                String.valueOf(user.getClientID()).toLowerCase().contains(lowerCaseFilter);
                    }
                    case "NAME" -> {
                        return (null != user.getFirstName() || null != user.getLastName()) &&
                                (user.getFirstName() + user.getLastName()).toLowerCase().contains(lowerCaseFilter);
                    }
                    case "EMAIL" -> {
                        return null != user.getWorkEmail() &&
                                String.valueOf(user.getWorkEmail()).toLowerCase().contains(lowerCaseFilter);
                    }
                    case "PHONE" -> {
                        return null != user.getWorkPhoneNumber() &&
                                String.valueOf(user.getWorkPhoneNumber()).toLowerCase().contains(lowerCaseFilter);
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
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowsPerPage));
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

        tableview.setItems(sortedData);


        tableview.setRowFactory(tv -> new TableRow<UserDTO>() {
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

                    ImageView activeIc = getImage("img/icon/admin_icon.png");
                    activeIc.setFitWidth(30);
                    activeIc.setFitHeight(30);


                    editBn.setGraphic(getImage("img/icon/update_ic.png"));
                    deleteBbn.setGraphic(getImage("img/icon/delete_ic_white.png"));
                    viewBn.setGraphic(getImage("img/icon/preview_ic.png"));


                    String signedUsername = (String) Login.authInfo.get("username");
                    String currentUsername = tableview.getItems().get(getIndex()).getRequestedLoginName();

                    if (signedUsername.equals(currentUsername)) {
                        deleteBbn.setVisible(false);
                    }

                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");
                    viewBn.setStyle("-fx-cursor: hand ; -fx-background-color: #04505e ; -fx-background-radius: 3 ");
                    deleteBbn.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3 ");

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
                        map.put("operation_type",OperationType.UPDATE);
                        map.put("client_id",icm.getClientID());
                        Main.primaryStage.setUserData(map);
                        customDialog.showFxmlFullDialog("profile/createProfile.fxml", "UPDATE PROFILE");

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
                                    OperationType.DELETE, icm.getClientID(),deleteBbn);

                        } else {
                            alert.close();
                        }
                    });

                    HBox managebtn = new HBox(viewBn, editBn, (signedUsername.equals(currentUsername) ? activeIc : deleteBbn));
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(editBn, new Insets(0, 30, 0, 30));

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
        filterByCom.valueProperty().addListener((observableValue, s, newValue) -> startThread(newValue, OperationType.START, 0L, null));
        searchByCom.valueProperty().addListener((observableValue, s, newValue) -> {
            searchTf.setPromptText(" Search By : " + newValue.toLowerCase());
        });

        filterByCom.setItems(localDb.getFilterType());
        filterByCom.getSelectionModel().selectFirst();
        sortByCom.setItems(localDb.getSortingType());
        sortByCom.getSelectionModel().selectFirst();

        searchByCom.setItems(localDb.getUserSearchType());
        searchByCom.getSelectionModel().selectFirst();

        comboboxConfig(filterByCom);
        comboboxConfig(sortByCom);
        comboboxConfig(searchByCom);
    }

    private void deleteUser(Long id, Button button) {
        try {

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
                    startThread(filterByCom.getSelectionModel().getSelectedItem(), OperationType.START, 0L, null);
                }

            }

            if (null != button){
                Platform.runLater(()->button.setGraphic(getImage("img/icon/delete_ic_white.png")));
            }
        } catch (Exception e) {
            if (null != button){
                button.setGraphic(getImage("img/icon/delete_ic_white.png"));
            }
            throw new RuntimeException(e);
        }
    }
}
