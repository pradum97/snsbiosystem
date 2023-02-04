package com.techwhizer.snsbiosystem.util;

import com.techwhizer.snsbiosystem.Main;
import javafx.stage.FileChooser;

import java.io.File;

public class ChooseFile {

    public File chooseCSVFile(){

        FileChooser fileChooser = new FileChooser();
      //  File file = new File("D:\\sns\\sample_data");
      //  fileChooser.setInitialDirectory(file);
     //   fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        return  fileChooser.showOpenDialog(Main.primaryStage);
    }
}
