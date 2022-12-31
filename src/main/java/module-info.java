module com.techwhizer.snsbiosystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
    requires org.json;
    requires org.apache.httpcomponents.httpclient;


    opens com.techwhizer.snsbiosystem to javafx.fxml;
    exports com.techwhizer.snsbiosystem;
}