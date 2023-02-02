package com.techwhizer.snsbiosystem.user.constant;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserSortingOptions {

    private static final Map<String, String> options = new LinkedHashMap<>(4);

    static{
        options.put("id", "id");
        options.put("Added date", "createdDate");
        options.put("Username", "userName");
        options.put("First Name", "firstName");
    }

    public static Map<String, String> getSortingOptions(){
        return options;
    }
}
