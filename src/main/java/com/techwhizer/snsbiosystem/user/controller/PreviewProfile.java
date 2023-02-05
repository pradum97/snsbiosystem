package com.techwhizer.snsbiosystem.user.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.user.constant.RoleOption;
import com.techwhizer.snsbiosystem.user.model.CreateUsersResponse;
import com.techwhizer.snsbiosystem.user.model.UserDTO;
import com.techwhizer.snsbiosystem.util.ChooseFile;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.RowPerPage;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class PreviewProfile implements Initializable {

    public VBox bottomContainer;
    public VBox chooseFileContainer;
    public VBox tableContainer;

    public TableView<UserDTO> tableview;
    public TableColumn<UserDTO, String> colClientId;
    public TableColumn<UserDTO, String> colUsername;
    public TableColumn<UserDTO, String> colFirstName;
    public TableColumn<UserDTO, String> colOfficeName;
    public TableColumn<UserDTO, String> colEmail;
    public TableColumn<UserDTO, String> colLastName;
    public TableColumn<UserDTO, String> colTelephoneNum;
    public TableColumn<UserDTO, String> colStatus;
    public Pagination pagination;
    public Label totalRecordL;
    public Label totalValidRecordL;
    public Label totalInvalidRecordL, fileNameL;
    public Label uploadNowBn;
    public ProgressIndicator progressbar,fileChooseProgressBar;
    public HBox paginationContainer;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private ObservableList<UserDTO> userList = FXCollections.observableArrayList();
    private FilteredList<UserDTO> filteredData;
    private File selectedFile;
    @FXML
    private ComboBox<String> roleCom;
    private int statusCode;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        method.hideElement(progressbar, bottomContainer, tableContainer);

        new MyAsyncTask(null,OperationType.SORTING_LOADING,null,null).execute();
    }

    public void chooseFileBnClick(MouseEvent mouseEvent) {
        File file = new ChooseFile().chooseCSVFile();
        if (null != file) {
            selectedFile = file;
            fileNameL.setText(file.getAbsolutePath());
        }
    }

    public void submitBnClick(MouseEvent event) {
        if (null == selectedFile) {
            method.show_popup("Please choose file", fileNameL);
            return;
        } else if (roleCom.getSelectionModel().isEmpty()) {
            method.show_popup("Please select role", roleCom);
            return;
        }

        String role = roleCom.getSelectionModel().getSelectedItem();

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText(CommonUtility.SUBMIT_CONFIRMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {

            Platform.runLater(() -> {
                Stage stage = (Stage) tableview.getScene().getWindow();
                stage.setMaximized(true);
                stage.setTitle("File Path : " + selectedFile.getAbsolutePath() + " RoleOption :" + role);
            });
            method.hideElement(chooseFileContainer);
            tableContainer.setVisible(true);
            bottomContainer.setVisible(true);
            Platform.runLater(()->{
                OptionalMethod.minimizedStage((Stage) uploadNowBn.getScene().getWindow(),true);
            });

            callThread(role, OperationType.PREVIEW, "Please wait Data is being verified.", null);


        } else {
            alert.close();
        }
    }

    private void callThread(String role, OperationType operationType, String loadingMsg, String json) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(role, operationType, loadingMsg, json);
        myAsyncTask.execute();
    }

    public void cancelBnClick(MouseEvent event) {
        Stage stage = (Stage) uploadNowBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void uploadBnClick(MouseEvent event) {

        if (userList.size() < 1) {
            customDialog.showAlertBox("", "Record not found");
            return;
        }
        long totRec = Long.parseLong(totalRecordL.getText());
        long totInvalid = Long.parseLong(totalInvalidRecordL.getText());

        if (totRec == totInvalid) {
            customDialog.showAlertBox("", "All records are invalid. Please select a valid file or role");
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
            String json = new Gson().toJson(userList);
            callThread(null, OperationType.CREATE, "Please wait", json);

        } else {
            alert.close();
        }
    }

    private void createMultipleProfile(String json) {

        try {
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpPost httpPut = new HttpPost(UrlConfig.getProfileCreateUrl());
            httpPut.addHeader("Content-Type", "application/json");
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                System.out.println(statusCode);
                System.out.println(content);

                if (statusCode == 200) {
                    method.hideElement(progressbar);
                    uploadNowBn.setVisible(true);
                    CreateUsersResponse cur = new Gson().fromJson(content, CreateUsersResponse.class);
                    customDialog.showAlertBox("Success", cur.getMessage() + "\n\nTotal User Added : " + cur.getTotalUserAdded());
                    Main.primaryStage.setUserData(true);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) uploadNowBn.getScene().getWindow();
                        if (null != stage && stage.isShowing()) {
                            stage.close();
                        }
                    });

                } else {
                    customDialog.showAlertBox("Failed.", "Something went wrong. Please try again.");
                }
            }

        } catch (Exception e) {
            method.hideElement(progressbar);
            uploadNowBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    public void cancelCsvChooser(MouseEvent mouseEvent) {

        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

        if (null != stage && stage.isShowing()){
            stage.close();
        }
    }

    public void keyPress(KeyEvent keyEvent) {

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        String role;
        OperationType operationType;
        String loadingMsg, json;

        public MyAsyncTask(String role, OperationType operationType, String loadingMsg, String json) {
            this.role = role;
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
            }else if (operationType == OperationType.SORTING_LOADING){}
            else {
                method.hideElement(uploadNowBn);
                progressbar.setVisible(true);
            }
        }
        @Override
        public Boolean doInBackground(String... params) {

            if ( operationType == OperationType.CREATE){
                createMultipleProfile(json);
            }else if (operationType == OperationType.PREVIEW){
                sendRequest(role);
            } else if (operationType == OperationType.SORTING_LOADING) {
                roleCom.setItems(FXCollections.observableArrayList(RoleOption.sortingMap.keySet()));
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {

            if (operationType == OperationType.CREATE) {
                method.hideElement(progressbar);
                uploadNowBn.setVisible(true);
            } else if (operationType == OperationType.SORTING_LOADING) {
                fileChooseProgressBar.setVisible(false);
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

    private void sendRequest(String role) {

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UrlConfig.getPreviewProfileCsvUrl());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setContentType(ContentType.create("multipart/form-data", Charset.forName("UTF-8")));
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(selectedFile);
            builder.addPart("file", fileBody);
            builder.addTextBody("role", role.toLowerCase());
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            statusCode = response.getStatusLine().getStatusCode();

            if (resEntity != null && response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(resEntity, "UTF-8");
                Type userListType = new TypeToken<Set<UserDTO>>() {
                }.getType();
                Set<UserDTO> userArray = new Gson().fromJson(content, userListType);
                userList = FXCollections.observableArrayList(userArray);
                long totalRecord = 0, totalInvalid = 0, totalValid = 0;
                totalRecord = userArray.size();

                for (UserDTO u : userList) {

                    if (u.isValid()) {
                        totalValid += 1;
                    } else {
                        totalInvalid += 1;
                    }
                }

                long finalTotalRecord = totalRecord;
                long finalTotalInvalid = totalInvalid;
                long finalTotalValid = totalValid;
                Platform.runLater(()->{
                    totalRecordL.setText(String.valueOf(finalTotalRecord));
                    totalValidRecordL.setText(String.valueOf(finalTotalValid));
                    totalInvalidRecordL.setText(String.valueOf(finalTotalInvalid));
                });

                if (userList.size() > 0) {
                    paginationContainer.setDisable(false);
                    search_Item();
                }
            }else {
                customDialog.showAlertBox("Failed","Something went wrong!");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    private void search_Item() {
        filteredData = new FilteredList<>(userList, p -> true);
        pagination.setCurrentPageIndex(0);
        int rowsPerPage = RowPerPage.PREVIEW_PROFILE_ROW_PER_PAGE;
        changeTableView(0, rowsPerPage);
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newValue1) -> {
                    tableview.scrollTo(0);
                    changeTableView(newValue1.intValue(), rowsPerPage);
                });
    }
    private void changeTableView(int index, int limit) {

        int totalPage = (int) (Math.ceil(filteredData.size() * 1.0 / RowPerPage.PREVIEW_PROFILE_ROW_PER_PAGE));
        Platform.runLater(() -> pagination.setPageCount(totalPage));

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

    private void setOptionalCell(){
        colStatus.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    boolean isValid = user.isValid();
                    String txt = isValid?"VALID":"INVALID";
                    Button button = new Button(txt);

                    ImageView info = new ImageView(new ImageLoader().load("img/icon/info_ic.png"));
                    info.setFitWidth(25);
                    info.setFitHeight(25);
                    info.setStyle("-fx-cursor: hand");
                    button.setMinWidth(70);
                    String bgColor = "" ;

                    if (isValid) {
                        info.setVisible(false);
                        bgColor = "green";
                    } else {
                        info.setVisible(true);
                       bgColor = "red";
                    }

                    info.setOnMousePressed(mouseEvent -> {

                        Set<String> invalidFields = user.getInvalidFields();

                        StringBuilder invalidsStr = new StringBuilder();
                        for (String str : invalidFields) {
                            invalidsStr.append(str).append(", ");
                        }
                        if (invalidFields.size() > 0) {
                            customDialog.showAlertBox("", "Invalid Data : " + invalidsStr);
                        }
                    });

                    String style = "-fx-cursor: hand ; -fx-background-color:" + bgColor + ";" +
                            "-fx-text-fill: white; -fx-background-radius: 3 ";

                    button.setStyle(style.toString());
                    HBox managebtn = new HBox(button, info);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(button, new Insets(0, 30, 0, 30));
                    setText(null);
                    setGraphic(managebtn);
                }

            }

        });


        colFirstName.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getFirstName()) {
                        if (!user.getFirstName().isEmpty()) {
                            Text text = new Text(user.getFirstName());
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

        colLastName.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getLastName()) {
                        if (!user.getLastName().isEmpty()) {
                            Text text = new Text(user.getLastName());
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


        colClientId.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getClientID()) {
                        if (!user.getClientID().toString().isEmpty()) {
                            Text text = new Text(String.valueOf(user.getClientID()));
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

        colUsername.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getRequestedLoginName()) {
                        if (!user.getRequestedLoginName().isEmpty()) {
                            Text text = new Text(user.getRequestedLoginName());
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
        colTelephoneNum.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
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

        colOfficeName.setCellFactory((TableColumn<UserDTO, String> param) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);

                } else {
                    UserDTO user = tableview.getItems().get(getIndex());
                    if (null != user.getOfficeCompanyName()) {
                        if (!user.getOfficeCompanyName().isEmpty()) {

                            Text text = new Text(user.getOfficeCompanyName());
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
}
