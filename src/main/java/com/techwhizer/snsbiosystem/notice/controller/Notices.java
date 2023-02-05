package com.techwhizer.snsbiosystem.notice.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.notice.model.NoticeBoardDTO;
import com.techwhizer.snsbiosystem.notice.model.NoticePageResponse;
import com.techwhizer.snsbiosystem.pagination.PaginationUtil;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class Notices implements Initializable {
    public ComboBox<String> searchByCom;
    public TextField searchTf;
    public TableView<NoticeBoardDTO> tableview;
    public TableColumn<NoticeBoardDTO, String> colPublishDate;
    public TableColumn<NoticeBoardDTO, String> colExpiryDate;
    public TableColumn<NoticeBoardDTO, String> colFor;
    public TableColumn<NoticeBoardDTO, String> colScheduled;
    public TableColumn<NoticeBoardDTO, String> colStatus;
    public TableColumn<NoticeBoardDTO, String> colMessage;
    public TableColumn<NoticeBoardDTO, String> colAction;
    public Pagination pagination;
    public ImageView refreshBn;
    public ComboBox<Integer> rowSizeCom;
    public HBox paginationContainer;
    private CustomDialog customDialog;
    private OptionalMethod method;
    private ObservableList<NoticeBoardDTO> noticeList = FXCollections.observableArrayList();
    private FilteredList<NoticeBoardDTO> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customDialog = new CustomDialog();
        method = new OptionalMethod();
        comboBoxConfig();
    }

    private void comboBoxConfig() {
        rowSizeCom.setItems(PaginationUtil.rowSize);

        rowSizeCom.valueProperty().addListener((observableValue, integer, rowPerPage) ->
                callThread(OperationType.START, 0L, null, rowPerPage));

        rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE);
    }

    private void callThread(OperationType operationType, Long noticeId, Button button, Integer rowPerPage) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(operationType, noticeId, button, rowPerPage);
        myAsyncTask.execute();
    }
    private class MyAsyncTask extends AsyncTask<Object, Integer, Boolean> {
        private OperationType operationType;
        private Long noticeId;
        private Button button;
        private int rowPerPage;

        public MyAsyncTask(OperationType operationType, Long noticeId, Button button, Integer rowPerPage) {
            this.operationType = operationType;
            this.noticeId = noticeId;
            this.button = button;
            this.rowPerPage = rowPerPage;
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
                case START -> getAllNotice(rowPerPage);
                case DELETE -> deleteNotice(noticeId, button);
            }
            return true;
        }

        private void deleteNotice(Long noticeId, Button button) {
            try {
                Thread.sleep(100);
                HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("easy").build()).build();

                HttpDelete httpMethod = new HttpDelete(UrlConfig.getKitNoticeUrl().concat("/").concat(String.valueOf(noticeId)));
                httpMethod.addHeader("Content-Type", "application/json");
                httpMethod.addHeader("Cookie", (String) Login.authInfo.get("token"));
                HttpResponse response = httpClient.execute(httpMethod);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {
                    String content = EntityUtils.toString(resEntity);

                    int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode == 200) {
                        callThread(OperationType.START, 0L, null, rowSizeCom.getSelectionModel().getSelectedItem().intValue());
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

        @Override
        public void onPostExecute(Boolean success) {
            refreshBn.setDisable(false);
            if (null != button) {
                button.setGraphic(getImage("img/icon/delete_ic_white.png"));
            }
            tableview.setPlaceholder(new Label("Notice not found"));

            if (noticeList.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("Notice not found"));
            }
        }

        @Override
        public void progressCallback(Integer... params) {
        }

    }
    private void getAllNotice(int rowPerPage) {

        if (null != noticeList) {
            noticeList.clear();
        }
        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getKitNoticeUrl());
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                NoticePageResponse pageResponse = new Gson().fromJson(content, NoticePageResponse.class);
                List<NoticeBoardDTO> noticeBoardDTOs = pageResponse.getNotices();
                noticeList = FXCollections.observableArrayList(noticeBoardDTOs);

                if (noticeList.size() > 0) {
                    paginationContainer.setVisible(true);

                    search_Item(rowPerPage);
                }
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(() -> {
            refreshBn.setDisable(false);
        });

    }
    private void search_Item(int rowPerPage) {
        searchTf.setText("");

        filteredData = new FilteredList<>(noticeList, p -> true);

        searchTf.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(notice -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String searchBy = searchByCom.getSelectionModel().getSelectedItem();

                return false;

               /* switch (searchBy) {

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
                }*/
            });

            changeTableView(pagination.getCurrentPageIndex(), rowPerPage);

        });

        pagination.setCurrentPageIndex(0);
        changeTableView(0, rowPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    tableview.scrollTo(0);
                    changeTableView(newValue1.intValue(), rowPerPage);
                });
    }

    private void changeTableView(int index, int rowPerPage) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / rowPerPage));
        Platform.runLater(() -> pagination.setPageCount(totalPage));
        setOptionalCell();
        int fromIndex = index * rowPerPage;
        int toIndex = Math.min(fromIndex + rowPerPage, noticeList.size());
        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<NoticeBoardDTO> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());
        Platform.runLater(() -> {
            if (sortedData.size() > 0) {
                tableview.setPlaceholder(method.getProgressBar(40, 40));
            } else {
                tableview.setPlaceholder(new Label("Notice not found"));
            }
        });
        tableview.setItems(sortedData);
        tableview.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(NoticeBoardDTO item, boolean empty) {
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

        colAction.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
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

                    CommonUtility.onHoverShowTextButton(editBn,"Update notice");
                    CommonUtility.onHoverShowTextButton(deleteBbn,"Delete notice");
                    CommonUtility.onHoverShowTextButton(viewBn,"View notice");

                    editBn.setGraphic(getImage("img/icon/update_ic.png"));
                    deleteBbn.setGraphic(getImage("img/icon/delete_ic_white.png"));
                    viewBn.setGraphic(getImage("img/icon/preview_ic.png"));


                    editBn.setStyle("-fx-cursor: hand ; -fx-background-color: #06a5c1 ; -fx-background-radius: 3 ");
                    viewBn.setStyle("-fx-cursor: hand ; -fx-background-color: #04505e ; -fx-background-radius: 3 ");
                    deleteBbn.setStyle("-fx-cursor: hand ; -fx-background-color: red ; -fx-background-radius: 3 ");

                    viewBn.setOnAction(event -> {
                        method.selectTable(getIndex(), tableview);
                        NoticeBoardDTO noticeBoardDTO = tableview.getSelectionModel().getSelectedItem();
                        Main.primaryStage.setUserData(noticeBoardDTO);
                        customDialog.showFxmlDialog2("notice/viewNotice.fxml", "");
                    });
                    editBn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        NoticeBoardDTO noticeBoardDTO = tableview.getSelectionModel().getSelectedItem();
                        Map<String, Object> map = new HashMap<>();
                        map.put("operation_type", OperationType.UPDATE);
                        map.put("notice_date", noticeBoardDTO);
                        Main.primaryStage.setUserData(map);
                        customDialog.showFxmlFullDialog("notice/createNotice.fxml", "UPDATE NOTICE");
                        if (Main.primaryStage.getUserData() instanceof Boolean) {
                            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
                            if (isUpdated) {
                                callThread(OperationType.START, 0L, null, rowSizeCom.getSelectionModel().getSelectedItem().intValue());

                            }
                        }

                    });
                    deleteBbn.setOnAction((event) -> {
                        method.selectTable(getIndex(), tableview);
                        NoticeBoardDTO noticeBoardDTO = tableview.getSelectionModel().getSelectedItem();
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

                            callThread(OperationType.DELETE, noticeBoardDTO.getId(), deleteBbn, rowSizeCom.getSelectionModel().getSelectedItem().intValue());

                        } else {
                            alert.close();
                        }
                    });

                    HBox managebtn = new HBox(viewBn, editBn, deleteBbn);
                    managebtn.setStyle("-fx-alignment:center");

                    managebtn.setSpacing(15);

                    setGraphic(managebtn);
                    setText(null);

                }
            }
        });
        colPublishDate.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    NoticeBoardDTO noticeBoardDTO = tableview.getItems().get(getIndex());
                    if (null != String.valueOf(noticeBoardDTO.getPublishOn())) {

                        LocalDateTime localDateTime = CommonUtility.getLocalDateTimeObject(noticeBoardDTO.getPublishOn());
                        String txt = localDateTime.format(CommonUtility.dateTimeFormator);

                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;-fx-font-size: 14");
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
        colExpiryDate.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    NoticeBoardDTO noticeBoardDTO = tableview.getItems().get(getIndex());
                    if (null != String.valueOf(noticeBoardDTO.getExpiresOn())) {
                        LocalDateTime localDateTime = CommonUtility.getLocalDateTimeObject(noticeBoardDTO.getExpiresOn());
                        String txt = localDateTime.format(CommonUtility.dateTimeFormator);
                        if (!txt.isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;-fx-font-size: 14");
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


        colFor.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    NoticeBoardDTO noticeBoardDTO = tableview.getItems().get(getIndex());

                    if (null != noticeBoardDTO) {

                        StringBuilder role = new StringBuilder();

                        if (noticeBoardDTO.isForAdmins()) {
                            role.append(" ").append("ADMIN");
                        }
                        if (noticeBoardDTO.isForDealers()) {
                            role.append(" ").append("DEALER");
                        }

                        if (noticeBoardDTO.isForDoctors()) {
                            role.append(" ").append("DOCTOR");
                        }

                        if (noticeBoardDTO.isForPatients()) {
                            role.append(" ").append("PATIENT");
                        }

                        if (noticeBoardDTO.isForGuests()) {
                            role.append(" ").append("GUEST");
                        }

                        String str = role.toString().replaceAll(" ", ", ");

                        if (str.endsWith(",")) {
                            str = str.substring(0, str.length() - 1) + " ";
                        }

                        Text text = new Text(str.replaceFirst(",", ""));
                        text.setStyle("-fx-text-alignment:center;-fx-font-size: 13");
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                        setText(null);
                        setGraphic(text);
                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colScheduled.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    NoticeBoardDTO noticeBoardDTO = tableview.getItems().get(getIndex());
                    if (null != noticeBoardDTO) {
                        Text text = new Text(noticeBoardDTO.isScheduled() ? "YES" : "NO");
                        text.setStyle("-fx-text-alignment:center;-fx-font-size: 14");
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                        setText(null);
                        setGraphic(text);

                    } else {
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

        colStatus.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    NoticeBoardDTO noticeBoardDTO = tableview.getItems().get(getIndex());
                    if (null != String.valueOf(noticeBoardDTO.getStatus())) {
                        String txt = String.valueOf(noticeBoardDTO.getStatus());
                        if (!String.valueOf(txt).isEmpty()) {
                            Text text = new Text(txt);
                            text.setStyle("-fx-text-alignment:center;-fx-font-size: 14");
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

        colMessage.setCellFactory((TableColumn<NoticeBoardDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    NoticeBoardDTO noticeBoardDTO = tableview.getItems().get(getIndex());
                    if (null != String.valueOf(noticeBoardDTO.getStory())) {
                        String txt = String.valueOf(noticeBoardDTO.getStory());
                        if (!String.valueOf(txt).isEmpty()) {
                            Text text = new Text(CommonUtility.getCutText(txt));
                            text.setStyle("-fx-text-alignment:center;-fx-font-size: 14");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                           if ( text.isHover()){
                            new OptionalMethod().show_popup(txt,text);
                           }

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

    public void addNotice(ActionEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("operation_type", OperationType.CREATE);
        Main.primaryStage.setUserData(map);
        customDialog.showFxmlFullDialog("notice/createNotice.fxml", "CREATE NOTICE");

        if (Main.primaryStage.getUserData() instanceof Boolean) {

            boolean isUpdated = (boolean) Main.primaryStage.getUserData();
            if (isUpdated) {
                refreshClick(null);
            }
        }
    }

    public void refreshClick(MouseEvent mouseEvent) {
        callThread(OperationType.START, 0L, null, rowSizeCom.getSelectionModel().getSelectedItem().intValue());
    }
}
