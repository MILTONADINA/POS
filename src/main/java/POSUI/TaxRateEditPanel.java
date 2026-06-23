package POSUI;

import POSPD.TaxCategory;
import POSPD.TaxRate;
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

public class TaxRateEditPanel extends JPanel {
    private JTextField rateField;
    private JTextField dateField;

    /** Create the panel. */
    public TaxRateEditPanel(
            JFrame currentFrame,
            JPanel currentPanel,
            POSPD.StoreService storeService,
            TaxCategory taxCategory,
            TaxRate taxRate,
            Boolean isAdd) {
        setLayout(null);

        JLabel lblTaxRateEdit = new JLabel("Tax Rate Edit");
        lblTaxRateEdit.setHorizontalAlignment(SwingConstants.CENTER);
        lblTaxRateEdit.setBounds(12, 53, 776, 16);
        add(lblTaxRateEdit);

        JLabel lblRate = new JLabel("Rate:");
        lblRate.setBounds(102, 132, 56, 16);
        add(lblRate);

        JLabel lblEffectiveDate = new JLabel("Effective Date:");
        lblEffectiveDate.setBounds(102, 164, 83, 16);
        add(lblEffectiveDate);

        rateField = new JTextField(taxRate.getTaxRate().toString());
        rateField.setBounds(260, 129, 116, 22);
        add(rateField);
        rateField.setColumns(10);

        dateField =
                new JTextField(
                        taxRate.getEffectiveDate().format(DateTimeFormatter.ofPattern("M/d/yyyy")));
        dateField.setBounds(260, 161, 116, 22);
        add(dateField);
        dateField.setColumns(10);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        BigDecimal rate;
                        LocalDate effective;
                        try {
                            rate = new BigDecimal(rateField.getText().trim());
                            effective =
                                    LocalDate.parse(
                                            dateField.getText().trim(),
                                            DateTimeFormatter.ofPattern("M/d/yyyy"));
                        } catch (RuntimeException ex) {
                            JOptionPane.showMessageDialog(
                                    TaxRateEditPanel.this,
                                    "Enter a valid rate and effective date (M/d/yyyy).",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        // Remove before mutating so the TreeSet re-sorts on the new effective date.
                        if (!isAdd) {
                            taxCategory.removeTaxRate(taxRate);
                        }
                        taxRate.setTaxRate(rate);
                        taxRate.setEffectiveDate(effective);
                        taxCategory.addTaxRate(taxRate);
                        storeService.saveStoreState();
                        currentFrame.getContentPane().removeAll();
                        currentFrame.getContentPane().add(currentPanel);
                        currentFrame.getContentPane().repaint();
                    }
                });
        btnSave.setBounds(200, 445, 97, 25);
        add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame.getContentPane().add(currentPanel);
                        currentFrame.getContentPane().repaint();
                    }
                });
        btnCancel.setBounds(497, 445, 97, 25);
        add(btnCancel);
    }
}
