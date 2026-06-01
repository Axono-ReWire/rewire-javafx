module com.axono {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires java.sql;
    requires java.desktop;
    requires javafx.base;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires javafx.swing;
    requires javafx.web;

    exports com.axono;
    exports com.axono.ui;
    exports com.axono.dashboard;
    exports com.axono.onboarding;
    exports com.axono.results;
    exports com.axono.database;
    exports com.axono.auth;
    exports com.axono.browser;
    exports com.axono.content;
    exports com.axono.player;
    exports com.axono.player.module;

}
