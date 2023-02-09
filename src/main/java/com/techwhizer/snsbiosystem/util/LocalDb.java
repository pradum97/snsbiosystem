package com.techwhizer.snsbiosystem.util;

import com.techwhizer.snsbiosystem.kit.constants.KitUsageSearchType;
import com.techwhizer.snsbiosystem.user.constant.ReportingMethods;
import com.techwhizer.snsbiosystem.sterilizer.constants.SterilizerType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashSet;
import java.util.Set;

public class LocalDb {

    public ObservableList<String> getReportSharingType() {
        return FXCollections.observableArrayList(ReportingMethods.EMAIL,ReportingMethods.FAX,ReportingMethods.MAIL);
    }

    public ObservableList<String> getKitUsageSearchType() {
        return FXCollections.observableArrayList(KitUsageSearchType.ID,KitUsageSearchType.KIT_NUMBER,KitUsageSearchType.STERILIZER_ID,
                KitUsageSearchType.STERILIZER_LIST_NUMBER,KitUsageSearchType.STERILIZER_TYPE,KitUsageSearchType.STERILIZER_SERIAL_NUMBER);
    }

    public ObservableList<String> getSterilizerType() {

        Set<String> type = new HashSet<>();
        type.add(SterilizerType.HEAT);
        type.add(SterilizerType.INFORMATION);
        type.add(SterilizerType.STEAM);
        type.add(SterilizerType.CHEMICLAVE);
        type.add(SterilizerType.VAPOR);
        type.add(SterilizerType.LONGS);
        type.add(SterilizerType.FLASH);
        return FXCollections.observableArrayList(type);
    }


}
