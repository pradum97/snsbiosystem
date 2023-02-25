package com.techwhizer.snsbiosystem.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.common.constant.CountryType;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.Message;
import com.techwhizer.snsbiosystem.util.StatusCode;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class CountryUtility extends Application {

    public static ObservableList<String> getCountryTypeFromJsonArray(JsonArray jsonArray , CountryType countryType) {

        ObservableList<String> countryList = FXCollections.observableArrayList();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jo = jsonArray.get(i).getAsJsonObject();

            if (countryType == CountryType.COUNTRY_NAME) {
                countryList.add(jo.get("name").getAsString());
            } else {
                countryList.add(jo.get("phoneCode").getAsString());
            }
        }
        return countryList;
    }

    public static JsonArray getCountryJson() {

        JsonArray jsonArray = new JsonArray();

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder param = new URIBuilder(UrlConfig.getCountriesUrl());
            HttpGet httpGet = new HttpGet(param.build());

            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == StatusCode.OK) {
                    return JsonParser.parseString(content).getAsJsonArray();

                } else if (statusCode == StatusCode.UNAUTHORISED) {
                    new HttpStatusHandler(StatusCode.UNAUTHORISED);
                    return jsonArray;
                } else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                    return jsonArray;
                }

            }else  {
                return jsonArray;
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}
