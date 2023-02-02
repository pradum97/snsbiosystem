package com.techwhizer.snsbiosystem.sterilizer.model;

public class SterilizerDTO {

    private Long clientID;
    private Long sterilizerID;
    private Integer sterilizerListNumber;
    private String sterilizerType;
    private String sterilizerBrand;
    private String sterilizerSerialNumber;
    private boolean valid  = true;
    private String errorMessage;

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
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
