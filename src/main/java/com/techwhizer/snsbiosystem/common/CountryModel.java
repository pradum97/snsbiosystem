package com.techwhizer.snsbiosystem.common;

import java.util.List;

public class CountryModel {
    private String name;
    private String phoneCode;
    private String currency;
    private String region;
    private List<String> timeZones;
    private String cca3;

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getPhoneCode() { return phoneCode; }
    public void setPhoneCode(String value) { this.phoneCode = value; }

    public String getCurrency() { return currency; }
    public void setCurrency(String value) { this.currency = value; }

    public String getRegion() { return region; }
    public void setRegion(String value) { this.region = value; }

    public List<String> getTimeZones() {
        return timeZones;
    }

    public void setTimeZones(List<String> timeZones) {
        this.timeZones = timeZones;
    }

    public String getCca3() { return cca3; }
    public void setCca3(String value) { this.cca3 = value; }
}
