package com.techwhizer.snsbiosystem.kit.model;
public class KitUsageDTO {
    private Long id;
    private Long kitNumber;
    private Long testDate;
    private Long sterilizerID;
    private Integer sterilizerListNumber;
    private String sterilizerType;
    private String sterilizerBrand;
    private String sterilizerSerialNumber;
    private String testResult;
    private String status;
    private Long clientID;
    private boolean valid = true;
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKitNumber() {
        return kitNumber;
    }

    public void setKitNumber(Long kitNumber) {
        this.kitNumber = kitNumber;
    }

    public Long getTestDate() {
        return testDate;
    }

    public void setTestDate(Long testDate) {
        this.testDate = testDate;
    }

    public Long getSterilizerID() {
        return sterilizerID;
    }

    public void setSterilizerID(Long sterilizerID) {
        this.sterilizerID = sterilizerID;
    }

    public Integer getSterilizerListNumber() {
        return sterilizerListNumber;
    }

    public void setSterilizerListNumber(Integer sterilizerListNumber) {
        this.sterilizerListNumber = sterilizerListNumber;
    }

    public String getSterilizerType() {
        return sterilizerType;
    }

    public void setSterilizerType(String sterilizerType) {
        this.sterilizerType = sterilizerType;
    }

    public String getSterilizerBrand() {
        return sterilizerBrand;
    }

    public void setSterilizerBrand(String sterilizerBrand) {
        this.sterilizerBrand = sterilizerBrand;
    }

    public String getSterilizerSerialNumber() {
        return sterilizerSerialNumber;
    }

    public void setSterilizerSerialNumber(String sterilizerSerialNumber) {
        this.sterilizerSerialNumber = sterilizerSerialNumber;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
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
