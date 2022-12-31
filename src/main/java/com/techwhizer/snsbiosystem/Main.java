package com.techwhizer.snsbiosystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends Application {

    static String username = "snsadmin";
    static String password = "snsadmin";
    static String to;
    static Map<String, Object> userMap = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public static void main(String[] args) {

        String url = "http://localhost:8080/v1/auth/authenticate";

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("easy")
                        .build()).build();

        HttpGet httpPost = new HttpGet(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", getBasicAuthenticationHeader(username, password));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);
                Header[] headers = response.getHeaders("Set-Cookie");
                String token = headers[0].getValue();

                to = token;

                userMap.put("token",token);
                userMap.put("basic_auth_header",getBasicAuthenticationHeader(username, password));
                userMap.put("user_data",content);
                sendFile(token);

                //   System.out.println(content);
                //  JSONObject jo = new JSONObject(content);

            }

        } catch (Exception e) {

        }
    }

    private static void sendFile(String token) {

        String url = "http://localhost:8080/v2/admin/sterilizer/preview-csv";


        File file = new File("D:\\sns\\Sorted Data\\sterilizerSortData.csv");

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(file);
            builder.addPart("file", fileBody);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null && response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(resEntity,"UTF-8");


                JSONArray jsonArray = new JSONArray(content);
                System.out.println(jsonArray.get(0));

                for (int i = 0; i < jsonArray.length(); i++) {
                  JSONObject object = jsonArray.getJSONObject(i);

                  String clientType = object.get("clientID").toString();
                  System.out.println(clientType);

                   // System.out.println(object);

                   // System.out.println(object.get("clientID").getClass().getName());


                }

            }
        } catch (IOException e) {

            System.out.println(e.getMessage());
        }

    }

}