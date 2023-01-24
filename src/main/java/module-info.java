module com.techwhizer.snsbiosystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
    requires org.apache.httpcomponents.httpclient;
    requires jfx.asynctask;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.logging;


    opens com.techwhizer.snsbiosystem to javafx.fxml;
    exports com.techwhizer.snsbiosystem;
    opens com.techwhizer.snsbiosystem.custom_enum to javafx.fxml;
    exports com.techwhizer.snsbiosystem.custom_enum;


    opens com.techwhizer.snsbiosystem.controller.auth to com.google.gson;
    exports com.techwhizer.snsbiosystem.controller.auth;

    opens com.techwhizer.snsbiosystem.controller.dashboard to com.google.gson;
    exports com.techwhizer.snsbiosystem.controller.dashboard;

    opens com.techwhizer.snsbiosystem.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.model;

    exports com.techwhizer.snsbiosystem.controller.profile;
    opens com.techwhizer.snsbiosystem.controller.profile to com.google.gson, javafx.fxml;


}