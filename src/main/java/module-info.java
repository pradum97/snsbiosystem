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
    requires java.sql;


    opens com.techwhizer.snsbiosystem to javafx.fxml;
    exports com.techwhizer.snsbiosystem;
    opens com.techwhizer.snsbiosystem.custom_enum to javafx.fxml;
    exports com.techwhizer.snsbiosystem.custom_enum;

    opens com.techwhizer.snsbiosystem.dialog to javafx.fxml;
    exports com.techwhizer.snsbiosystem.dialog;

    opens com.techwhizer.snsbiosystem.notice.controller to javafx.fxml;
    exports com.techwhizer.snsbiosystem.notice.controller;

    opens com.techwhizer.snsbiosystem.notice.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.notice.model;


    opens com.techwhizer.snsbiosystem.user.controller.auth to com.google.gson;
    exports com.techwhizer.snsbiosystem.user.controller.auth;

    opens com.techwhizer.snsbiosystem.sterilizer.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.sterilizer.model;

    opens com.techwhizer.snsbiosystem.kit.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.kit.model ;

    opens com.techwhizer.snsbiosystem.kit.controller to com.google.gson;
    exports com.techwhizer.snsbiosystem.kit.controller ;

    exports com.techwhizer.snsbiosystem.user.model;
    opens com.techwhizer.snsbiosystem.user.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.user.controller;
    opens com.techwhizer.snsbiosystem.user.controller to com.google.gson, javafx.fxml;
    exports com.techwhizer.snsbiosystem.dashboard.controller;
    opens com.techwhizer.snsbiosystem.dashboard.controller to com.google.gson;
    exports com.techwhizer.snsbiosystem.dashboard.model;
    opens com.techwhizer.snsbiosystem.dashboard.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.sterilizer.controller;
    opens com.techwhizer.snsbiosystem.sterilizer.controller to com.google.gson;
    exports com.techwhizer.snsbiosystem.kit.controller.kitusage;
    opens com.techwhizer.snsbiosystem.kit.controller.kitusage to com.google.gson;


}