package POSUI;

import POSDM.CsvStoreRepository;
import POSDM.StoreRepository;
import POSPD.StoreService;

/**
 * Application entry point. Wires the CSV-backed repository into the service and launches the GUI.
 */
public class Start {

    public static void main(String[] args) {
        StoreRepository repository = CsvStoreRepository.defaultRepository();
        StoreService storeService = new StoreService(repository);
        POSFrame.run(storeService);
    }
}
