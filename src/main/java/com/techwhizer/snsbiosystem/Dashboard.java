package com.techwhizer.snsbiosystem;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.controller.auth.Login;
import com.techwhizer.snsbiosystem.model.AuthResponse;
import com.techwhizer.snsbiosystem.model.DashboardModel;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.UrlConfig;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dashboard extends OptionalMethod implements Initializable {

    @FXML
    public Button dashboardBn, manageKitBn, manageSterilizerBn, userBn, labBn, accountBn;
    public Button logoutBn;

    public Label fullName;
    public Label username;
    public StackPane mainContainer;
    @FXML
    ImageView hideIv, showIv;
    public VBox menuContainer, topUserContainer;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userBnClick(null);
        customDialog = new CustomDialog();
        config();
        startThread();
    }

    private void config() {
        hideElement(showIv);
        hideMenu(null);
        setToolTip();
    }

    private void setToolTip() {
        set(dashboardBn, "HOME");
        set(manageKitBn, "MANAGE KITS");
        set(manageSterilizerBn, "MANAGE STERILIZER");
        set(userBn, "USERS");
        set(labBn, "LAB");
        set(logoutBn, "LOGOUT");
    }

    private void set(Button button, String text) {

        if (showIv.isVisible()) {
            Tooltip t = new Tooltip(text);
            button.setTooltip(t);
        }
    }

    public void hideMenu(MouseEvent mouseEvent) {
        dashboardBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        manageKitBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        manageSterilizerBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        userBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        labBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        logoutBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        accountBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        showIv.setVisible(true);
        hideElement(topUserContainer, hideIv);
        menuContainer.setStyle("-fx-padding:0 10 0 10");
    }

    public void showMenu(MouseEvent mouseEvent) {
        dashboardBn.setContentDisplay(ContentDisplay.LEFT);
        manageKitBn.setContentDisplay(ContentDisplay.LEFT);
        manageSterilizerBn.setContentDisplay(ContentDisplay.LEFT);
        userBn.setContentDisplay(ContentDisplay.LEFT);
        labBn.setContentDisplay(ContentDisplay.LEFT);
        logoutBn.setContentDisplay(ContentDisplay.LEFT);
        accountBn.setContentDisplay(ContentDisplay.LEFT);
        topUserContainer.setVisible(true);
        hideIv.setVisible(true);
        menuContainer.setStyle("-fx-padding: 0 10 0 10");
        hideElement(showIv);
    }

    public void startThread() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }

    void toggleButtonSize(Node node) {

        switch (node.getId()) {
            case "profile" -> {
                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: green;" +
                        "-fx-font-size: 13;-fx-border-radius: 5;-fx-text-fill: white;" +
                        "-fx-font-weight: bold;-fx-cursor: hand");
            }
            case "edit" -> {

                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: #006666;-fx-font-size: 13;" +
                        "-fx-border-radius: 5;-fx-text-fill: white;-fx-font-weight: bold;-fx-cursor: hand");
            }

            case "changePassword" -> {

                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: #7e0101;-fx-font-size: 13;" +
                        "-fx-border-radius: 5;-fx-text-fill: white;-fx-font-weight: bold;-fx-cursor: hand");
            }

        }

    }

    public void logoutBnClick(ActionEvent event) {

        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText("Are you sure you want to logout?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            new Main().changeScene("auth/login.fxml", "LOGIN HERE");
            Login.authInfo.clear();
        } else {
            alert.close();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        public void onPreExecute() {
        }

        @Override
        public Boolean doInBackground(String... params) {
            /* Background Thread is running */
            Platform.runLater(()->{
                setUserData();
            });

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
        }

        @Override
        public void progressCallback(Integer... params) {
        }
    }

    private void setUserData() {

        AuthResponse authResponse =(AuthResponse) Login.authInfo.get("auth_response");
        fullName.setText(authResponse.getFirstName()+" "+authResponse.getLastName());
        username.setText(authResponse.getUserName());
    }

    public void accountBnClick(ActionEvent event) {
        ToggleButton profileBn = new ToggleButton("PROFILE");
        ToggleButton editProfileBn = new ToggleButton("EDIT PROFILE");
        ToggleButton changePassword = new ToggleButton("CHANGE PASSWORD");
        Stage stage2 = new Stage();

        profileBn.setId("profile");
        editProfileBn.setId("edit");
        changePassword.setId("changePassword");

        toggleButtonSize(profileBn);
        toggleButtonSize(editProfileBn);
        toggleButtonSize(changePassword);

        VBox pane = new VBox(20, profileBn, editProfileBn, changePassword);
        pane.setAlignment(Pos.CENTER_RIGHT);
        pane.setStyle("-fx-padding: 80 80 80 80");

        stage2.initOwner(Main.primaryStage);
        stage2.initStyle(StageStyle.UTILITY);
        stage2.setTitle("ACCOUNT SETTING");
        stage2.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(pane);
        stage2.setScene(scene);
        stage2.setResizable(false);

        changePassword.setOnAction(event1 -> customDialog.showFxmlDialog2("auth/changePassword.fxml", ""));
        editProfileBn.setOnAction(event1 -> customDialog.showFxmlFullDialog("update/user/createProfile.fxml", ""));
        profileBn.setOnAction(event12 -> {
            Long id =(Long) Login.authInfo.get("current_id");
            Main.primaryStage.setUserData(id);
            customDialog.showFxmlFullDialog("profile/my_profile.fxml", "PROFILE");
        });

        stage2.showAndWait();
    }

    public void labBnClick(ActionEvent event) {
        replaceScene("dashboard/labs.fxml");
    }

    public void userBnClick(ActionEvent event) {
        replaceScene("dashboard/users.fxml");
    }

    public void sterilizerBnClick(ActionEvent event) {
        replaceScene("dashboard/sterilizers.fxml");
    }

    public void dashboardBnClick(ActionEvent event) {
        replaceScene("dashboard/home.fxml");
    }

    public void manageKitBnClick(ActionEvent event) {
        replaceScene("dashboard/kits.fxml");
    }

    private void replaceScene(String fxml_file_name) {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml_file_name)));
            mainContainer.getChildren().removeAll();
            mainContainer.getChildren().setAll(parent);
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
}
