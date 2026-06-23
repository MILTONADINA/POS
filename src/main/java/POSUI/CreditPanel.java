package POSUI;

import POSPD.Credit;
import POSPD.Sale;
import POSPD.Session;
import POSPD.Store;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CreditPanel extends JPanel {
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JTextField textField_3;

    /** Create the panel. */
    public CreditPanel(
            JFrame currentFrame,
            JPanel paymentPanel,
            Store store,
            Session session,
            Sale sale,
            Credit credit) {
        CreditPanel thisPanel = this;
        setLayout(null);

        JLabel lblCredit = new JLabel("Credit");
        lblCredit.setHorizontalAlignment(SwingConstants.CENTER);
        lblCredit.setBounds(12, 32, 376, 16);
        add(lblCredit);

        JLabel lblCardType = new JLabel("Card Type:");
        lblCardType.setBounds(47, 61, 64, 16);
        add(lblCardType);

        textField = new JTextField();
        textField.setBounds(125, 61, 116, 22);
        add(textField);
        textField.setColumns(10);

        JLabel lblAmount = new JLabel("Amount: ");
        lblAmount.setBounds(47, 104, 56, 16);
        add(lblAmount);

        textField_1 = new JTextField();
        textField_1.setBounds(125, 101, 116, 22);
        add(textField_1);
        textField_1.setColumns(10);

        JLabel lblAccount = new JLabel("Account #:");
        lblAccount.setBounds(47, 143, 64, 16);
        add(lblAccount);

        textField_2 = new JTextField();
        textField_2.setBounds(125, 140, 116, 22);
        add(textField_2);
        textField_2.setColumns(10);

        JLabel lblExpireDate = new JLabel("Expire Date:");
        lblExpireDate.setBounds(47, 185, 70, 16);
        add(lblExpireDate);

        textField_3 = new JTextField();
        textField_3.setBounds(125, 182, 116, 22);
        add(textField_3);
        textField_3.setColumns(10);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        BigDecimal tendered;
                        LocalDate expire;
                        try {
                            tendered = new BigDecimal(textField_1.getText().trim());
                            expire =
                                    LocalDate.parse(
                                            textField_3.getText().trim(),
                                            DateTimeFormatter.ofPattern("M/d/yyyy"));
                        } catch (RuntimeException ex) {
                            JOptionPane.showMessageDialog(
                                    thisPanel,
                                    "Enter a valid amount and expiry date (M/d/yyyy).",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (tendered.signum() <= 0 || tendered.scale() > 2) {
                            JOptionPane.showMessageDialog(
                                    thisPanel,
                                    "Enter a positive amount with at most two decimal places.",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        // A card is charged its exact applied amount (capped at the balance); there
                        // is no cash-style over-tender, so tendered == applied (no phantom change).
                        BigDecimal applied = sale.calcAmount(tendered);
                        credit.setCardType(textField.getText());
                        credit.setAmount(applied);
                        credit.setAmtTendered(applied);
                        credit.setAcctNumber(textField_2.getText());
                        credit.setExpireDate(expire);
                        sale.addPayment(credit);
                        paymentPanel.remove(thisPanel);
                        currentFrame.getContentPane().removeAll();
                        currentFrame.getContentPane().add(paymentPanel);
                        currentFrame.getContentPane().repaint();
                    }
                });
        btnSave.setBounds(73, 240, 97, 25);
        add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        paymentPanel.remove(thisPanel);
                        currentFrame.getContentPane().removeAll();
                        currentFrame.getContentPane().add(paymentPanel);
                        currentFrame.getContentPane().repaint();
                    }
                });
        btnCancel.setBounds(221, 240, 97, 25);
        add(btnCancel);

        JLabel lblMmddyyyy = new JLabel("MM/dd/yyyy");
        lblMmddyyyy.setBounds(253, 185, 78, 16);
        add(lblMmddyyyy);
    }
}
