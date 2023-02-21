package com.techwhizer.snsbiosystem.user.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.ImageLoader;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.common.CountryUtility;
import com.techwhizer.snsbiosystem.common.constant.CountryType;
import com.techwhizer.snsbiosystem.custom_enum.OperationType;
import com.techwhizer.snsbiosystem.user.constant.ReportingMethods;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.CreateUsersResponse;
import com.techwhizer.snsbiosystem.user.model.RoleConfigModel;
import com.techwhizer.snsbiosystem.user.model.UpdateUserResponse;
import com.techwhizer.snsbiosystem.user.model.UserDTO;
import com.techwhizer.snsbiosystem.user.util.CheckUsername;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.nio.charset.StandardCharsets;
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
    public VBox sharingMethodContainer;
    public TextField officeCountyTf;
    public TextField billingCountyTf;
    public ComboBox<String> countryCodeCom;
    public ComboBox<String> officeCountryCom;
    public ComboBox<String> billingCountryCom;
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
        method.hideElement(sharingMethodContainer);
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

                MyAsyncTask myAsyncTask = new MyAsyncTask(OperationType.FETCH_COUNTRY);
                myAsyncTask.execute();
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

    private void setTextFieldData(UserDTO userDTO) {

        usernameTf.setText(userDTO.getRequestedLoginName() == null ? "" : userDTO.getRequestedLoginName());
        firstNameTf.setText(null == userDTO.getFirstName() ? "" : userDTO.getFirstName());
        lastNameTf.setText(null == userDTO.getLastName() ? "" : userDTO.getLastName());

        workEmailTf.setText(null == userDTO.getWorkEmail() ? "" : userDTO.getWorkEmail());
        companyNameTf.setText(null == userDTO.getOfficeCompanyName() ? "" : userDTO.getOfficeCompanyName());
        officeStateTf.setText(null == userDTO.getOfficeState() ? "" : userDTO.getOfficeState());

        if (null != userDTO.getOfficeCountry()) {
            officeCountryCom.getSelectionModel().select(userDTO.getOfficeCountry());
        }


        officeCountyTf.setText(null == userDTO.getOfficeCounty() ? "" : userDTO.getOfficeCounty());
        officeCityTf.setText(null == userDTO.getOfficeCity() ? "" : userDTO.getOfficeCity());
        officeZipTf.setText(null == userDTO.getOfficeZip() ? "" : userDTO.getOfficeZip());
        officeFaxNumberTf.setText(null == userDTO.getOfficeFaxNumber() ? "" : userDTO.getOfficeFaxNumber());
        officeAddressTa.setText(null == userDTO.getOfficeAddress() ? "" : userDTO.getOfficeAddress());
        billingStateTf.setText(null == userDTO.getHomeState() ? "" : userDTO.getHomeState());

        if (null != userDTO.getHomeCountry()) {
            billingCountryCom.getSelectionModel().select(userDTO.getHomeCountry());
        }

        billingCountyTf.setText(null == userDTO.getHomeCounty() ? "" : userDTO.getHomeCounty());

        billingCityTf.setText(null == userDTO.getHomeCity() ? "" : userDTO.getHomeCity());
        billingAddressTa.setText(null == userDTO.getHomeAddress() ? "" : userDTO.getHomeAddress());
        billingZipTf.setText(null == userDTO.getHomeZip() ? "" : userDTO.getHomeZip());
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

        String phoneNumWithCode = userDTO.getWorkPhoneNumber();

        try {
            String[] phoneList = phoneNumWithCode.split("-",2);
            String phoneCode = phoneList[0];
            String phoneNumber = phoneList[1];
            countryCodeCom.getSelectionModel().select(phoneCode);
            workPhoneNumberTf.setText(phoneNumber);
        } catch (Exception e) {
            workPhoneNumberTf.setText(phoneNumWithCode);
        }
    }

    private void addressConfig() {

        officeStateTf.textProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingStateTf.setText(t1);
            }
        });

        officeCountryCom.valueProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingCountryCom.getSelectionModel().select(t1);
            }
        });

        officeCountyTf.textProperty().addListener((observableValue, s, t1) -> {
            if (isSameAsAbove) {
                billingCountyTf.setText(t1);
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

                billingCountyTf.setText(officeCountyTf.getText());

                billingCountryCom.getSelectionModel().select(officeCountryCom.getSelectionModel().getSelectedItem());

            } else {

                billingStateTf.setText("");
                billingCityTf.setText("");
                billingAddressTa.setText("");
                billingZipTf.setText("");

                billingCountyTf.setText("");
                billingCountryCom.getSelectionModel().clearSelection();
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
        String phoneCode = countryCodeCom.getSelectionModel().getSelectedItem();
        String telephoneNumber = workPhoneNumberTf.getText();
        String email = workEmailTf.getText();
        String companyName = companyNameTf.getText();

        String officeState = officeStateTf.getText();
        String officeCity = officeCityTf.getText();
        String officeZip = officeZipTf.getText();
        String officeFax = officeFaxNumberTf.getText();
        String officeAddress = officeAddressTa.getText();

        String officeCountry = officeCountryCom.getSelectionModel().getSelectedItem();
        String officeCounty = officeCountyTf.getText();

        String billingState = billingStateTf.getText();
        String billingCity = billingCityTf.getText();
        String billingZip = billingZipTf.getText();
        String billingAddress = billingAddressTa.getText();

        String billingCountry = billingCountryCom.getSelectionModel().getSelectedItem();
        String billingCounty = billingCountyTf.getText();

        if (username.isEmpty()) {
            method.show_popup("Please Enter Username", usernameTf);
            return;
        }

        if (userCreateOperationType == OperationType.UPDATE) {
            if (null == phoneCode) {
                method.show_popup("Please Select country code", countryCodeCom);
                return;
            }
        }

        if (telephoneNumber.isEmpty()) {
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
                    if (userCreateOperationType == OperationType.CREATE){
                        if (email.isEmpty()){
                            method.show_popup("Please enter email.", workEmailTf);
                            return;
                        }

                    }else {
                        if (null == email){
                            method.show_popup("Please enter email.", workEmailTf);
                            return;
                        }
                    }
                }
                case ReportingMethods.FAX -> {

                    if (userCreateOperationType == OperationType.CREATE){
                        if (officeFax.isEmpty()) {
                            method.show_popup("Please enter fax number.", officeFaxNumberTf);
                            return;
                        }
                    }else {
                        if (null == officeFax) {
                            method.show_popup("Please enter fax number.", officeFaxNumberTf);
                            return;
                        }
                    }
                }
            }

        }

        if (!officeFax.isEmpty() ) {
            if (officeFax.length() < 9) {
                method.show_popup("Enter fax number more then 8 digit", officeFaxNumberTf);
                return;
            }
        }
        if (null == officeAddress || officeAddress.isEmpty()) {
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
        dm.setWorkPhoneNumber(phoneCode + "-" + telephoneNumber);
        dm.setWorkEmail(email);
        dm.setOfficeCompanyName(companyName);
        dm.setOfficeState(officeState);

        dm.setOfficeCountry(officeCountry);
        dm.setOfficeCounty(officeCounty);

        dm.setOfficeCity(officeCity);
        dm.setOfficeZip(officeZip);
        dm.setOfficeFaxNumber(officeFax);
        dm.setOfficeAddress(officeAddress);
        dm.setRoles(role);
        dm.setHomeState(billingState);
        dm.setHomeCity(billingCity);
        dm.setHomeZip(billingZip);
        dm.setHomeAddress(billingAddress);
        dm.setHomeCountry(billingCountry);
        dm.setHomeCounty(billingCounty);

        if (!sharingMethodCom.getSelectionModel().isEmpty()) {
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

            } else {
                progressContainer.setVisible(true);
                contentContainer.setDisable(true);
            }
        }

        @Override
        public Boolean doInBackground(String... params) {
            getCountryData();

            if (operationType != OperationType.FETCH) {

                if (params.length > 0) {
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
                }

            } else {
                getUserData();
            }
            return false;
        }

        void getCountryData() {

            ObservableList<String> countryNameList = CountryUtility.getCountryName(CountryType.COUNTRY_NAME);
            ObservableList<String> countryPhoneCode = CountryUtility.getCountryName(CountryType.COUNTRY_CODE);

            Platform.runLater(() -> {
                countryCodeCom.setItems(countryPhoneCode);
                officeCountryCom.setItems(countryNameList);
                billingCountryCom.setItems(countryNameList);


                if (userCreateOperationType == OperationType.CREATE) {

                    countryCodeCom.getSelectionModel().select(CommonUtility.DEFAULT_PHONE_CODE_SELECTION);
                    officeCountryCom.getSelectionModel().select(CommonUtility.DEFAULT_COUNTRY_SELECTION);
                    billingCountryCom.getSelectionModel().select(CommonUtility.DEFAULT_COUNTRY_SELECTION);

                }
            });
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
            HttpPut httpPut = new HttpPut(UrlConfig.getUserprofileUrl().concat(String.valueOf(clientId)));
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
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
                            }
                        });

                    }
                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
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

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    UserDTO user = new Gson().fromJson(content, UserDTO.class);
                    Platform.runLater(() -> {
                        setTextFieldData(user);
                    });
                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                }
            }

        } catch (Exception e) {
            method.hideElement(progressBar, progressContainer);
            contentContainer.setDisable(false);
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        }

    }

    private void createProfile(String json) {

        try {
            HttpPost httpPut = new HttpPost(UrlConfig.getProfileCreateUrl());
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", (String) Login.authInfo.get("token"));
            StringEntity se = new StringEntity(json, StandardCharsets.UTF_8);
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

                    } else if (statusCode == StatusCode.UNAUTHORISED) {
                        new HttpStatusHandler(StatusCode.UNAUTHORISED);
                    } else {
                        Main.primaryStage.setUserData(true);
                        customDialog.showAlertBox("Failed.", content);
                    }
                });
            }

        } catch (Exception e) {
            method.hideElement(progressBar);
            submitBn.setVisible(true);
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
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
