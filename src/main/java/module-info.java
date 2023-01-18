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




    opens com.techwhizer.snsbiosystem to javafx.fxml;
    exports com.techwhizer.snsbiosystem;

    opens com.techwhizer.snsbiosystem.controller.dashboard.account to javafx.fxml;
    exports com.techwhizer.snsbiosystem.controller.dashboard.account ;

    opens com.techwhizer.snsbiosystem.controller.auth to com.google.gson;
    exports com.techwhizer.snsbiosystem.controller.auth;

    opens com.techwhizer.snsbiosystem.controller.update.user to com.google.gson;
    exports com.techwhizer.snsbiosystem.controller.update.user;

    opens com.techwhizer.snsbiosystem.model to com.google.gson;
    exports com.techwhizer.snsbiosystem.model to com.google.gson;



}