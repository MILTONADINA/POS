package POSUI;

import POSPD.Item;
import POSPD.Sale;
import POSPD.SaleLineItem;
import POSPD.Session;
import POSPD.StoreService;
import com.github.lgooddatepicker.components.DatePicker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class ItemReport extends JPanel {

    /** Create the panel. */
    public ItemReport(JFrame currentFrame, StoreService storeService) {
        setLayout(null);

        JLabel lblItemReport = new JLabel("Item Report");
        lblItemReport.setHorizontalAlignment(SwingConstants.CENTER);
        lblItemReport.setBounds(12, 31, 776, 16);
        add(lblItemReport);

        DatePicker datePicker = new DatePicker();
        datePicker.setBounds(312, 126, 160, 22);
        add(datePicker);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(179, 170, 456, 276);
        add(scrollPane);

        JTextArea textArea = new JTextArea();
        scrollPane.setViewportView(textArea);

        JButton btnGenerate = new JButton("Generate");
        btnGenerate.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        HashMap<Item, Integer> map = new HashMap<Item, Integer>();
                        int temp;
                        textArea.setText(
                                "Item report for "
                                        + datePicker
                                                .getDate()
                                                .format(DateTimeFormatter.ofPattern("M/d/yyyy"))
                                        + "\n\n");
                        for (Session s : storeService.getStore().getSessions()) {
                            for (Sale sa : s.getSales()) {
                                for (SaleLineItem sli : sa.getSaleLineItems()) {
                                    if (map.containsKey(sli.getItem())) {
                                        temp = map.get(sli.getItem());
                                        map.remove(sli.getItem());
                                        temp += sli.getQuantity();
                                        map.put(sli.getItem(), temp);
                                    } else map.put(sli.getItem(), sli.getQuantity());
                                }
                            }
                        }
                        for (java.util.Map.Entry<Item, Integer> entry : map.entrySet()) {
                            Item i = entry.getKey();
                            textArea.append(
                                    i.getNumber()
                                            + "\t"
                                            + i.getDescription()
                                            + "\t\t"
                                            + entry.getValue()
                                            + "\n");
                        }
                    }
                });
        btnGenerate.setBounds(179, 459, 97, 25);
        add(btnGenerate);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(538, 459, 97, 25);
        btnCancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame.getContentPane().add(new POSHome(currentFrame, storeService));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        add(btnCancel);
    }
}
