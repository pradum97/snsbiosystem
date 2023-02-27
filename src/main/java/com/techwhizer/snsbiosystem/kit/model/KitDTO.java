package com.techwhizer.snsbiosystem.kit.model;

public class KitDTO {

    private Long id;
    private Long clientID;
    private Long dealerID;
    private Long sterilizerID;
    private Long kitNumber;
    private String kitSerialNumber;
    private Long expiryDate;
    private Long lotNumber;
    private Integer testUsed;
    private Long testDate;
    private Integer sterilizerListNumber;
    private boolean valid = true;
    private String errorMessage;

    public String getKitSerialNumber() {
        return kitSerialNumber;
    }

    public void setKitSerialNumber(String kitSerialNumber) {
        this.kitSerialNumber = kitSerialNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public Long getDealerID() {
        return dealerID;
    }

    public void setDealerID(Long dealerID) {
        this.dealerID = dealerID;
    }

    public Long getSterilizerID() {
        return sterilizerID;
    }

    public void setSterilizerID(Long sterilizerID) {
        this.sterilizerID = sterilizerID;
    }

    public Long getKitNumber() {
        return kitNumber;
    }

    public void setKitNumber(Long kitNumber) {
        this.kitNumber = kitNumber;
    }

    public Long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Long getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(Long lotNumber) {
        this.lotNumber = lotNumber;
    }

    public Integer getTestUsed() {
        return testUsed;
    }

    public void setTestUsed(Integer testUsed) {
        this.testUsed = testUsed;
    }

    public Long getTestDate() {
        return testDate;
    }

    public void setTestDate(Long testDate) {
        this.testDate = testDate;
    }

    public Integer getSterilizerListNumber() {
        return sterilizerListNumber;
    }

    public void setSterilizerListNumber(Integer sterilizerListNumber) {
        this.sterilizerListNumber = sterilizerListNumber;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
