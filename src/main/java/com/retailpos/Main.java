package com.retailpos;

import com.retailpos.ui.login.LoginFrame;
import com.retailpos.web.RetailPosWebServer;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        try {
            RetailPosWebServer webServer =
                    new RetailPosWebServer();

            webServer.start();

        } catch (Exception e) {

            System.err.println(
                    "Failed to start Retail POS Web Server."
            );

            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {

            LoginFrame loginFrame =
                    new LoginFrame();

            loginFrame.setVisible(true);
        });
    }
}