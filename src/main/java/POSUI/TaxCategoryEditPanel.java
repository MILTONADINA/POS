package POSUI;

import POSPD.Store;
import POSPD.TaxCategory;
import POSPD.TaxRate;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TaxCategoryEditPanel extends JPanel {
    JButton btnEdit;
    JButton btnDelete;

    JList<TaxRate> list;

    private JTextField textField;

    JPanel currentPanel = this;

    DefaultListModel listModel;

    /** Create the panel. */
    public TaxCategoryEditPanel(
            JFrame currentFrame,
            POSPD.StoreService storeService,
            TaxCategory taxCategory,
            Boolean isAdd) {
        Store store = storeService.getStore();
        addAncestorListener(
                new AncestorListener() {
                    public void ancestorAdded(AncestorEvent arg0) {
                        listModel = new DefaultListModel();
                        for (TaxRate taxRate : taxCategory.getTaxRates())
                            listModel.addElement(taxRate);
                        list.setModel(listModel);
                    }

                    public void ancestorMoved(AncestorEvent arg0) {}

                    public void ancestorRemoved(AncestorEvent arg0) {}
                });
        setLayout(null);

        JLabel lblEditTaxCategory = new JLabel("Edit Tax Category");
        lblEditTaxCategory.setHorizontalAlignment(SwingConstants.CENTER);
        lblEditTaxCategory.setBounds(12, 53, 776, 16);
        add(lblEditTaxCategory);

        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setBounds(102, 132, 56, 16);
        add(lblCategory);

        textField = new JTextField(taxCategory.getCategory());
        textField.setBounds(181, 129, 116, 22);
        add(textField);
        textField.setColumns(10);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String newName = textField.getText().trim();
                        if (newName.isEmpty()
                                || (!newName.equals(taxCategory.getCategory())
                                        && store.getTaxCategory(newName) != null)) {
                            JOptionPane.showMessageDialog(
                                    TaxCategoryEditPanel.this,
                                    "Enter a unique, non-blank category name.",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        // Remove under the old key before renaming, then (re)insert under the new
                        // key, so the Store's name-keyed map never holds a stale entry.
                        if (!isAdd) {
                            store.removeTaxCategory(taxCategory);
                        }
                        taxCategory.setCategory(newName);
                        store.addTaxCategory(taxCategory);
                        if (!SaveSupport.saveOrWarn(null, storeService)) {
                            return;
                        }
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(new TaxCategoryListPanel(currentFrame, storeService));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnSave.setBounds(200, 445, 97, 25);
        add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(new TaxCategoryListPanel(currentFrame, storeService));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnCancel.setBounds(497, 445, 97, 25);
        add(btnCancel);

        listModel = new DefaultListModel();
        for (TaxRate taxRate : taxCategory.getTaxRates()) listModel.addElement(taxRate);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(497, 130, 258, 190);
        add(scrollPane);

        list = new JList<TaxRate>(listModel);
        list.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (list.getSelectedValue() != null) {
                            if (list.getSelectedValue() != null) btnEdit.setEnabled(true);
                            if (!list.getSelectedValue().isUsed()) btnDelete.setEnabled(true);
                            else btnDelete.setEnabled(false);
                        } else {
                            btnEdit.setEnabled(false);
                            btnEdit.setEnabled(false);
                        }
                    }
                });
        scrollPane.setViewportView(list);

        JLabel lblTaxRates = new JLabel("Tax Rates:");
        lblTaxRates.setBounds(497, 97, 72, 16);
        add(lblTaxRates);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(
                                        new TaxRateEditPanel(
                                                currentFrame,
                                                currentPanel,
                                                storeService,
                                                taxCategory,
                                                new TaxRate(),
                                                true));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnAdd.setBounds(497, 347, 56, 25);
        add(btnAdd);

        btnEdit = new JButton("Edit");
        btnEdit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(
                                        new TaxRateEditPanel(
                                                currentFrame,
                                                currentPanel,
                                                storeService,
                                                taxCategory,
                                                list.getSelectedValue(),
                                                false));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnEdit.setBounds(589, 347, 61, 25);
        add(btnEdit);
        btnEdit.setEnabled(false);

        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        taxCategory.removeTaxRate(list.getSelectedValue());
                        if (!SaveSupport.saveOrWarn(null, storeService)) {
                            return;
                        }

                        listModel.removeElement(list.getSelectedValue());
                        btnDelete.setEnabled(false);
                    }
                });
        btnDelete.setBounds(683, 347, 72, 25);
        add(btnDelete);
        btnDelete.setEnabled(false);
    }
}
