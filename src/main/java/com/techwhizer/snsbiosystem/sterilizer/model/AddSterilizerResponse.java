package com.techwhizer.snsbiosystem.sterilizer.model;

import java.util.LinkedList;
import java.util.List;

public class AddSterilizerResponse {
    private List<SterilizerDTO> added = new LinkedList<>();
    private List<SterilizerDTO> invalidData = new LinkedList<>();

    public List<SterilizerDTO> getAdded() {
        return added;
    }

    public void setAdded(List<SterilizerDTO> added) {
        this.added = added;
    }

    public void addSaved(SterilizerDTO added) {
        this.added.add(added);
    }

    public List<SterilizerDTO> getInvalidData() {
        return invalidData;
    }

    public void setInvalidData(List<SterilizerDTO> invalidData) {
        this.invalidData = invalidData;
    }

    public void addInvalidData(SterilizerDTO invalidData) {
        this.invalidData.add(invalidData);
    }

}
