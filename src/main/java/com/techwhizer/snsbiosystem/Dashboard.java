package com.techwhizer.snsbiosystem;

import com.techwhizer.snsbiosystem.app.AppConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.AuthResponse;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
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

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dashboard extends OptionalMethod implements Initializable {

    @FXML
    public Button dashboardBn, manageKitBn, manageSterilizerBn, userBn,noticeBn , accountBn;
    public Button logoutBn;

    public Label fullName;
    public Label username;
    public StackPane mainContainer;
    public ImageView topProfileImg;
    public Separator topSeparator;
    @FXML
    ImageView hideIv, showIv;
    public VBox menuContainer, topUserContainer;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dashboardBnClick(null);

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
        CommonUtility.onHoverShowTextButton(dashboardBn, "DASHBOARD");
        CommonUtility.onHoverShowTextButton(manageKitBn, "KITS");
        CommonUtility.onHoverShowTextButton(manageSterilizerBn, "STERILIZERS");
        CommonUtility.onHoverShowTextButton(userBn, "USERS");
        CommonUtility.onHoverShowTextButton(noticeBn, "NOTICE");
        CommonUtility.onHoverShowTextButton(logoutBn, "LOGOUT");
    }

    public void hideMenu(MouseEvent mouseEvent) {
        dashboardBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        manageKitBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        manageSterilizerBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        userBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        noticeBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        logoutBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        accountBn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        showIv.setVisible(true);
        hideElement(topProfileImg,fullName,username, topSeparator,hideIv);
        menuContainer.setStyle("-fx-padding:0 10 0 10");
    }

    public void showMenu(MouseEvent mouseEvent) {
        dashboardBn.setContentDisplay(ContentDisplay.LEFT);
        manageKitBn.setContentDisplay(ContentDisplay.LEFT);
        manageSterilizerBn.setContentDisplay(ContentDisplay.LEFT);
        userBn.setContentDisplay(ContentDisplay.LEFT);
        noticeBn.setContentDisplay(ContentDisplay.LEFT);
        logoutBn.setContentDisplay(ContentDisplay.LEFT);
        accountBn.setContentDisplay(ContentDisplay.LEFT);

        topProfileImg.setVisible(true);
        fullName.setVisible(true);
        username.setVisible(true);
        topSeparator.setVisible(true);

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
                        "-fx-font-size: 13;-fx-background-radius:30;-fx-border-radius: 30;-fx-text-fill: white;" +
                        "-fx-font-weight: bold;-fx-cursor: hand");
            }
            case "edit" -> {

                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: #006666;-fx-font-size: 13;" +
                        "-fx-border-radius: 30;-fx-background-radius:30;-fx-text-fill: white;-fx-font-weight: bold;-fx-cursor: hand");
            }

            case "changePassword" -> {

                node.setStyle("-fx-min-width: " + (double) 200 + ";-fx-min-height: " + (double) 35 + ";" +
                        " -fx-border-color: grey;-fx-background-color: #7e0101;-fx-font-size: 13;" +
                        "-fx-border-radius: 30;-fx-background-radius:30;-fx-text-fill: white;-fx-font-weight: bold;-fx-cursor: hand");
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

        AuthResponse authResponse = (AuthResponse) Login.authInfo.get("auth_response");
        fullName.setText((authResponse.getFirstName() + " " + authResponse.getLastName()).toUpperCase());
        username.setText(authResponse.getUserName());
    }

    void unselectedBg(Node... nodes) {
        for (Node node : nodes) {
            node.setStyle("-fx-background-color: transparent");
            node.setDisable(false);
        }
    }

    void selectedBg(Node node) {
        node.setDisable(true);
        node.setStyle("""
                 -fx-border-radius: 4;
                    -fx-background-color: #006666;
                    -fx-text-fill: white;
                """);
    }

    public void accountBnClick(ActionEvent event) {


        Main.primaryStage.setUserData(null);
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
        stage2.initStyle(StageStyle.DECORATED);
        stage2.setTitle("ACCOUNT SETTING");
        stage2.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(pane);
        stage2.setScene(scene);
        stage2.setResizable(false);
        Long id = (Long) Login.authInfo.get("current_id");
        OptionalMethod method = new OptionalMethod();

        changePassword.setOnAction(event1 -> {
            method.closeStage(changePassword);
            customDialog.showFxmlDialog2("auth/changePassword.fxml", "");
        });
        editProfileBn.setOnAction(event1 -> {
            Map<String, Object> map = new HashMap<>();
            map.put("operation_type", OperationType.UPDATE);
            map.put("client_id", id);
            Main.primaryStage.setUserData(map);
            method.closeStage(editProfileBn);
            customDialog.showFxmlFullDialog("profile/createProfile.fxml", "");
        });
        profileBn.setOnAction(event12 -> {
            Main.primaryStage.setUserData(id);
            method.closeStage(profileBn);
            customDialog.showFxmlFullDialog("profile/my_profile.fxml", "PROFILE");
        });

        stage2.show();
    }

    public void userBnClick(ActionEvent event) {

        changeTitle("USERS");

        unselectedBg(dashboardBn, manageKitBn, manageSterilizerBn, accountBn, noticeBn);
        selectedBg(userBn);

        Main.primaryStage.setUserData(null);
        replaceScene("profile/users.fxml");
    }

    public void sterilizerBnClick(ActionEvent event) {

        changeTitle("STERILIZERS");
        unselectedBg(dashboardBn, manageKitBn, userBn, accountBn, noticeBn);
        selectedBg(manageSterilizerBn);
        Main.primaryStage.setUserData(null);
        replaceScene("sterilizer/sterilizers.fxml");
    }

    public void dashboardBnClick(ActionEvent event) {
        changeTitle("HOME");

        unselectedBg(userBn, manageKitBn, manageSterilizerBn, accountBn, noticeBn);
        selectedBg(dashboardBn);

        Main.primaryStage.setUserData(null);
        replaceScene("dashboard/home.fxml");
    }

    public void manageKitBnClick(ActionEvent event) {
        changeTitle("KITS");
        unselectedBg(dashboardBn, userBn, manageSterilizerBn, accountBn, noticeBn);
        selectedBg(manageKitBn);
        Main.primaryStage.setUserData(null);
        replaceScene("kit/kits.fxml");
    }

    public void noticeBnClick(ActionEvent event) {
        changeTitle("NOTICES");
        unselectedBg(dashboardBn, manageKitBn, manageSterilizerBn, accountBn, userBn);
        selectedBg(noticeBn);
        replaceScene("notice/notices.fxml");
    }

    private void changeTitle(String str) {

        String previousTitle =   Main.primaryStage.getTitle();

        Main.primaryStage.setTitle(AppConfig.APPLICATION_NAME+"( "+"DASHBOARD->"+str+" )");
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
