module com.example.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires org.xerial.sqlitejdbc;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;

    opens com.example.app to javafx.fxml;
    exports com.example.app;
    exports com.example.app.Controllers;
    exports com.example.app.Controllers.Babysitter;
    exports com.example.app.Controllers.User;
    exports com.example.app.Models;
    exports com.example.app.Views;
}