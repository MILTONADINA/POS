package POSDM;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;

import POSPD.Cash;
import POSPD.Cashier;
import POSPD.Check;
import POSPD.Credit;
import POSPD.Item;
import POSPD.Price;
import POSPD.PromoPrice;
import POSPD.Register;
import POSPD.Sale;
import POSPD.SaleLineItem;
import POSPD.Session;
import POSPD.Store;
import POSPD.TaxCategory;
import POSPD.TaxRate;
import POSPD.UPC;

public class DataManagement {
	public static Store loadStore(Store store) {
		Sale saAdd = null;

		String fileName = "src/main/resources/StoreData_v2024FA.csv";
		String line = null;
		String dataType;
		String[] splitter;
		FileReader fileReader;
		BufferedReader bufferedReader;
		try {
			fileReader = new FileReader(fileName);

			bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {

				splitter = line.split(",", -1);

				dataType = splitter[0];

				if (dataType.equals("Store")) {
					store.setName(splitter[1]);
				} else if (dataType.equals("TaxCategory")) {
					// tax category to add to the store
					TaxCategory tCAdd = new TaxCategory(splitter[1]);
					// tax rate to add to the above tax category
					TaxRate tRAdd = new TaxRate(splitter[3], splitter[2]);
					tCAdd.addTaxRate(tRAdd);
					store.addTaxCategory(tCAdd);
				} else if (dataType.equals("Cashier")) {
					store.addCashier(new Cashier(splitter[1], splitter[2], splitter[3], splitter[4], splitter[5],
							splitter[6], splitter[7], splitter[8], splitter[9]));
				} else if (dataType.equals("Item")) {
					// new item to add to the store
					Item iAdd = new Item(splitter[1], splitter[3]);
					// price to add to item
					Price pAdd = new Price(splitter[5], splitter[6]);
					// upc to add to item
					UPC uAdd = new UPC(splitter[2], iAdd);
					// set item tax category
					iAdd.setTaxCategory(splitter[4], store);
					iAdd.addPrice(pAdd);
					// if the splitter array is greater than 7 then we have a promoprice to add
					if (splitter.length > 7) {
						PromoPrice pPAdd = new PromoPrice(splitter[7], splitter[8], splitter[9]);
						iAdd.addPrice(pPAdd);
					}
					store.addItem(iAdd);
				} else if (dataType.equals("Register")) {
					store.addRegister(new Register(splitter[1]));
				}

				else if (dataType.equals("Session")) {
					Session session = new Session(splitter[1], splitter[2], store);
					session.setEndDateTime(LocalDateTime.now());
					store.addSession(session);
				} else if (dataType.equals("Sale")) {
					saAdd = new Sale(splitter[1]);
					store.getSessions().get(store.getSessions().size() - 1).addSale(saAdd);
				} else if (dataType.equals("SaleLineItem")) {
					Session session = store.getSessions().get(store.getSessions().size() - 1);
					session.getSales().get(session.getSales().size() - 1)
							.addSaleLineItem(new SaleLineItem(splitter[1], splitter[2], store));
				} else if (dataType.equals("Payment")) {
					dataType = splitter[1];

					if (dataType.equals("Cash")) {
						Session session = store.getSessions().get(store.getSessions().size() - 1);
						session.getSales().get(session.getSales().size() - 1)
								.addPayment(new Cash(splitter[2], splitter[3]));
					} else if (dataType.equals("Credit")) {
						Session session = store.getSessions().get(store.getSessions().size() - 1);
						Credit cAdd = new Credit(splitter[4], splitter[5], splitter[6]);
						cAdd.setAmount(splitter[2]);
						cAdd.setAmtTendered(splitter[3]);
						session.getSales().get(session.getSales().size() - 1).addPayment(cAdd);
					} else if (dataType.equals("Check")) {
						Session session = store.getSessions().get(store.getSessions().size() - 1);
						Check cAdd = new Check(splitter[2], splitter[3], splitter[5], splitter[6]);
						cAdd.setRoutingNumber(splitter[4]);
						session.getSales().get(session.getSales().size() - 1).addPayment(cAdd);
					}
				}
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException e) {
			System.out.println("Error reading file '" + fileName + "'");
		}

		return store;
	}

	public static void saveStore(Store store) {
		String fileName = "src/main/resources/StoreData_v2024FA.csv";
		try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(fileName))) {
			// Save Store
			writer.println("Store," + store.getName());

			// Save TaxCategories and TaxRates
			for (TaxCategory tc : store.getTaxCategories().values()) {
				for (TaxRate tr : tc.getTaxRates()) {
					writer.println("TaxCategory," + tc.getCategory() + "," + tr.getTaxRate().toString() + ","
							+ tr.getEffectiveDate().format(java.time.format.DateTimeFormatter.ofPattern("M/d/yy")));
				}
			}

			// Save Cashiers
			for (Cashier c : store.getCashiers().values()) {
				writer.println("Cashier," + c.getNumber() + "," + c.getPerson().getName() + "," + c.getPerson().getSSN()
						+ "," +
						c.getPerson().getAddress() + "," + c.getPerson().getCity() + "," + c.getPerson().getState()
						+ "," +
						c.getPerson().getZip() + "," + c.getPerson().getPhone() + "," + c.getPassword());
			}

			// Save Items, UPCs, and Prices
			for (Item item : store.getItems().values()) {
				for (UPC upc : item.getUpcs().values()) {
					StringBuilder line = new StringBuilder("Item," + item.getNumber() + "," + upc.getUPC() + ","
							+ item.getDescription() + "," + item.getTaxCategory().getCategory());
					for (Price p : item.getPrices()) {
						line.append(",").append(p.getPrice().toString()).append(",").append(
								p.getEffectiveDate().format(java.time.format.DateTimeFormatter.ofPattern("M/d/yy")));
						if (p instanceof PromoPrice) {
							line.append(",").append(((PromoPrice) p).getEndDate()
									.format(java.time.format.DateTimeFormatter.ofPattern("M/d/yy")));
						}
					}
					writer.println(line.toString());
				}
			}

			// Save Registers
			for (Register r : store.getRegisters().values()) {
				writer.println("Register," + r.getNumber());
			}

			// Save Sessions, Sales, SaleLineItems, and Payments
			for (Session s : store.getSessions()) {
				writer.println("Session," + s.getCashier().getNumber() + "," + s.getRegister().getNumber());
				for (Sale sale : s.getSales()) {
					writer.println("Sale," + (sale.getTaxFree() ? "Y" : "N"));
					for (SaleLineItem sli : sale.getSaleLineItems()) {
						writer.println("SaleLineItem," + sli.getItem().getNumber() + "," + sli.getQuantity());
					}
					for (POSPD.Payment p : sale.getPayments()) {
						if (p instanceof Cash) {
							writer.println("Payment,Cash," + p.getAmount() + "," + p.getAmtTendered());
						} else if (p instanceof Credit) {
							Credit c = (Credit) p;
							writer.println("Payment,Credit," + c.getAmount() + "," + c.getAmtTendered() + ","
									+ c.getCardType() + "," + c.getAcctNumber() + "," + c.getExpireDate()
											.format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy")));
						} else if (p instanceof Check) {
							Check c = (Check) p;
							writer.println("Payment,Check," + c.getAmount() + "," + c.getAmtTendered() + ","
									+ c.getRoutingNumber() + "," + c.getAccountNumber() + "," + c.getCheckNumber());
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error saving file '" + fileName + "'");
		}
	}
}
