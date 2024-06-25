module net.rchronicler.kibby {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.slf4j;
    requires java.prefs;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;


    opens net.rchronicler.kibby to javafx.fxml;
    opens net.rchronicler.kibby.controllers to javafx.fxml;
    exports net.rchronicler.kibby;
}