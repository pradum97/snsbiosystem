package com.techwhizer.snsbiosystem.kit.model;

import java.util.LinkedList;
import java.util.List;

public class AddKitUsagesResponse {
    List<KitUsageDTO> added = new LinkedList<>();
    List<KitUsageDTO> invalidKits = new LinkedList<>();

    public List<KitUsageDTO> getAdded() {
        return added;
    }

    public void setAdded(List<KitUsageDTO> added) {
        this.added = added;
    }

    public List<KitUsageDTO> getInvalidKits() {
        return invalidKits;
    }

    public void setInvalidKits(List<KitUsageDTO> invalidKits) {
        this.invalidKits = invalidKits;
    }
}
