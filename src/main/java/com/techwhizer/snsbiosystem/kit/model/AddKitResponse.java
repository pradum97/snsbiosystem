package com.techwhizer.snsbiosystem.kit.model;

import java.util.LinkedList;
import java.util.List;

public class AddKitResponse {
    List<KitDTO> added = new LinkedList<>();
    List<KitDTO> invalidKits = new LinkedList<>();

    public List<KitDTO> getAdded() {
        return added;
    }

    public void setAdded(List<KitDTO> added) {
        this.added = added;
    }

    public List<KitDTO> getInvalidKits() {
        return invalidKits;
    }

    public void setInvalidKits(List<KitDTO> invalidKits) {
        this.invalidKits = invalidKits;
    }
}
