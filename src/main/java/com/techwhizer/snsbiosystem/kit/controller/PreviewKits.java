package com.techwhizer.snsbiosystem.kit.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.kit.model.AddKitResponse;
import com.techwhizer.snsbiosystem.kit.model.KitDTO;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.RowPerPage;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class PreviewKits implements Initializable {
    public Pagination pagination;
    public Label totalRecordL;
    public Label totalValidRecordL;
    public Label totalInvalidRecordL;
    public Label uploadNowBn;
    public ProgressIndicator progressbar;
    public TableView<KitDTO> tableview;
    public TableColumn<KitDTO, String> colClientId;
    public TableColumn<KitDTO, String> colStatus;
    public TableColumn<KitDTO, String> colDealerId;
    public TableColumn<KitDTO, String> colKitNumber;
    public TableColumn<KitDTO, String> colExpiryDate;
    public TableColumn<KitDTO, String> colLotNumber;
    public TableColumn<KitDTO, String> colTestUsed;
    public HBox paginationContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private File file;
    private int statusCode;

    private ObservableList<KitDTO> kitsList = FXCollections.observableArrayList();
    private FilteredList<KitDTO> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        method.hideElement(progressbar);
        Platform.runLater(()->{
            OptionalMethod.minimizedStage((Stage) uploadNowBn.getScene().getWindow(),true);
        });
        if (null != Main.primaryStage.getUserData() &&
                Main.primaryStage.getUserData() instanceof File) {
            file = (File) Main.primaryStage.getUserData();

            callThread(OperationType.PREVIEW, "Please wait Data is being verified.", null);

            Platform.runLater(() -> {
                Stage stage = (Stage) uploadNowBn.getScene().getWindow();
                if (null != stage) {
                    stage.setTitle(file.getAbsolutePath());
                }

            });
        }

    }

    private void callThread(OperationType operationType, String loadingMsg, String json) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(operationType, loadingMsg, json);
        myAsyncTask.execute();
    }

    public void cancelBnClick(MouseEvent event) {
        Stage stage = (Stage) uploadNowBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void uploadBnClick(MouseEvent event) {

        if (kitsList.size() < 1) {
            customDialog.showAlertBox("", "Record not found");
            return;
        }
        long totRec = Long.parseLong(totalRecordL.getText());
        long totInvalid = Long.parseLong(totalInvalidRecordL.getText());

        if (totRec == totInvalid) {
            customDialog.showAlertBox("", "All records are invalid. Please select a valid file.");
            return;
        }

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure, you want to upload the file?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            String json = new Gson().toJson(kitsList);
            callThread(OperationType.CREATE, "Please wait...", json);
        } else {
            alert.close();
        }
    }

    private void createMultipleKIts(String json) {

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UrlConfig.getGetKitsUrl());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cookie", (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                AddKitResponse asr = new Gson().fromJson(content, AddKitResponse.class);
                List<KitDTO> failed = asr.getInvalidKits();
                List<KitDTO> added = asr.getAdded();

                int addedCount = added.size();
                int failedCount = failed.size();

                tableview.setItems(null);
                tableview.refresh();

                Platform.runLater(() -> {
                    if (statusCode == 200) {

                        if (failed.size() > 0) {

                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Total Added : ").append(addedCount).append("\n");
                            stringBuilder.append("Total Failed : ").append(failedCount).append("\n\n");
                            stringBuilder.append("Kit Number").append("\n");
                            for (KitDTO kitDTO : failed) {
                                stringBuilder.append(" ").append(null == kitDTO.getKitNumber() ? CommonUtility.EMPTY_LABEL_FOR_TABLE : kitDTO.getKitNumber()).append("  -   ").
                                        append(null == kitDTO.getErrorMessage() ? "Invalid Data" : kitDTO.getErrorMessage()).append("\n");
                            }
                            Main.primaryStage.setUserData(true);
                            cancelBnClick(null);
                            customDialog.showFullAlertBox("Success", String.valueOf(stringBuilder));
                        } else {
                            Main.primaryStage.setUserData(true);
                            cancelBnClick(null);
                            customDialog.showAlertBox("Success", "Kits successfully added");

                        }

                        method.hideElement(progressbar);
                        uploadNowBn.setVisible(true);

                    } else {
                        customDialog.showAlertBox("Failed.", "Something went wrong. Please try again");
                    }
                });
            }

        } catch (Exception e) {
            method.hideElement(progressbar);
            uploadNowBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    public void keyPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER){
            uploadBnClick(null);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        OperationType operationType;
        String loadingMsg, json;

        public MyAsyncTask(OperationType operationType, String loadingMsg, String json) {
            this.operationType = operationType;
            this.loadingMsg = loadingMsg;
            this.json = json;
        }

        @Override
        public void onPreExecute() {
            if (operationType != OperationType.CREATE) {
                if (!tableview.getItems().isEmpty()) {
                    tableview.getItems().clear();
                    tableview.refresh();
                }

                ProgressIndicator pi = (method.getProgressBar(45, 45));
                pi.setStyle("-fx-progress-color: red");
                Label label = new Label(loadingMsg);

                VBox box = new VBox(pi, label);
                VBox.setMargin(label, new Insets(10, 0, 0, 0));
                box.setStyle("-fx-alignment: center");
                tableview.setPlaceholder(box);
            } else {
                method.hideElement(uploadNowBn);
                progressbar.setVisible(true);
            }
        }

        @Override
        public Boolean doInBackground(String... params) {

            if (operationType == OperationType.CREATE) {
                createMultipleKIts(json);
            } else if (operationType == OperationType.PREVIEW) {
                sendRequest();
            }
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {

            if (operationType == OperationType.CREATE) {
                method.hideElement(progressbar);
                uploadNowBn.setVisible(true);
            } else {

                if (statusCode == 500) {
                    tableview.setPlaceholder(new Label("CSV File not valid. Please select valid CSV file."));
                } else {
                    tableview.setPlaceholder(new Label("User Not founded"));
                }


            }
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void sendRequest() {

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UrlConfig.getPreviewKitsUrl());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(file);
            builder.addPart("file", fileBody);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            statusCode = response.getStatusLine().getStatusCode();

            if (resEntity != null && response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(resEntity);
                Type type = new TypeToken<Set<KitDTO>>() {
                }.getType();
                Set<KitDTO> kitsArray = new Gson().fromJson(content, type);
                kitsList = FXCollections.observableArrayList(kitsArray);
                long totalRecord = 0, totalInvalid = 0, totalValid = 0;

                totalRecord = kitsArray.size();
                for (KitDTO u : kitsArray) {
                    if (u.isValid()) {
                        totalValid += 1;
                    } else {
                        totalInvalid += 1;
                    }
                }

                long finalTotalRecord = totalRecord;
                long finalTotalInvalid = totalInvalid;
                long finalTotalValid = totalValid;
                Platform.runLater(() -> {
                    totalRecordL.setText(String.valueOf(finalTotalRecord));
                    totalValidRecordL.setText(String.valueOf(finalTotalValid));
                    totalInvalidRecordL.setText(String.valueOf(finalTotalInvalid));
                });

                if (kitsList.size() > 0) {
                    paginationContainer.setDisable(false);
                    search_Item();
                }
            } else {
                customDialog.showAlertBox("Failed", "Something went wrong!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void search_Item() {
        filteredData = new FilteredList<>(kitsList, p -> true);
        pagination.setCurrentPageIndex(0);
        int rowsPerPage = RowPerPage.PREVIEW_KITS_ROW_PER_PAGE;
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    tableview.scrollTo(0);
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }

    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / RowPerPage.PREVIEW_KITS_ROW_PER_PAGE));

        Platform.runLater(() -> pagination.setPageCount(totalPage));

        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, kitsList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<KitDTO> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());



        tableview.setItems(sortedData);
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
        colStatus.setCellFactory((TableColumn<KitDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    KitDTO user = tableview.getItems().get(getIndex());
                    boolean isValid = user.isValid();
                    String txt = isValid ? "VALID" : "INVALID";
                    Button button = new Button(txt);
                    button.setMinWidth(70);
                    button.setMaxHeight(10);
                    String bgColor = "";

                    if (isValid) {
                        bgColor = "green";
                    } else {
                        bgColor = "red";
                    }

                    String style = "-fx-cursor: hand ; -fx-background-color:" + bgColor + ";" +
                            "-fx-text-fill: white; -fx-background-radius: 3 ";

                    button.setStyle(style.toString());
                    HBox managebtn = new HBox(button);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(button, new Insets(0, 0, 0, 0));
                    setGraphic(managebtn);
                }
                setText(null);
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
                        String id = String.valueOf(kd.getClientID());
                        if (null != id && !id.isEmpty()) {
                            Text text = new Text(id);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
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
                    if (null != String.valueOf(kd.getDealerID())) {
                        String id = String.valueOf(kd.getDealerID());
                        if (null != id && !id.isEmpty() && !id.equalsIgnoreCase("null")) {
                            Text text = new Text(id);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
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
                        String kitNumber = String.valueOf(kd.getKitNumber());
                        if (null != kitNumber && !kitNumber.isEmpty()) {
                            Text text = new Text(kitNumber);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
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

                        String expiryDate = String.valueOf(kd.getExpiryDate());
                        if (null != expiryDate && !expiryDate.isEmpty()) {

                            String date = new SimpleDateFormat(CommonUtility.COMMON_DATE_PATTERN)
                                    .format(new java.util.Date(kd.getExpiryDate()));
                            Text text = new Text(date);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
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

                        String lotNum = String.valueOf(kd.getLotNumber());
                        if (!lotNum.isEmpty()) {
                            Text text = new Text(lotNum);
                            text.setStyle("-fx-text-alignment:center;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
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
                        String type = String.valueOf(kd.getTestUsed());
                        if (!type.isEmpty()) {
                            setGraphic(null);
                            setText(type);
                        } else {
                            setGraphic(null);
                            setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                        }

                    } else {
                        setGraphic(null);
                        setGraphic(null);
                        setText(CommonUtility.EMPTY_LABEL_FOR_TABLE);
                    }
                }
            }

        });

    }
}
