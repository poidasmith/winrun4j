package org.boris.winrun4j.res;

import java.io.InputStream;
import java.net.URL;

public class TestHandler
{
    public static void main(String[] args) throws Exception {
        registerHandler();
        InputStream is = new URL("res:///WinRun4J.jar").openStream();
        System.out.println();
    }
    
    public static void registerHandler() {
        // Now register resource URL stream handler
        String urlHandlers = System.getProperty("java.protocol.handler.pkgs");
        if(urlHandlers == null) {
            urlHandlers = "org.boris.winrun4j";
        } else {
            urlHandlers += ";org.boris.winrun4j";
        }
        System.setProperty("java.protocol.handler.pkgs", urlHandlers);

    }
}
