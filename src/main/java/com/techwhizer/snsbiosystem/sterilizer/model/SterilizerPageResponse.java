package com.techwhizer.snsbiosystem.sterilizer.model;


import java.util.List;

public class SterilizerPageResponse {
    private int totalPage;
    private long totalRecord;
    private List<SterilizerTableView> sterilizers;

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public List<SterilizerTableView> getSterilizers() {
        return sterilizers;
    }

    public void setSterilizers(List<SterilizerTableView> sterilizers) {
        this.sterilizers = sterilizers;
    }

}
