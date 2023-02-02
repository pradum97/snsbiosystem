package com.techwhizer.snsbiosystem.pagination;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PaginationUtil {

    public final static Integer DEFAULT_PAGE_SIZE = 25;
    public final static Integer DEFAULT_PAGE_INDEX = 0;
    public final static ObservableList<Integer> rowSize = FXCollections.observableArrayList(25,50,75,100);
}
