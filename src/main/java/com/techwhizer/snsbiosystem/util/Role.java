package com.techwhizer.snsbiosystem.util;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.controller.auth.Login;
import com.techwhizer.snsbiosystem.model.RolesModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;

public class Role {

    public RolesModel getRole(){

        try {

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

                HashMap<String,Object> map = new Gson().fromJson(content, HashMap.class);
                RolesModel rm = new RolesModel();

                rm.setAdmin(map.get("Admin").toString());
                rm.setDealer(map.get("Dealer").toString());
                rm.setDoctor(map.get("Doctor").toString());
                rm.setPatient(map.get("Patient").toString());

                return rm;
            }else {
                return null;
            }
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }

    }
}
