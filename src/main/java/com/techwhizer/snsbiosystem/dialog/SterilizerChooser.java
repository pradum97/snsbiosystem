package com.techwhizer.snsbiosystem.dialog;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.sterilizer.constants.SterilizerSearchType;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerPageResponse;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerTableView;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.LocalDb;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.RowPerPage;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    public ComboBox<String> searchByCom;
    private int rowsPerPage = 7;
    public TextField searchTf;
    public TableColumn<SterilizerTableView, Integer> colSrNo;
    public TableColumn<SterilizerTableView, String> colSerialId;
    public TableColumn<SterilizerTableView, String> colType;
    public TableColumn<SterilizerTableView, String> colAction;
    public TableView<SterilizerTableView> tableView;
    public Pagination pagination;
    private CustomDialog customDialog;
    private OptionalMethod method;
    private ObservableList<SterilizerTableView> sterilizerList = FXCollections.observableArrayList();
    private FilteredList<SterilizerTableView> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        searchByCom.setItems(new LocalDb().getSterilizerSearchType());
        searchByCom.getSelectionModel().selectFirst();
        callThread();
    }

    private void callThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String msg;

        @Override
        public void onPreExecute() {
            tableView.setPlaceholder(method.getProgressBar(30, 30));
            msg = "";
        }

        @Override
        public Boolean doInBackground(String... params) {
            getAllSterilizer();
            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            tableView.setPlaceholder(new Label(msg));
        }
        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getAllSterilizer() {

        if (null != sterilizerList) {
            sterilizerList.clear();
        }
        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getAddSterilizerUrl());
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                String content = EntityUtils.toString(resEntity);

                if (statusCode == 200){
                    SterilizerPageResponse pageResponse = new Gson().fromJson(content, SterilizerPageResponse.class);
                    List<SterilizerTableView> stvs = pageResponse.getSterilizers();
                    sterilizerList = FXCollections.observableArrayList(stvs);

                    System.out.println(sterilizerList.size());

                    if (sterilizerList.size() > 0) {
                        pagination.setVisible(true);
                        search_Item();
                    }
                } else if (statusCode == 401) {
                    new HttpStatusHandler(401);
                }else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                }

            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void search_Item() {
        searchTf.setText("");

        filteredData = new FilteredList<>(sterilizerList, p -> true);

        int rowsPerPage = RowPerPage.STERILIZERS_ROW_PER_PAGE;
        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(sterilizer -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String searchBy = searchByCom.getSelectionModel().getSelectedItem();

                switch (searchBy) {

                    case SterilizerSearchType.STERILIZER_ID -> {

                        return null != sterilizer.getId() &&
                                String.valueOf(sterilizer.getId()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case SterilizerSearchType.STERILIZER_BRAND -> {
                        return null != sterilizer.getBrand() &&
                                String.valueOf(sterilizer.getBrand()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }
                    case SterilizerSearchType.STERILIZER_SERIAL_NUMBER -> {
                        return null != sterilizer.getSerialNumber() &&
                                String.valueOf(sterilizer.getSerialNumber()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }

                    case SterilizerSearchType.STERILIZER_LIST_NUMBER -> {
                        sterilizer.getListNumber();
                        return String.valueOf(sterilizer.getListNumber()).toLowerCase().equalsIgnoreCase(lowerCaseFilter);
                    }

                    default -> {

                        return null != sterilizer.getType() &&
                                sterilizer.getType().toLowerCase().contains(lowerCaseFilter);
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

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 /rowsPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        colSrNo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                tableView.getItems().indexOf(cellData.getValue()) + 1));

        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, sterilizerList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<SterilizerTableView> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        Platform.runLater(()->{
            if (sortedData.size() > 0) {
                tableView.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableView.setPlaceholder(new Label("Sterilizer not found"));
            }
        });

        tableView.setItems(sortedData);

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
                            Stage stage = (Stage) searchTf.getScene().getWindow();
                            if (null != stage && stage.isShowing()) {
                                stage.close();
                            }
                        }
                    });
                    HBox managebtn = new HBox(selectBn);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(selectBn, new Insets(0, 0, 0, 0));

                    setGraphic(managebtn);

                    setText(null);

                }
            }

        });
    }
}
