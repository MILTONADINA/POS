package POSUI;

import POSDM.CsvStoreRepository;
import POSDM.StoreRepository;
import POSPD.StoreService;
import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Application entry point. Wires the CSV-backed repository into the service and launches the GUI.
 */
public class Start {

    private static final Logger LOG = Logger.getLogger(Start.class.getName());

    public static void main(String[] args) {
        // Backstop: surface any otherwise-uncaught exception (including on the EDT) to the user
        // instead of letting it vanish to stderr in a deployed GUI.
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) ->
                        reportFatal("Unexpected error on thread " + thread.getName(), error));

        try {
            StoreRepository repository = CsvStoreRepository.defaultRepository();
            StoreService storeService = new StoreService(repository);
            POSFrame.run(storeService);
        } catch (RuntimeException e) {
            reportFatal("The application could not start", e);
            System.exit(1);
        }
    }

    /**
     * Reports a fatal error: logging is the guaranteed channel and always runs first; the dialog is
     * best-effort. It is skipped entirely when headless and any dialog failure is swallowed, so
     * this method can never throw back into the uncaught-exception handler and spin into an error
     * storm.
     *
     * @param message a human-readable context message
     * @param error the error to report (may be {@code null})
     */
    static void reportFatal(String message, Throwable error) {
        LOG.log(Level.SEVERE, message, error);
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            String detail = error == null ? "" : ("\n" + error.getMessage());
            JOptionPane.showMessageDialog(
                    null, message + detail, "Unexpected error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException | Error dialogFailure) {
            // The dialog itself failed (e.g. display lost). Log and stop — never rethrow.
            LOG.log(Level.SEVERE, "Could not display the error dialog", dialogFailure);
        }
    }
}
