package com.retailpos;

import com.retailpos.web.RetailPosWebServer;

public class WebMain {
    public static void main(String[] args) throws Exception {
        RetailPosWebServer webServer = new RetailPosWebServer();
        webServer.start();
    }
}