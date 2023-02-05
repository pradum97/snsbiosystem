package com.techwhizer.snsbiosystem.sterilizer.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.sterilizer.model.AddSterilizerResponse;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerDTO;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.*;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class PreviewSterilizers implements Initializable {
    public Pagination pagination;
    public Label totalRecordL;
    public Label totalValidRecordL;
    public Label totalInvalidRecordL;
    public Label uploadNowBn;
    public ProgressIndicator progressbar;
    public TableView<SterilizerDTO> tableview;
    public TableColumn<SterilizerDTO, String> colClientId;
    public TableColumn<SterilizerDTO, String> colSterilizerId;
    public TableColumn<SterilizerDTO, String> colSterilizerListNumber;
    public TableColumn<SterilizerDTO, String> colSterilizerType;
    public TableColumn<SterilizerDTO, String> colSterilizerBrand;
    public TableColumn<SterilizerDTO, String> colSterilizerSerialNumber;
    public TableColumn<SterilizerDTO, String> colStatus;
    public HBox paginationContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private File file;
    private int statusCode;

    private ObservableList<SterilizerDTO> sterilizerList = FXCollections.observableArrayList();
    private FilteredList<SterilizerDTO> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        method.hideElement(progressbar);

        Platform.runLater(()->{
            OptionalMethod.minimizedStage((Stage) uploadNowBn.getScene().getWindow(),true);
        });

        if (null != Main.primaryStage.getUserData() &&
                Main.primaryStage.getUserData()  instanceof File){
            file = (File) Main.primaryStage.getUserData();

            callThread( OperationType.PREVIEW, "Please wait Data is being verified.", null);

            Platform.runLater(()->{
                Stage stage = (Stage)uploadNowBn.getScene().getWindow();
                if (null != stage){
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

        if (sterilizerList.size() < 1) {
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
        alert.setHeaderText("are you sure, you want to upload the file?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            String json = new Gson().toJson(sterilizerList);
            callThread( OperationType.CREATE, "Please wait", json);

        } else {
            alert.close();
        }
    }

    private void createMultipleSterilizer(String json) {

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UrlConfig.getAddSterilizerUrl());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cookie",  (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
            httpPost.setEntity(se);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                AddSterilizerResponse asr = new Gson().fromJson(content, AddSterilizerResponse.class);
                List<SterilizerDTO> failedSterilizer = asr.getInvalidData();
                List<SterilizerDTO> added = asr.getAdded();

                int addedCount = added.size();
                int failedCount = failedSterilizer.size();

                tableview.setItems(null);
                tableview.refresh();

                Platform.runLater(() -> {
                    if (statusCode == 200) {

                        if (failedSterilizer.size() > 0) {

                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Total Added : ").append(addedCount).append("\n");
                            stringBuilder.append("Total Failed : ").append(failedCount).append("\n\n");
                            stringBuilder.append("S ID").append("\n");
                            for (SterilizerDTO u : failedSterilizer) {
                                stringBuilder.append(" ").append(null == u.getSterilizerListNumber() ? CommonUtility.EMPTY_LABEL_FOR_TABLE : u.getSterilizerListNumber()).append("  -   ").
                                        append(null == u.getErrorMessage()?"Invalid Data":u.getErrorMessage()).append("\n");
                            }
                            Main.primaryStage.setUserData(true);
                            cancelBnClick(null);
                            customDialog.showFullAlertBox("Success", String.valueOf(stringBuilder));
                        } else {
                            Main.primaryStage.setUserData(true);
                            cancelBnClick(null);
                            customDialog.showAlertBox("Success", "Sterilizer successfully created");

                        }

                        method.hideElement(progressbar);
                        uploadNowBn.setVisible(true);

                    } else if (statusCode == StatusCode.UNAUTHORISED) {
                        new HttpStatusHandler(StatusCode.UNAUTHORISED);
                    } else {
                        new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                    }
                });
            }

        } catch (Exception e) {
            method.hideElement(progressbar);
            uploadNowBn.setVisible(true);
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
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

            if ( operationType == OperationType.CREATE){
                createMultipleSterilizer(json);
            }else if (operationType == OperationType.PREVIEW){
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

                if (statusCode == 500 ){
                    tableview.setPlaceholder(new Label("CSV File not valid. Please select valid CSV file."));
                }else {
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
            HttpPost httpPost = new HttpPost(UrlConfig.getSterilizerPreviewUrl());
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

                Type type = new TypeToken<Set<SterilizerDTO>>() {}.getType();
                Set<SterilizerDTO> sterilizerArray = new Gson().fromJson(content, type);

                sterilizerList = FXCollections.observableArrayList(sterilizerArray);
                long totalRecord = 0, totalInvalid = 0, totalValid = 0;
                totalRecord = sterilizerArray.size();
                for (SterilizerDTO u : sterilizerArray) {

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

                if (sterilizerList.size() > 0) {
                    paginationContainer.setDisable(false);
                    search_Item();
                }
            } else if (statusCode == StatusCode.UNAUTHORISED) {
                new HttpStatusHandler(StatusCode.UNAUTHORISED);
            } else {
                new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            }
        } catch (IOException e) {
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        }
    }
    private void search_Item() {
        filteredData = new FilteredList<>(sterilizerList, p -> true);
        pagination.setCurrentPageIndex(0);
        int rowsPerPage = RowPerPage.PREVIEW_STERILIZERS_ROW_PER_PAGE;
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    tableview.scrollTo(0);
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }
    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / RowPerPage.PREVIEW_STERILIZERS_ROW_PER_PAGE));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

        setOptionalCell();

        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, sterilizerList.size());

        int minIndex = Math.min(toIndex, filteredData.size());
        SortedList<SterilizerDTO> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);
        tableview.setRowFactory(tv -> new TableRow<SterilizerDTO>() {
            @Override
            protected void updateItem(SterilizerDTO item, boolean empty) {
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
    private void setOptionalCell(){
        colStatus.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SterilizerDTO user = tableview.getItems().get(getIndex());
                    boolean isValid = user.isValid();
                    String txt = isValid?"VALID":"INVALID";
                    Button button = new Button(txt);

                    button.setMinWidth(70);
                    button.setMaxHeight(10);
                    String bgColor = "" ;

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


        colClientId.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerDTO sd = tableview.getItems().get(getIndex());
                    if (null != sd.getClientID()) {
                        String id = String.valueOf(sd.getClientID());
                        if (null !=  id &&!id.isEmpty()) {
                            Text text = new Text(id);
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

        colSterilizerId.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerDTO sd = tableview.getItems().get(getIndex());
                    if (null != String.valueOf(sd.getSterilizerID())) {

                        String sterilizerId = String.valueOf(sd.getSterilizerID());
                        if (null !=  sterilizerId && !sterilizerId.isEmpty()&&!sterilizerId.equalsIgnoreCase("null")) {
                            Text text = new Text(sterilizerId);
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


        colSterilizerBrand.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerDTO sd = tableview.getItems().get(getIndex());
                    if (null != sd.getSterilizerBrand()) {
                        String brand = sd.getSterilizerBrand();
                        if (null !=  brand &&!brand.isEmpty()) {
                            Text text = new Text(brand);
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

        colSterilizerListNumber.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerDTO sd = tableview.getItems().get(getIndex());
                    if (null != sd.getSterilizerListNumber()) {
                        String listNumber = String.valueOf(sd.getSterilizerListNumber());
                        if (null !=  listNumber && !listNumber.isEmpty()) {
                            Text text = new Text(listNumber);
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

        colSterilizerSerialNumber.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerDTO sd = tableview.getItems().get(getIndex());
                    if (null != sd.getSterilizerSerialNumber()) {

                        String serialNum = sd.getSterilizerSerialNumber();
                        if (!serialNum.isEmpty()) {
                            Text text = new Text(serialNum);
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
        colSterilizerType.setCellFactory((TableColumn<SterilizerDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    SterilizerDTO sd = tableview.getItems().get(getIndex());
                    if (null != sd.getSterilizerType()) {
                        String type = sd.getSterilizerType();
                        if (!type.isEmpty()) {
                            setText(type);
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
}
