package com.techwhizer.snsbiosystem.user.constant;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.user.controller.auth.Login;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class RoleOption {

    public static Map<String, String> sortingMap = getSortingOptions();

    public static String getKeyValue(String key){
        return sortingMap == null||sortingMap.isEmpty() ? getSortingOptions().get(key):sortingMap.get(key);
    }

    public static HashMap<String,String> getSortingOptions(){

        try {

            Thread.sleep(100);

            HttpClient httpClient = HttpClients.custom() .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy") .build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getRoleOptionUrl());
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie",(String) Login.authInfo.get("token"));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                HashMap<String,String> map = new Gson().fromJson(content, HashMap.class);
                return map;
            }else {
                return null;
            }
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }

    }
}
