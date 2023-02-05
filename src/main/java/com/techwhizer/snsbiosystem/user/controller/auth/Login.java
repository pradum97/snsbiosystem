package com.techwhizer.snsbiosystem.user.controller.auth;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.user.constant.Roles;
import com.techwhizer.snsbiosystem.user.model.AuthResponse;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.*;

public class Login implements Initializable {

    public TextField usernameTf;
    public TextField passwordTf;
    public Button login_button;
    public ProgressIndicator progressBar;
    public HBox passwordContainer;
    private OptionalMethod method ;
    private CustomDialog customDialog;

    public static Map<String , Object> authInfo = new HashMap<>();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        method.hideElement(progressBar);

      //  CommonUtility.passwordMaskFiled(passwordTf,maskImage);

        passwordMaskFiled();
    }

    public  void passwordMaskFiled() {

        usernameTf.setFocusTraversable(true);
        passwordTf = new TextField();
        passwordTf.setManaged(false);
        passwordTf.setVisible(false);
        final PasswordField passwordField = new PasswordField();
        passwordField.setText("snsadmin");
        ImageView icon = new ImageView(new ImageLoader().load("img/icon/showPassword.png"));
        icon.setFitHeight(26);
        icon.setFitWidth(26);
        icon.setStyle("-fx-cursor: hand");

        passwordField.prefHeight(passwordTf.getPrefHeight());
        passwordField.prefWidth(passwordTf.getPrefWidth());

        passwordField.setPromptText("Enter password");
        passwordTf.setPromptText("Enter password");

        passwordField.setMinWidth(385);
        passwordField.setMinHeight(46);

        passwordTf.setMinWidth(385);
        passwordTf.setMinHeight(46);

        passwordTf.managedProperty().bind(icon.pressedProperty());
        passwordTf.visibleProperty().bind(icon.pressedProperty());

        HBox.setMargin(icon,new Insets(0,8,0,0));
        passwordField.setStyle("-fx-background-radius: 4;-fx-border-radius:4;-fx-border-color: white;-fx-background-color: white;-fx-font-size: 14;-fx-font-family: Arial");
        passwordTf.setStyle("-fx-background-radius: 4;-fx-border-radius:4;-fx-border-color: white;-fx-background-color: white;-fx-font-size: 14;-fx-font-family: Arial");

        passwordField.managedProperty().bind(icon.pressedProperty().not());
        passwordField.visibleProperty().bind(icon.pressedProperty().not());
        passwordTf.textProperty().bindBidirectional(passwordField.textProperty());
        passwordContainer.getChildren().addAll(passwordTf,passwordField,icon);

    }

    public void forget_password_bn(ActionEvent event) {

        customDialog.showFxmlDialog2("auth/forgotPassword.fxml","FORGOT PASSWORD");
    }

    public void enterPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            callThread();
        }
    }

    public void loginBn(ActionEvent event) {
        callThread();
    }

    private void callThread() {

        String username = usernameTf.getText();
        String password = passwordTf.getText();

        if (username.isEmpty()){
            method.show_popup("Please enter username",usernameTf);
            return;
        }else if (password.isEmpty()){
            method.show_popup("Please enter password",passwordContainer);
            return;
        }

        Map<String,String> userMap = new HashMap<>();
        userMap.put("username",username);
        userMap.put("password",password);

        MyAsyncTask myAsyncTask = new MyAsyncTask(userMap);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();

    }

    private void startLogin(Map<String, String> userMap) {

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("easy")
                        .build()).build();

        HttpGet httpPost = new HttpGet(UrlConfig.getLoginUrl());
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", getBasicAuthenticationHeader(userMap.get("username"),
                userMap.get("password")));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                Header[] headers = response.getHeaders("Set-Cookie");
                String token = headers[0].getValue();

                if(response.getStatusLine().getStatusCode() == 200){
                    AuthResponse authRes = new Gson().fromJson(content, AuthResponse.class);
                    Set<String> roles = authRes.getRoles();
                    boolean isAdmin = false;

                    for (String role:roles){
                        isAdmin = role.equals(Roles.ROLE_ADMIN.toString());
                    }

                    if(isAdmin){
                        authInfo.put("token",token);
                        authInfo.put("username",userMap.get("username"));
                        authInfo.put("basic_auth_header",
                                getBasicAuthenticationHeader(userMap.get("username"), userMap.get("password")));

                        authInfo.put("auth_response",authRes);
                        authInfo.put("current_id",authRes.getId());

                        Platform.runLater(()->new Main().changeScene("dashboard.fxml","DASHBOARD"));
                    }else {

                        String msg = "You do not have permission to access this application";
                        String contentMsg = "Please login with administrator privileges and try again";

                        customDialog.showAlertBox("",msg,contentMsg);
                    }

                }else {
                    customDialog.showAlertBox(" Authenticate Failed","Username or Password Incorrect.");
                }
            }

        } catch (Exception e) {
            Platform.runLater(()->customDialog.showAlertBox("Authentication Failed!","Something went wrong. Please try again"));
            method.hideElement(progressBar);
            login_button.setVisible(true);
            throw new RuntimeException(e);
        }


    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Map<String,String> userMap;
        public MyAsyncTask(Map<String, String> userMap) {
            this.userMap = userMap;
        }

        @Override
        public void onPreExecute() {
            method.hideElement(login_button);
            progressBar.setVisible(true);

        }

        @Override
        public Boolean doInBackground(String... params) {
           /* if (InternetConnection.checkInternetConnection(true)){
                startLogin(userMap);
            }*/

            startLogin(userMap);

            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            login_button.setVisible(true);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }
}
