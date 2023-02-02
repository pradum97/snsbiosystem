package com.techwhizer.snsbiosystem.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SaveReport {

    public boolean save(InputStream inputStream,String path){

        if(path == null){
            return false;
        }else{
            try {
                FileOutputStream  outputStream = new FileOutputStream(path);
                outputStream.write(inputStream.readAllBytes());
                outputStream.close();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
