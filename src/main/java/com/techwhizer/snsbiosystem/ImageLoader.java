package com.techwhizer.snsbiosystem;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class ImageLoader {

    public ImageView getDownloadImage() {

        ImageView down_iv = new ImageView();
        down_iv.setFitHeight(17);
        down_iv.setFitWidth(17);
        down_iv.setImage(load("img/icon/download_ic.png"));
        return down_iv;
    }

    public ImageView getShareIcon() {
        ImageView down_iv = new ImageView();
        down_iv.setFitHeight(20);
        down_iv.setFitWidth(20);
        down_iv.setImage(load("img/icon/share_icon.png"));
        return down_iv;
    }

    public Image load(String imagePath) {

        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/icon/img_preview.png")));
        }
    }

    public ImageView loadImageView(String imagePath){

        try {
            return new ImageView( new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        } catch (Exception e) {
            return  new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/icon/img_preview.png"))));
        }
    }

    public Object reportLogo(String imagePath){

        try {
            return  getClass().getResourceAsStream(imagePath);
        } catch (Exception e) {
            return "img/icon/img_preview.png";
        }
    }

    public Image loadWithSize(String imagePath){

        try {
         return  new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)) , 100,100 , false , true);
        } catch (Exception e) {
            return  new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/icon/img_preview.png")));
        }
    }
}
