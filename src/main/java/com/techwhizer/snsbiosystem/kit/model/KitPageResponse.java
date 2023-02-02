package com.techwhizer.snsbiosystem.kit.model;

import java.util.List;

public class KitPageResponse {
    private int totalPage;
    private long totalRecord;
    private List<KitDTO> kits;
    private List<KitUsageDTO> kitUsages;

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

    public List<KitDTO> getKits() {
        return kits;
    }

    public void setKits(List<KitDTO> kits) {
        this.kits = kits;
    }

    public List<KitUsageDTO> getKitUsages() {
        return kitUsages;
    }

    public void setKitUsages(List<KitUsageDTO> kitUsages) {
        this.kitUsages = kitUsages;
    }

}
