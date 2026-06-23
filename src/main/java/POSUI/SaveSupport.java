package POSUI;

import POSDM.StorePersistenceException;
import POSPD.StoreService;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Shared helper so every screen reports a persistence failure the same way the sale path does —
 * with a clear "not saved" message — instead of letting it reach the generic global handler.
 */
final class SaveSupport {

    private SaveSupport() {
        // Utility class; not instantiable.
    }

    /**
     * Persists the store, showing a clear failure dialog if persistence fails.
     *
     * @param parent the dialog's parent component (may be {@code null})
     * @param storeService the service to persist through
     * @return {@code true} if the save succeeded; {@code false} if it failed (caller should stop)
     */
    static boolean saveOrWarn(Component parent, StoreService storeService) {
        try {
            storeService.saveStoreState();
            return true;
        } catch (StorePersistenceException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Your changes were NOT saved: " + ex.getMessage(),
                    "Save failed",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
