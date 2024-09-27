module ca.cmpt213.webclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens ca.cmpt213.webclient to com.google.gson;
    exports ca.cmpt213.webclient;
}