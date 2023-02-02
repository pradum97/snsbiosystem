package com.techwhizer.snsbiosystem.user.model;

import java.util.HashSet;
import java.util.Set;

public class UserDTO {
    private Long clientID;
    private String requestedLoginName;
    private String firstName;
    private String lastName;
    private String workPhoneNumber;
    private String workEmail;
    private Set<String> roles = new HashSet<>(4);
    private String officeCompanyName;
    private String officeAddress;
    private String officeCity;
    private String officeState;
    private String officeZip;
    private String officeFaxNumber;
    private String homeAddress;
    private String homeCity;
    private String homeState;
    private String homeZip;
    private long createdDate;
    private String prefaredMethodForReportSharing;
    private Set<String> invalidFields = new HashSet<>(5);
    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getPrefaredMethodForReportSharing() {
        return prefaredMethodForReportSharing;
    }

    public void setPrefaredMethodForReportSharing(String prefaredMethodForReportSharing) {
        this.prefaredMethodForReportSharing = prefaredMethodForReportSharing;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public String getRequestedLoginName() {
        return requestedLoginName;
    }

    public void setRequestedLoginName(String requestedLoginName) {
        this.requestedLoginName = requestedLoginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getWorkPhoneNumber() {
        return workPhoneNumber;
    }

    public void setWorkPhoneNumber(String workPhoneNumber) {
        this.workPhoneNumber = workPhoneNumber;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getOfficeCompanyName() {
        return officeCompanyName;
    }

    public void setOfficeCompanyName(String officeCompanyName) {
        this.officeCompanyName = officeCompanyName;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getOfficeCity() {
        return officeCity;
    }

    public void setOfficeCity(String officeCity) {
        this.officeCity = officeCity;
    }

    public String getOfficeState() {
        return officeState;
    }

    public void setOfficeState(String officeState) {
        this.officeState = officeState;
    }

    public String getOfficeZip() {
        return officeZip;
    }

    public void setOfficeZip(String officeZip) {
        this.officeZip = officeZip;
    }

    public String getOfficeFaxNumber() {
        return officeFaxNumber;
    }

    public void setOfficeFaxNumber(String officeFaxNumber) {
        this.officeFaxNumber = officeFaxNumber;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getHomeCity() {
        return homeCity;
    }

    public void setHomeCity(String homeCity) {
        this.homeCity = homeCity;
    }

    public String getHomeState() {
        return homeState;
    }

    public void setHomeState(String homeState) {
        this.homeState = homeState;
    }

    public String getHomeZip() {
        return homeZip;
    }

    public void setHomeZip(String homeZip) {
        this.homeZip = homeZip;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public Set<String> getInvalidFields() {
        return invalidFields;
    }

    public void setInvalidFields(Set<String> invalidFields) {
        this.invalidFields = invalidFields;
    }
}
