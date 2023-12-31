package com.techwhizer.snsbiosystem.user.controller;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.Main;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.user.constant.Roles;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.user.model.UserDTO;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.OptionalMethod;
import com.techwhizer.snsbiosystem.util.StatusCode;
import com.victorlaerte.asynctask.AsyncTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class MyProfile implements Initializable {
    public ListView<String> roleLv;
    public Label clientIdL;
    public Label usernameL,reportSharingMethod;
    public Label workPhoneNumberL;
    public Label workEmailL;
    public Label companyNameL;
    public Label officeCityL;
    public Label officeStateL;
    public Label officeZipL;
    public Label officeAddressL;
    public Label officeFaxNumberL;
    public Label homeCityL;
    public Label homeStateL;
    public Label homeZipL;
    public Label homeAddressL;
    public Label createdDateL;
    public VBox contentContainer;
    public ProgressIndicator progressBar;
    public Label fullNameL;
    public Label officeCountryL;
    public Label officeCountyL;
    public Label billingCountryL;
    public Label billingCountyL;
    private OptionalMethod method;
    private CustomDialog customDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        method = new OptionalMethod();
        customDialog = new CustomDialog();

   //     method.hideElement(roleLv);
        contentContainer.setDisable(true);
        progressBar.setVisible(true);

      Long id = (Long) Main.primaryStage.getUserData();

        MyAsyncTask myAsyncTask = new MyAsyncTask(id);
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();
    }
    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private Long id;
        public MyAsyncTask(Long id) {
            this.id = id;
        }
        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {
            getUserProfile(id);
            return false;
        }

        @Override
        public void onPostExecute(Boolean success) {
            method.hideElement(progressBar);
            contentContainer.setDisable(false);
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }

    private void getUserProfile(Long id) {

       String token = (String) Login.authInfo.get("token");

        try {
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();

            HttpGet httpPut = new HttpGet(UrlConfig.getUserprofileUrl().concat(String.valueOf(id)));
            httpPut.addHeader("Content-Type", "application/json");
            httpPut.addHeader("Cookie", token);
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                int statusCode = response.getStatusLine().getStatusCode();

                System.out.println(statusCode);

                if (statusCode == 200){
                    UserDTO user = new Gson().fromJson(content, UserDTO.class);
                    Platform.runLater(()-> setUserDetails(user));
                }else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                }

            }

        } catch (Exception e) {
            method.hideElement(progressBar);
            contentContainer.setDisable(false);
            customDialog.showAlertBox("Failed", "Something went wrong. Please try again");
            throw new RuntimeException(e);
        }

    }


    public String getRole(String key){

        Map<String,String> map = new HashMap<>();
        map.put(Roles.ROLE_ADMIN.name(),"ADMIN");
        map.put(Roles.ROLE_PATIENT.name(),"PATIENT");
        map.put(Roles.ROLE_DEALER.name(),"DEALER");
        map.put(Roles.ROLE_DOCTOR.name(),"DOCTOR");
        return map.get(key);
    }
    private void setUserDetails(UserDTO user) {

        String name;
        name = (null == user.getFirstName() || user.getFirstName().isEmpty()?"":user.getFirstName())+" "+
                (null == user.getLastName() || user.getLastName().isEmpty()?"":user.getLastName());
        fullNameL.setText(name);

        Set<String> roles = new HashSet<>();
        String roleTitle = user.getRoles().size()>1?"ROLES : ":"ROLE :";
        roles.add(roleTitle);
        for (String str:user.getRoles()){
            roles.add(getRole(str));
        }

        ObservableList<String> roleLists = FXCollections.observableArrayList(roles);
        roleLv.setItems(roleLists);

        clientIdL.setText(String.valueOf(user.getClientID()));
        usernameL.setText(user.getRequestedLoginName());
        workPhoneNumberL.setText(user.getWorkPhoneNumber());
        workEmailL.setText(user.getWorkEmail());
        companyNameL.setText(user.getOfficeCompanyName());
        officeCityL.setText(user.getOfficeCity());
        officeStateL.setText(user.getOfficeState());
        officeCountryL.setText(user.getOfficeCountry());
        officeCountyL.setText(user.getOfficeCounty());
        officeZipL.setText(user.getOfficeZip());
        officeAddressL.setText(user.getOfficeAddress());
        officeFaxNumberL.setText(user.getOfficeFaxNumber());

        reportSharingMethod.setText(user.getPrefaredMethodForReportSharing());

        homeStateL.setText(user.getHomeState());
        homeCityL.setText(user.getHomeCity());

        billingCountryL.setText(user.getHomeCountry());
        billingCountyL.setText(user.getHomeCounty());


        homeZipL.setText(user.getHomeZip());
        homeAddressL.setText(user.getHomeAddress());
        LocalDateTime localDateTime = CommonUtility.getLocalDateTimeObject(user.getCreatedDate());
        createdDateL.setText(CommonUtility.dateTimeFormator.format(localDateTime));
    }
}
