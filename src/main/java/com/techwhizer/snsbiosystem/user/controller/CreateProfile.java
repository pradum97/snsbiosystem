package com.techwhizer.snsbiosystem.user.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.user.constant.ReportingMethods;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.*;
import com.techwhizer.snsbiosystem.user.util.CheckUsername;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;


public class CreateProfile implements Initializable {

    public Button submitBn;
    public CheckBox adminCb;
    public TextField usernameTf;
    public TextField firstNameTf;
    public TextField lastNameTf;
    public TextField workPhoneNumberTf;
    public TextField workEmailTf;
    public TextField companyNameTf;
    public TextField officeStateTf;
    public TextField officeCityTf;
    public TextField officeZipTf;
    public TextField officeFaxNumberTf;
    public TextArea officeAddressTa;
    public CheckBox sameAsOfficeAddress;
    public TextField billingCityTf;
    public TextArea billingAddressTa;
    public TextField billingZipTf;
    public CheckBox dealerCb, selectAllCb;
    public CheckBox doctorCb;
    public CheckBox patientCb;
    public ProgressIndicator progressBar;
    public TextField billingStateTf;
    public VBox billingDetailsContainer;
    public VBox contentContainer;
    public ComboBox<String> sharingMethodCom;
    public HBox progressContainer;
    private OperationType userCreateOperationType;
    private OptionalMethod method;
    private CustomDialog customDialog;
    private RoleConfigModel r = new RoleConfigModel();
    private boolean isSameAsAbove = false;
    private String currentUsername;
    private Long clientId;
    private boolean isEmailAvailable = false;
    private HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec("easy").build()).build();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();
        method.hideElement(progressBar, progressContainer);
        sharingMethodCom.setItems(ReportingMethods.reportShareMethod);
        Map<String, Object> map = (Map<String, Object>) Main.primaryStage.getUserData();
        userCreateOperationType = (OperationType) map.get("operation_type");

        Platform.runLater(()->{
            OptionalMethod.minimizedStage((Stage) submitBn.getScene().getWindow(),true);
        });

        if (null == userCreateOperationType) {
            customDialog.showAlertBox("", "Operation type not valid");
            Platform.runLater(() -> {
                Stage stage = (Stage) submitBn.getScene().getWindow();
                if (null != stage && stage.isShowing()) {
                    stage.close();
                }
            });
            return;
        }
        switch (userCreateOperationType) {
            case CREATE -> {
                submitBn.setText("SUBMIT");
            }
            case UPDATE -> {
                MyAsyncTask myAsyncTask = new MyAsyncTask(OperationType.FETCH);
                myAsyncTask.execute();
                clientId = (Long) map.get("client_id");
                submitBn.setText("UPDATE");
            }
        }

        roleConfig();
        addressConfig();

    }

    private void setTextFieldData(User userDTO) {

        usernameTf.setText(userDTO.getRequestedLoginName());
        firstNameTf.setText(userDTO.getFirstName());
        lastNameTf.setText(userDTO.getLastName());
        workPhoneNumberTf.setText(userDTO.getWorkPhoneNumber());
        workEmailTf.setText(userDTO.getWorkEmail());
        companyNameTf.setText(userDTO.getOfficeCompanyName());
        officeStateTf.setText(userDTO.getOfficeState());
        officeCityTf.setText(userDTO.getOfficeCity());
        officeZipTf.setText(userDTO.getOfficeZip());
        officeFaxNumberTf.setText(userDTO.getOfficeFaxNumber());
        officeAddressTa.setText(userDTO.getOfficeAddress());
        billingStateTf.setText(userDTO.getHomeState());
        billingCityTf.setText(userDTO.getHomeCity());
        billingAddressTa.setText(userDTO.getHomeAddress());
        billingZipTf.setText(userDTO.getHomeZip());
        currentUsername = userDTO.getRequestedLoginName();
        isEmailAvailable = null != userDTO.getWorkEmail();
        sharingMethodCom.getSelectionModel().select(userDTO.getPrefaredMethodForReportSharing().toUpperCase());
        String signedUsername = (String) Login.authInfo.get("username");
        if (signedUsername.equals(currentUsername)) {
            adminCb.setDisable(true);
        }
        Set<String> roles = userDTO.getRoles();
        for (String str : roles) {

            switch (str) {

                case "ROLE_DEALER" -> {
                    dealerCb.setSelected(true);
                }
                case "ROLE_PATIENT" -> {
                    patientCb.setSelected(true);

                }
                case "ROLE_DOCTOR" -> {
                    doctorCb.setSelected(true);

                }
                case "ROLE_ADMIN" -> {
                    adminCb.setSelected(true);

                }
            }
        }

        if (Objects.equals(userDTO.getOfficeAddress(), userDTO.getHomeAddress())) {
            sameAsOfficeAddress.setSelected(true);
        }

        checkAllRoleSelected();
    }

    private void addressConfig() {

        officeStateTf.textProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingStateTf.setText(t1);
            }
        });
        officeCityTf.textProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingCityTf.setText(t1);
            }

        });
        officeZipTf.textProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingZipTf.setText(t1);
            }

        });

        officeAddressTa.textProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingAddressTa.setText(t1);
            }

        });
    }

    private void roleConfig() {
        adminCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            r.setAdmin(t1);
            checkAllRoleSelected();
        });
        doctorCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            r.setDoctor(t1);
            checkAllRoleSelected();
        });
        dealerCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            r.setDealer(t1);
            checkAllRoleSelected();
        });
        patientCb.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            r.setPatient(t1);
            checkAllRoleSelected();
        });
        selectAllCb.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                String signedUsername = (String) Login.authInfo.get("username");
                if (signedUsername.equals(currentUsername)) {
                    adminCb.setDisable(true);
                }

                if (selectAllCb.isSelected()) {

                    if (!adminCb.isDisable()) {
                        adminCb.setSelected(true);
                    }

                    doctorCb.setSelected(true);
                    dealerCb.setSelected(true);
                    patientCb.setSelected(true);
                } else {
                    if (!adminCb.isDisable()){
                        adminCb.setSelected(false);

                    }
                    doctorCb.setSelected(false);
                    dealerCb.setSelected(false);
                    patientCb.setSelected(false);
                }
            }
        });
        sameAsOfficeAddress.selectedProperty().addListener((observableValue, aBoolean, isTrue) -> {

            if (officeAddressTa.getText().isEmpty()) {
                method.show_popup("Please enter office address", officeAddressTa);
                sameAsOfficeAddress.setSelected(false);
                return;
            }
            billingDetailsContainer.setDisable(isTrue);
            isSameAsAbove = isTrue;
            if (isTrue) {
                billingStateTf.setText(officeStateTf.getText());
                billingCityTf.setText(officeCityTf.getText());
                billingAddressTa.setText(officeAddressTa.getText());
                billingZipTf.setText(officeZipTf.getText());
            } else {

                billingStateTf.setText("");
                billingCityTf.setText("");
                billingAddressTa.setText("");
                billingZipTf.setText("");
            }

        });
    }

    void checkAllRoleSelected() {

        if (adminCb.isSelected() && doctorCb.isSelected() & dealerCb.isSelected() &&
                patientCb.isSelected()) {
            selectAllCb.setSelected(true);
        } else {
            selectAllCb.setSelected(false);
        }
    }

    public void cancelBnClick(ActionEvent event) {
        Stage stage = (Stage) submitBn.getScene().getWindow();
        if (null != stage && stage.isShowing()) {
            stage.close();
        }
    }

    public void submitBnClick(ActionEvent event) {

        String username = usernameTf.getText();
        String firstName = firstNameTf.getText();
        String lastName = lastNameTf.getText();
        String telephoneNumber = workPhoneNumberTf.getText();
        String email = workEmailTf.getText();
        String companyName = companyNameTf.getText();

        String officeState = officeStateTf.getText();
        String officeCity = officeCityTf.getText();
        String officeZip = officeZipTf.getText();
        String officeFax = officeFaxNumberTf.getText();
        String officeAddress = officeAddressTa.getText();

        String billingState = billingStateTf.getText();
        String billingCity = billingCityTf.getText();
        String billingZip = billingZipTf.getText();
        String billingAddress = billingAddressTa.getText();

        if (null == username && username.isEmpty()) {
            method.show_popup("Please Enter Username", usernameTf);
            return;
        } else if (null == telephoneNumber && telephoneNumber.isEmpty()) {
            method.show_popup("Please Enter Telephone Number", workPhoneNumberTf);
            return;
        } else if (telephoneNumber.length() < 9) {
            method.show_popup("Enter telephone number more then 8 digit", workPhoneNumberTf);
            return;
        } else if (isEmailAvailable) {
            if (email.isEmpty()) {
                method.show_popup("Can't remove email. but only update it.", workEmailTf);
                return;
            }

        }

        if (!sharingMethodCom.getSelectionModel().isEmpty()){
            String sharedMethod = sharingMethodCom.getSelectionModel().getSelectedItem();

            switch (sharedMethod){

                case ReportingMethods.EMAIL -> {
                    if (null == email){
                        method.show_popup("Please enter email.", workEmailTf);
                        return;
                    }
                }
                case ReportingMethods.FAX -> {

                    if (null == officeFax){
                        method.show_popup("Please enter fax number.", officeFaxNumberTf);
                        return;
                    }
                }
            }

        }

        if (null != officeFax) {
            if (officeFax.length() < 9) {
                method.show_popup("Enter fax number more then 8 digit", officeFaxNumberTf);
                return;
            }
        } else if (null == officeAddress || officeAddress.isEmpty()) {
            method.show_popup("Please Enter Office Address", officeAddressTa);
            return;
        }

        Set<String> role = new HashSet<>(4);
        if (r.isAdmin()) {
            role.add("ROLE_ADMIN");
        }
        if (r.isDoctor()) {
            role.add("ROLE_DOCTOR");
        }
        if (r.isDealer()) {
            role.add("ROLE_DEALER");
        }
        if (r.isPatient()) {
            role.add("ROLE_PATIENT");
        }

        if (role.size() < 1) {
            method.show_popup("Please Choose At least one role", patientCb);
            return;
        }

        UserDTO dm = new UserDTO();
        dm.setRequestedLoginName(username);
        dm.setFirstName(firstName);
        dm.setLastName(lastName);
        dm.setWorkPhoneNumber(telephoneNumber);
        dm.setWorkEmail(email);
        dm.setOfficeCompanyName(companyName);
        dm.setOfficeState(officeState);
        dm.setOfficeCity(officeCity);
        dm.setOfficeZip(officeZip);
        dm.setOfficeFaxNumber(officeFax);
        dm.setOfficeAddress(officeAddress);
        dm.setRoles(role);
        dm.setHomeState(billingState);
        dm.setHomeCity(billingCity);
        dm.setHomeZip(billingZip);
        dm.setHomeAddress(billingAddress);

        if (!sharingMethodCom.getSelectionModel().isEmpty()){
            dm.setPrefaredMethodForReportSharing(sharingMethodCom.getSelectionModel().getSelectedItem());
        }
        ImageView image = new ImageView(new ImageLoader().load("img/icon/warning_ic.png"));
        image.setFitWidth(45);
        image.setFitHeight(45);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning ");
        alert.setGraphic(image);
        alert.setHeaderText(Message.CONFIRMATION_MESSAGE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(Main.primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        ButtonType button = result.orElse(ButtonType.CANCEL);
        if (button == ButtonType.OK) {
            List<UserDTO> list = new ArrayList<>();
            list.add(dm);
            String dataJson = new Gson().toJson(list, List.class);
            MyAsyncTask myAsyncTask = new MyAsyncTask(userCreateOperationType);
            myAsyncTask.execute(dataJson, username);
        } else {
            alert.close();
        }
    }

    public void keyPress(KeyEvent keyEvent) {

        if (keyEvent.getCode() == KeyCode.ENTER){

            submitBnClick(null);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        OperationType operationType;

        public MyAsyncTask(OperationType operationType) {
            this.operationType = operationType;
        }

        @Override
        public void onPreExecute() {
            if (operationType != OperationType.FETCH) {
                method.hideElement(submitBn);
                progressBar.setVisible(true);

                disableFocusTraversable();

            } else {
                progressContainer.setVisible(true);
                contentContainer.setDisable(true);
            }
        }

        @Override
        public Boolean doInBackground(String... params) {
            if (operationType != OperationType.FETCH) {
                String json = params[0];
                String username = params[1];

                if (operationType == OperationType.UPDATE) {
                    switch (userCreateOperationType) {
                        case CREATE -> createProfile(json);
                        case UPDATE -> update(json);
                    }

                } else {
                    if (new CheckUsername().check(username)) {
                        Platform.runLater(() -> customDialog.showAlertBox("", "Username already exists"));
                    } else {
                        switch (userCreateOperationType) {
                            case CREATE -> createProfile(json);
                            case UPDATE -> update(json);
                        }
                    }
                }


            } else {
                getUserData();
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar, progressContainer);
            submitBn.setVisible(true);
            contentContainer.setDisable(false);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void disableFocusTraversable() {
        usernameTf.setFocusTraversable(false);
        firstNameTf.setFocusTraversable(false);
        lastNameTf.setFocusTraversable(false);
        workPhoneNumberTf.setFocusTraversable(false);
        workEmailTf.setFocusTraversable(false);
        companyNameTf.setFocusTraversable(false);
        officeStateTf.setFocusTraversable(false);
        officeCityTf.setFocusTraversable(false);
        officeZipTf.setFocusTraversable(false);
        officeFaxNumberTf.setFocusTraversable(false);
        officeAddressTa.setFocusTraversable(false);
        billingStateTf.setFocusTraversable(false);
        billingCityTf.setFocusTraversable(false);
        billingAddressTa.setFocusTraversable(false);
        billingZipTf.setFocusTraversable(false);
    }

    private void update(String json) {
        Gson gson = new Gson();
        Type userListType = new TypeToken<ArrayList<UserDTO>>() { }.getType();
        ArrayList<UserDTO> userArray = gson.fromJson(json, userListType);
        json = gson.toJson(userArray.get(0));

        try {
            String token = (String) Login.authInfo.get("token");

            HttpPut httpPut = new HttpPut(UrlConfig.getUserprofileUrl().concat(String.valueOf(clientId)));
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            StringEntity se = new StringEntity(json);
            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200){
                    UpdateUserResponse res = null;
                    try {
                        res = new Gson().fromJson(content, UpdateUserResponse.class);
                        Set<String> invalidFields = res.getData().getInvalidFields();
                        if (invalidFields.size() > 0) {
                            StringBuilder invalidsStr = new StringBuilder();
                            for (String str : invalidFields) {
                                invalidsStr.append(str).append(", ");
                            }
                            customDialog.showAlertBox("Failed", "Invalid Data : " + invalidsStr);
                        } else {
                            Platform.runLater(() -> {
                                Main.primaryStage.setUserData(true);
                                customDialog.showAlertBox("Success", content);
                                cancelBnClick(null);
                                String signedUsername = (String) Login.authInfo.get("username");
                                if (signedUsername.equals(currentUsername)) {
                                    new Main().changeScene("auth/login.fxml", "LOGIN HERE");
                                    Login.authInfo.clear();
                                }
                            });
                        }
                    } catch (JsonSyntaxException e) {

                        Platform.runLater(() -> {
                            Main.primaryStage.setUserData(true);
                            customDialog.showAlertBox("Success", content);
                            cancelBnClick(null);
                            String signedUsername = (String) Login.authInfo.get("username");
                            if (signedUsername.equals(currentUsername)) {
                                new Main().changeScene("auth/login.fxml", "LOGIN HERE");
                                Login.authInfo.clear();
                            }
                        });

                    }
                }else {
                    customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
                }

            }
        } catch (Exception e) {
            method.hideElement(progressBar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    private void getUserData() {
        String token = (String) Login.authInfo.get("token");
        try {

            HttpGet httpPut = new HttpGet(UrlConfig.getUserprofileUrl().concat(String.valueOf(clientId)));
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                User user = new Gson().fromJson(content, User.class);
                Platform.runLater(() -> {
                    setTextFieldData(user);
                });
            }

        } catch (Exception e) {
            method.hideElement(progressBar, progressContainer);
            contentContainer.setDisable(false);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
            throw new RuntimeException(e);
        }

    }

    private void createProfile(String json) {
        try {

            HttpPost httpPut = new HttpPost(UrlConfig.getProfileCreateUrl());
            httpPut.addHeader("Content-Type", "application/json");
            StringEntity se = new StringEntity(json);
            httpPut.setEntity(se);

            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();
                Platform.runLater(() -> {
                    if (statusCode == 200) {
                        CreateUsersResponse cur = new Gson().fromJson(content, CreateUsersResponse.class);
                        Set<UserDTO> failedUsers = cur.getFailedUsers();
                        if (failedUsers.size() > 0) {
                            for (UserDTO u : failedUsers) {
                                Set<String> invalidFields = u.getInvalidFields();

                                StringBuilder invalidsStr = new StringBuilder();
                                for (String str : invalidFields) {
                                    invalidsStr.append(str).append(", ");
                                }
                                if (invalidFields.size() > 0) {
                                    customDialog.showAlertBox("Failed", "Invalid Data : " + invalidsStr);
                                }
                            }
                        } else {
                            resetAllField();
                            Main.primaryStage.setUserData(true);
                            customDialog.showAlertBox("Success", cur.getMessage());
                        }

                        method.hideElement(progressBar);
                        submitBn.setVisible(true);

                    } else {
                        Main.primaryStage.setUserData(true);
                        customDialog.showAlertBox("Failed.", content);
                    }
                });
            }

        } catch (Exception e) {
            method.hideElement(progressBar);
            submitBn.setVisible(true);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again.");
            e.printStackTrace();
        }
    }

    private void resetAllField() {
        Platform.runLater(() -> {
            usernameTf.setText("");
            firstNameTf.setText("");
            lastNameTf.setText("");
            workPhoneNumberTf.setText("");
            workEmailTf.setText("");
            companyNameTf.setText("");
            officeStateTf.setText("");
            officeCityTf.setText("");
            officeZipTf.setText("");
            officeFaxNumberTf.setText("");
            officeAddressTa.setText("");
            billingStateTf.setText("");
            billingCityTf.setText("");
            billingAddressTa.setText("");
            billingZipTf.setText("");
            sameAsOfficeAddress.setSelected(false);
            adminCb.setSelected(false);
            doctorCb.setSelected(false);
            dealerCb.setSelected(false);
            patientCb.setSelected(false);
            selectAllCb.setSelected(false);
        });
    }
}
