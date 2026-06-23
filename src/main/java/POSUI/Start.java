package POSUI;

import POSDM.CsvStoreRepository;
import POSDM.StoreRepository;
import POSPD.StoreService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Application entry point. Wires the CSV-backed repository into the service and launches the GUI.
 */
public class Start {

    private static final Logger LOG = Logger.getLogger(Start.class.getName());

    public static void main(String[] args) {
        // Backstop: surface any otherwise-uncaught exception (including on the EDT) to the user
        // instead of letting it vanish to stderr in a deployed GUI.
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> {
                    LOG.log(
                            Level.SEVERE,
                            "Uncaught exception on thread " + thread.getName(),
                            error);
                    SwingUtilities.invokeLater(
                            () ->
                                    JOptionPane.showMessageDialog(
                                            null,
                                            "An unexpected error occurred:\n" + error.getMessage(),
                                            "Unexpected error",
                                            JOptionPane.ERROR_MESSAGE));
                });

        try {
            StoreRepository repository = CsvStoreRepository.defaultRepository();
            StoreService storeService = new StoreService(repository);
            POSFrame.run(storeService);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Failed to start the application", e);
            JOptionPane.showMessageDialog(
                    null,
                    "The application could not start: " + e.getMessage(),
                    "Startup error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
