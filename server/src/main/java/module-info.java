module com.whisper.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    opens com.whisper.server to javafx.fxml;
    exports com.whisper.server;
}