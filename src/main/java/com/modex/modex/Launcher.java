package com.modex.modex;

import com.modex.modex.view.UIControl;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "MODEx");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MODEx");

        Application.launch(UIControl.class, args);

    }
}