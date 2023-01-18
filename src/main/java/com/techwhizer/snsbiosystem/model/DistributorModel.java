package com.techwhizer.snsbiosystem.model;

import java.util.ArrayList;

public class DistributorModel {
    private String requestedLoginName;
    private String firstName;
    private String lastName;
    private String workPhoneNumber;
    private String workEmail;
    ArrayList<String> roles ;
    private String officeCompanyName;
    private String officeAddress;
    private String officeCity;
    private String officeState;
    private String officeZip;
    private String homeAddress;
    private String homeCity;
    private String homeState;
    private String homeZip;
    private float createdDate;
   private ArrayList<DistributorModel> invalidFields ;
    private boolean valid;

    // for invalid field
    public DistributorModel(String requestedLoginName, String firstName, String lastName, String workPhoneNumber, String workEmail, ArrayList<String> roles, String officeCompanyName, String officeAddress, String officeCity, String officeState, String officeZip, String homeAddress, String homeCity,
                            String homeState, String homeZip, float createdDate,boolean valid) {
        this.requestedLoginName = requestedLoginName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workPhoneNumber = workPhoneNumber;
        this.workEmail = workEmail;
        this.roles = roles;
        this.officeCompanyName = officeCompanyName;
        this.officeAddress = officeAddress;
        this.officeCity = officeCity;
        this.officeState = officeState;
        this.officeZip = officeZip;
        this.homeAddress = homeAddress;
        this.homeCity = homeCity;
        this.homeState = homeState;
        this.homeZip = homeZip;
        this.createdDate = createdDate;
        this.valid = valid;
    }

    public DistributorModel(String requestedLoginName, String firstName, String lastName, String workPhoneNumber, String workEmail, ArrayList<String> roles, String officeCompanyName, String officeAddress, String officeCity, String officeState, String officeZip, String homeAddress, String homeCity,
                            String homeState, String homeZip, float createdDate, ArrayList<DistributorModel> invalidFields, boolean valid) {
        this.requestedLoginName = requestedLoginName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.workPhoneNumber = workPhoneNumber;
        this.workEmail = workEmail;
        this.roles = roles;
        this.officeCompanyName = officeCompanyName;
        this.officeAddress = officeAddress;
        this.officeCity = officeCity;
        this.officeState = officeState;
        this.officeZip = officeZip;
        this.homeAddress = homeAddress;
        this.homeCity = homeCity;
        this.homeState = homeState;
        this.homeZip = homeZip;
        this.createdDate = createdDate;
        this.invalidFields = invalidFields;
        this.valid = valid;
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<String> roles) {
        this.roles = roles;
    }

    public ArrayList<DistributorModel> getInvalidFields() {
        return invalidFields;
    }

    public void setInvalidFields(ArrayList<DistributorModel> invalidFields) {
        this.invalidFields = invalidFields;
    }

    public boolean isValid() {
        return valid;
    }

    // Getter Methods

    public String getRequestedLoginName() {
        return requestedLoginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getWorkPhoneNumber() {
        return workPhoneNumber;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public String getOfficeCompanyName() {
        return officeCompanyName;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public String getOfficeCity() {
        return officeCity;
    }

    public String getOfficeState() {
        return officeState;
    }

    public String getOfficeZip() {
        return officeZip;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public String getHomeCity() {
        return homeCity;
    }

    public String getHomeState() {
        return homeState;
    }

    public String getHomeZip() {
        return homeZip;
    }

    public float getCreatedDate() {
        return createdDate;
    }

    public boolean getValid() {
        return valid;
    }

    // Setter Methods

    public void setRequestedLoginName( String requestedLoginName ) {
        this.requestedLoginName = requestedLoginName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    public void setWorkPhoneNumber( String workPhoneNumber ) {
        this.workPhoneNumber = workPhoneNumber;
    }

    public void setWorkEmail( String workEmail ) {
        this.workEmail = workEmail;
    }

    public void setOfficeCompanyName( String officeCompanyName ) {
        this.officeCompanyName = officeCompanyName;
    }

    public void setOfficeAddress( String officeAddress ) {
        this.officeAddress = officeAddress;
    }

    public void setOfficeCity( String officeCity ) {
        this.officeCity = officeCity;
    }

    public void setOfficeState( String officeState ) {
        this.officeState = officeState;
    }

    public void setOfficeZip( String officeZip ) {
        this.officeZip = officeZip;
    }

    public void setHomeAddress( String homeAddress ) {
        this.homeAddress = homeAddress;
    }

    public void setHomeCity( String homeCity ) {
        this.homeCity = homeCity;
    }

    public void setHomeState( String homeState ) {
        this.homeState = homeState;
    }

    public void setHomeZip( String homeZip ) {
        this.homeZip = homeZip;
    }

    public void setCreatedDate( float createdDate ) {
        this.createdDate = createdDate;
    }

    public void setValid( boolean valid ) {
        this.valid = valid;
    }
}
