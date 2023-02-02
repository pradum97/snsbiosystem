package com.techwhizer.snsbiosystem.report.constent;

public class ReportDownloadPath {
    public  final static String FILE_PATH () {
       return System.getProperty("user.home") + "\\Downloads";
    }

    public  final static String FILE_PATH (String fileName){

        String path = System.getProperty("user.home") + "\\Downloads\\";
        return path+fileName;
    }
}
