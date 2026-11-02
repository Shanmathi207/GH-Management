package hms;

import hms.web.HospitalWebServer;
import java.awt.Desktop;
import java.net.URI;

/**
 * Main launcher entry point for the HospitalOOP70 Web Application.
 * Starts the embedded REST web server and opens the browser to http://localhost:8080.
 */
public class WebLauncher {

    public static void main(String[] args) {
        int port = HospitalWebServer.DEFAULT_PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }
        try {
            HospitalWebServer server = new HospitalWebServer(port);
            server.start();

            String url = "http://localhost:" + port;
            System.out.println("[WebLauncher] Hospital Management System is online.");
            System.out.println("[WebLauncher] Opening browser to: " + url);
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
                }
            } catch (Exception ex) {
                System.out.println("[WebLauncher] Please open your browser and navigate to: " + url);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

