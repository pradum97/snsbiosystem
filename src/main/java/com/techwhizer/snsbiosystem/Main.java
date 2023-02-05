package com.techwhizer.snsbiosystem;

import com.techwhizer.snsbiosystem.app.AppConfig;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Objects;

public class Main extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {

        primaryStage = stage;
        //    Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("testPagination.fxml")));
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("auth/login.fxml")));
        //    stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(AppConfig.APPLICATION_ICON))));
        stage.setTitle(AppConfig.APPLICATION_NAME);
        stage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public void changeScene(String fxml, String title) {

        try {
            if (null != primaryStage && primaryStage.isShowing()) {
                Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
                primaryStage.getScene().setRoot(pane);
                Platform.runLater(() -> {
                    primaryStage.setTitle(AppConfig.APPLICATION_NAME + " ( " + title + " ) ");
                });
                primaryStage.show();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ParseException, IOException {
        launch(args);
        //  get();

     /* Platform.runLater(()->{

          // text field to show password as unmasked
          final TextField textField = new TextField();
          // Set initial state
          textField.setManaged(false);
          textField.setVisible(false);

          // Actual password field
          final PasswordField passwordField = new PasswordField();

          CheckBox checkBox = new CheckBox("Show/Hide password");

          // Bind properties. Toggle textField and passwordField
          // visibility and managability properties mutually when checkbox's state is changed.
          // Because we want to display only one component (textField or passwordField)
          // on the scene at a time.
          textField.managedProperty().bind(checkBox.selectedProperty());
          textField.visibleProperty().bind(checkBox.selectedProperty());

          passwordField.managedProperty().bind(checkBox.selectedProperty().not());
          passwordField.visibleProperty().bind(checkBox.selectedProperty().not());

          // Bind the textField and passwordField text values bidirectionally.
          textField.textProperty().bindBidirectional(passwordField.textProperty());

          VBox root = new VBox(10);
          root.getChildren().addAll(passwordField, textField, checkBox);
          Scene scene = new Scene(root, 300, 250);

          Stage stage = new Stage();

          stage.setTitle("Demo");
          stage.setScene(scene);
          stage.show();

      });*/
    }/**/

    static void get() {

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(UrlConfig.getPreviewProfileCsvUrl());

            MultipartEntityBuilder param = MultipartEntityBuilder.create();
            param.setContentType(ContentType.create("multipart/form-data", StandardCharsets.UTF_8));
            param.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(new File("D:\\sns\\sample_data\\doctorCsv.csv"));
            param.addBinaryBody("file", fileBody.getFile());
            param.addTextBody("role", "doctor");
            HttpEntity entity = param.build();
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            String content = EntityUtils.toString(resEntity, "UTF-8");

            createMultipleProfile(content);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void createMultipleProfile(String json) {

        try {
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpPost httpPut = new HttpPost(UrlConfig.getProfileCreateUrl());
            httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
            StringEntity se = new StringEntity(json);
            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity, "UTF-8");

                System.out.println(content);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}