module com.axono {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.axono to javafx.fxml;
    exports com.axono;
}
