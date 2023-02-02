package com.techwhizer.snsbiosystem;

import javafx.scene.Scene;

import java.util.Objects;

public class CssLoader {

    public  void set(Scene scene,String cssFileName){

        scene.getStylesheets().add(Objects.requireNonNull(CssLoader.class.getResource("css/"+cssFileName)).toExternalForm());
    }
}
