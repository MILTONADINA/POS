package POSUI;

import POSDM.DataManagement;
import POSPD.Store;
import POSPD.StoreService;

public class Start {
	public static void main(String[] args) {

		Store myStore = new Store();
		DataManagement.loadStore(myStore);
		StoreService storeService = new StoreService(myStore);
		POSFrame.run(storeService);
	}
}