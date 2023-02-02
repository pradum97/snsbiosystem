package com.techwhizer.snsbiosystem.notice.model;

public class NoticeBoardDTO {

    private Long id;

    private Long createdDateTime;

    private Long publishOn;

    private Long expiresOn;

    private boolean forAdmins;

    private boolean forDoctors;

    private boolean forPatients;

    private boolean forDealers;

    private boolean forGuests;

    private String story;

    private String status;

    private boolean scheduled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Long getPublishOn() {
        return publishOn;
    }

    public void setPublishOn(Long publishOn) {
        this.publishOn = publishOn;
    }

    public Long getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(Long expiresOn) {
        this.expiresOn = expiresOn;
    }

    public boolean isForAdmins() {
        return forAdmins;
    }

    public void setForAdmins(boolean forAdmins) {
        this.forAdmins = forAdmins;
    }

    public boolean isForDoctors() {
        return forDoctors;
    }

    public void setForDoctors(boolean forDoctors) {
        this.forDoctors = forDoctors;
    }

    public boolean isForPatients() {
        return forPatients;
    }

    public void setForPatients(boolean forPatients) {
        this.forPatients = forPatients;
    }

    public boolean isForDealers() {
        return forDealers;
    }

    public void setForDealers(boolean forDealers) {
        this.forDealers = forDealers;
    }

    public boolean isForGuests() {
        return forGuests;
    }

    public void setForGuests(boolean forGuests) {
        this.forGuests = forGuests;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}
