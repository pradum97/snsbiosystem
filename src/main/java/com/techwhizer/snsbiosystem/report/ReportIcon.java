package com.techwhizer.snsbiosystem.report;

import com.techwhizer.snsbiosystem.ImageLoader;
import javafx.scene.image.ImageView;

public class ReportIcon {
    static ImageLoader imageLoader;

    static {

        imageLoader = new ImageLoader();
    }

    public static ImageView getDownloadIcon() {

        ImageView down_iv = new ImageView();
        down_iv.setFitHeight(30);
        down_iv.setFitWidth(30);
        down_iv.setImage(imageLoader.load("img/icon/download_ic.png"));
        return down_iv;
    }

    public static ImageView getEmailIcon() {
        ImageView down_iv = new ImageView();
        down_iv.setFitHeight(30);
        down_iv.setFitWidth(30);
        down_iv.setImage(imageLoader.load("img/icon/email_ic.png"));
        return down_iv;
    }


    public static ImageView getFaxIcon() {

        ImageView down_iv = new ImageView();
        down_iv.setFitHeight(30);
        down_iv.setFitWidth(30);
        down_iv.setImage(imageLoader.load("img/icon/fax_ic.png"));
        return down_iv;
    }

    public static ImageView getSmsIcon() {

        ImageView down_iv = new ImageView();
        down_iv.setFitHeight(30);
        down_iv.setFitWidth(30);
        down_iv.setImage(imageLoader.load("img/icon/sms_ic.png"));
        return down_iv;
    }




}
