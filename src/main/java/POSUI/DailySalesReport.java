package POSUI;

import POSPD.Cash;
import POSPD.Check;
import POSPD.Credit;
import POSPD.Payment;
import POSPD.Sale;
import POSPD.Session;
import POSPD.StoreService;
import com.github.lgooddatepicker.components.DatePicker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class DailySalesReport extends JPanel {

    /** Create the panel. */
    public DailySalesReport(JFrame currentFrame, StoreService storeService) {
        setLayout(null);

        JLabel lblDailySalesReport = new JLabel("Daily Sales Report");
        lblDailySalesReport.setHorizontalAlignment(SwingConstants.CENTER);
        lblDailySalesReport.setBounds(12, 31, 776, 16);
        add(lblDailySalesReport);

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
                        LocalDate selectedDate = datePicker.getDate();
                        if (selectedDate == null) {
                            JOptionPane.showMessageDialog(
                                    DailySalesReport.this,
                                    "Please select a date.",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        textArea.setText(
                                "Daily Sales Report for "
                                        + selectedDate.format(
                                                DateTimeFormatter.ofPattern("M/d/yyyy"))
                                        + "\n\n");
                        textArea.append("Sales\tItems\tCash\tCheck\tCredit\n");
                        int totSales = 0;
                        int totNumItems = 0;
                        BigDecimal cash = new BigDecimal(0),
                                check = new BigDecimal(0),
                                credit = new BigDecimal(0);
                        for (Session s : storeService.getStore().getSessions()) {
                            if (s.getEndDateTime() == null) {
                                continue; // skip active/un-ended sessions
                            }
                            if (s.getEndDateTime().toLocalDate().equals(selectedDate)) {
                                totSales += s.getSales().size();
                                for (Sale sa : s.getSales()) {
                                    totNumItems += sa.getSaleLineItems().size();

                                    for (Payment p : sa.getPayments()) {
                                        if (p instanceof Cash) {
                                            cash = cash.add(p.getAmtTendered());
                                        } else if (p instanceof Check) {
                                            check = check.add(p.getAmtTendered());
                                        } else if (p instanceof Credit) {
                                            credit = credit.add(p.getAmtTendered());
                                        }
                                    }
                                }
                            }
                        }
                        textArea.append(
                                totSales
                                        + "\t"
                                        + totNumItems
                                        + "\t"
                                        + cash.toString()
                                        + "\t"
                                        + check.toString()
                                        + "\t"
                                        + credit.toString());
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
