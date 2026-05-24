module com.robotvacuum {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.robotvacuum to javafx.fxml;
    opens com.robotvacuum.controller to javafx.fxml;
    opens com.robotvacuum.model to javafx.fxml;
    opens com.robotvacuum.view to javafx.fxml;

    exports com.robotvacuum;
    exports com.robotvacuum.controller;
    exports com.robotvacuum.model;
    exports com.robotvacuum.view;
    exports com.robotvacuum.util;
}
