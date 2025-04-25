module com.example.app {
// JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;

    // External libraries
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires org.xerial.sqlitejdbc;
    requires com.sothawo.mapjfx;
    requires org.slf4j;

    //maps
    // Java core
    requires java.sql;
    requires java.desktop;
    requires java.xml.crypto;

    // Allow FXML to access these packages via reflection
    opens com.example.app to javafx.fxml;
    opens com.example.app.Controllers.Client to javafx.fxml;
    opens com.example.app.Controllers.Email to javafx.fxml;
    opens com.example.app.Controllers.User to javafx.fxml;
    opens com.example.app.Controllers.Contact to javafx.fxml;
    opens com.example.app.Controllers.Settings to javafx.fxml;
    opens com.example.app.Controllers.Babysitter to javafx.fxml;
    // Exports for app structure
    exports com.example.app;
    exports com.example.app.Controllers.Babysitter;
    exports com.example.app.Controllers.User;
    exports com.example.app.Controllers.Client;
    exports com.example.app.Controllers.Contact;
    exports com.example.app.Controllers.Settings;
    exports com.example.app.Controllers.Email;
    exports com.example.app.Models;
    exports com.example.app.Views;
}