package com.techwhizer.snsbiosystem;

import com.google.gson.Gson;
import com.techwhizer.snsbiosystem.app.UrlConfig;
import com.techwhizer.snsbiosystem.pagination.PaginationUtil;
import com.techwhizer.snsbiosystem.sterilizer.constants.SterilizerSortingOptions;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerPageResponse;
import com.techwhizer.snsbiosystem.sterilizer.model.SterilizerTableView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TestPagination implements Initializable {
    public Pagination pagination;
    public ComboBox<Integer> rowSizeCom;
    public ComboBox<String> sortingCom;

    int SIZE = 50;

    String token = """
                        
AuthToken=d/d5VECDlIyAEGdTnIfU1sbXW+IjoSYcnj7Bc3AxF8AmlO9TWIZZta6Xx0eEki7a9GoT/De4js/82X1PcrMTuCHrqEb0EGrHzWQTLDaQeLRCMQlTM7PDyxSwt5yDfLow7xZomqDndvOVupONz0+syntjH4LzuL4o/wWew9QlDcbHyS4yuUd6rryuhs3Ti6qq0kI7j5DY3b0tChdfdbmhLqSeITT8cj2t7D87sXn0xtxKNe0RWH1dW7ygDNloCb3N; Path=/; Domain=localhost; HttpOnly;            """;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        sortingComboboxConfig();
        paginationConfig();

    }

    private void sortingComboboxConfig() {

        sortingCom.setItems(FXCollections.observableArrayList(SterilizerSortingOptions.sortingMap.keySet()));
        sortingCom.valueProperty().addListener((observableValue, s, t1) -> {

            System.out.println("chane......");
            String filedName = SterilizerSortingOptions.getKeyValue(t1);
            String order = "asc";

            String sort = filedName+","+order;
            rowSizeComboBox(sort);
        });
        sortingCom.getSelectionModel().selectFirst();
    }


    private void rowSizeComboBox(String sort) {
        pagination.setCurrentPageIndex(0);
        rowSizeCom.setItems(PaginationUtil.rowSize);
        rowSizeCom.valueProperty().addListener((observableValue, integer, t1) -> {
            getAllSterilizer(PaginationUtil.DEFAULT_PAGE_INDEX, t1,sort);
        });
        rowSizeCom.getSelectionModel().select(PaginationUtil.DEFAULT_PAGE_SIZE);
    }

    private void getAllSterilizer(int page, int size,String sort) {

        System.out.println("sort:"+sort);

        try {
            Thread.sleep(100);
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec("easy").build()).build();
            URIBuilder uriBuilder = new URIBuilder(UrlConfig.getAddSterilizerUrl());
            uriBuilder.setParameter("page", String.valueOf(page));
            uriBuilder.setParameter("size", String.valueOf(size));
            uriBuilder.setParameter("sort",sort);
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cookie", token);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String content = EntityUtils.toString(resEntity);

                SterilizerPageResponse pageResponse = new Gson().fromJson(content, SterilizerPageResponse.class);
                List<SterilizerTableView> str = pageResponse.getSterilizers();

                System.out.println("page:" + page);
                System.out.println(str.size());

                int totalPage = pageResponse.getTotalPage();
                createPagination(totalPage);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void paginationConfig() {
        pagination.currentPageIndexProperty().addListener(
                (observable1, oldValue1, newIndex) -> {
                    int currentIndex = newIndex.intValue();

                    String filedName = SterilizerSortingOptions.getKeyValue(sortingCom.getSelectionModel().getSelectedItem());
                    String order = "asc";

                    String sort = filedName+","+order;

                    getAllSterilizer(currentIndex, SIZE,sort);
                });
    }

    private void createPagination(int totalPage) {
        Platform.runLater(() -> {
            pagination.setPageCount(totalPage);
        });
    }
}
