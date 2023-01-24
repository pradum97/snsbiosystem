package com.techwhizer.snsbiosystem.controller.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.FileLoader;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.controller.auth.Login;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.model.UserDTO;
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
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

public class PreviewProfile implements Initializable {

    private int rowsPerPage = 25;
    public TableView<UserDTO> tableview;
    public TableColumn<UserDTO,String> colClientId;
    public TableColumn<UserDTO,String>colUsername;
    public TableColumn<UserDTO,String>colFirstName;
    public TableColumn<UserDTO,String>colOfficeName;
    public TableColumn<UserDTO,String>colEmail;
    public TableColumn<UserDTO,String>colLastName;
    public TableColumn<UserDTO,String> colTelephoneNum;
    public TableColumn<UserDTO,String>colStatus;
    public Pagination pagination;
    public Label totalRecordL;
    public Label totalValidRecordL;
    public Label totalInvalidRecordL;
    public Button uploadNowBn;
    public ProgressIndicator progressbar;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private ObservableList<UserDTO> userList = FXCollections.observableArrayList();
    private FilteredList<UserDTO> filteredData;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        String path =(String) Main.primaryStage.getUserData();

        if (null != path){
            callThread(path, OperationType.PREVIEW,"Please wait Data is being verified.");
        }else {
            customDialog.showAlertBox("","Invalid file. Please re select file");
            Platform.runLater(()->{
                Stage stage = (Stage) tableview.getScene().getWindow();
                if (null != stage && stage.isShowing()){
                    stage.close();
                }
            });
        }
    }

    private void callThread(String path, OperationType operationType,String loadingMsg) {
        MyAsyncTask myAsyncTask = new MyAsyncTask(path ,operationType,loadingMsg );
        myAsyncTask.execute();
    }

    public void cancelBnClick(ActionEvent event) {
        Stage stage = (Stage) uploadNowBn.getScene().getWindow();
        if (null != stage && stage.isShowing()){
            stage.close();
        }
    }

    public void changeFileBnClick(ActionEvent event) {
    }

    public void uploadBnClick(ActionEvent event) {
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        String path;

        OperationType operationType;
        String loadingMsg;

        public MyAsyncTask(String path, OperationType operationType, String loadingMsg) {
            this.path = path;
            this.operationType = operationType;
            this.loadingMsg = loadingMsg;
        }

        @Override
        public void onPreExecute() {

            if (!tableview.getItems().isEmpty()){
                tableview.getItems().clear();
                tableview.refresh();
            }

            ProgressIndicator pi = (method.getProgressBar(45,45));
            pi.setStyle("-fx-progress-color: red");
            Label label = new Label(loadingMsg);

            VBox box = new VBox(pi,label);
            VBox.setMargin(label,new Insets(10,0,0,0));
            box.setStyle("-fx-alignment: center");

            tableview.setPlaceholder(box);
        }
        @Override
        public Boolean doInBackground(String... params) {

            if ( operationType == OperationType.CREATE){
              //  createUser();
            }else if (operationType == OperationType.PREVIEW){
                sendRequest(path);
            }
            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

   private void sendRequest(String path){

       File file = new File(path);
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UrlConfig.getPreviewProfileCsvUrl());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(file);

            builder.addPart("file", fileBody);
           builder.addTextBody("role","dealer");
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null && response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(resEntity);
                Type userListType = new TypeToken<Set<UserDTO>>() { }.getType();
                Set<UserDTO> userArray = new Gson().fromJson(content, userListType);
                userList = FXCollections.observableArrayList(userArray);
                long totalRecord = 0,totalInvalid = 0 , totalValid = 0;
                totalRecord =userArray.size();

                for (UserDTO u:userList){

                    if (u.isValid()){
                        totalValid +=1;
                    }else {
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
                    pagination.setVisible(true);
                    search_Item();
                }
            }
        } catch (IOException e) {

            System.out.println(e.getMessage());
        }
    }

    private void search_Item() {
        filteredData = new FilteredList<>(userList, p -> true);
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

        colClientId.setCellValueFactory(new PropertyValueFactory<>("clientID"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("requestedLoginName"));

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

                        button.setStyle("-fx-cursor: hand ; -fx-background-color:"+ bgColor +";" +
                            "-fx-text-fill: white; -fx-background-radius: 3 ");
                    HBox managebtn = new HBox(button,info);
                    managebtn.setStyle("-fx-alignment:center");
                    HBox.setMargin(button, new Insets(0, 30, 0, 30));
                    setGraphic(managebtn);
                }
                setText(null);
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
                            setText("-");
                        }

                    } else {
                        setText("-");
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

    }
}
