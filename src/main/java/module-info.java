module com.modex.modex {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens com.modex.modex to javafx.fxml;
    exports com.modex.modex;
    exports com.modex.modex.view;
    opens com.modex.modex.view to javafx.fxml;
}