module com.peko_xmpp {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.peko_xmpp to javafx.fxml;
    exports com.peko_xmpp;
}
