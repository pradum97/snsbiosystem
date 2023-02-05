package com.techwhizer.snsbiosystem.kit.constants;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.CustomDialog;
import com.techwhizer.snsbiosystem.app.HttpStatusHandler;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.util.CommonUtility;
import com.techwhizer.snsbiosystem.util.Message;
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
import java.util.Map;

public class KitSortingOptions {

    public static Map<String, String> sortingMap = getSortingOptions();

    public static String getKeyValue(String key) {
        return sortingMap == null || sortingMap.isEmpty() ? getSortingOptions().get(key) : sortingMap.get(key);
    }

    public static Map<String, String> getSortingOptions() {

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getKitSortingOptionUrl());
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", (String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200){
                    Map<String, String> option = new Gson().fromJson(content, Map.class);
                    return option;
                } else if (statusCode == 401) {
                    new HttpStatusHandler(401);
                    return null;
                }else {
                    new CustomDialog().showAlertBox("Failed", Message.SOMETHING_WENT_WRONG);
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
