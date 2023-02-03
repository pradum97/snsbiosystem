package com.techwhizer.snsbiosystem.kit.constants;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
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

public class TestResultOptions {

    public static Map<String, String> sortingMap = getSortingOptions();

    public static String getKeyValue(String key) {
        return sortingMap == null || sortingMap.isEmpty() ? getSortingOptions().get(key) : sortingMap.get(key);
    }

    public static String getKeyFromValue(String key) {
        String val = null;

        for (Map.Entry<String, String> entry : sortingMap.entrySet()) {
            if (entry.getValue().equals(key)) {
             val = entry.getKey();
            }
        }
        return val;
    }



    public static void main(String[] args) {

        System.out.println(getSortingOptions());
    }

    public static Map<String, String> getSortingOptions() {

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("easy").build()).build();
                URIBuilder uriBuilder = new URIBuilder(UrlConfig.getTestResultOptionUrl());
                HttpGet httpGet = new HttpGet(uriBuilder.build());
                httpGet.addHeader("Content-Type", "application/json");
                httpGet.addHeader("Cookie",(String) Login.authInfo.get("token"));
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {
                    String content = EntityUtils.toString(resEntity);

                    Map<String, String> option = new Gson().fromJson(content,Map.class);
                    return option;

                }else {
                    return null;
                }
            } catch (IOException | URISyntaxException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

}
