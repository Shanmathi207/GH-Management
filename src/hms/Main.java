package hms;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Government Hospital Management System (HospitalOOP70).
 * Launches the modern Swing Desktop Frontend by default on the Event Dispatch Thread (EDT).
 * Console mode is preserved and can be launched via `--console` command line argument.
 */
public class Main {

    public static void main(String[] args) {
        // Preserve console launcher capability if invoked with --console or console argument
        if (args.length > 0 && ("--console".equalsIgnoreCase(args[0]) || "console".equalsIgnoreCase(args[0]))) {
            System.out.println("Starting HospitalOOP70 in Console Mode...");
            ConsoleLauncher.runConsole(new HospitalManagementSystem());
            return;
        }

        // Launch Desktop GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native rendering
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback to default Swing Look & Feel
            }

            // Launch modern Hospital Login Window
            hms.ui.LoginFrame loginFrame = new hms.ui.LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
