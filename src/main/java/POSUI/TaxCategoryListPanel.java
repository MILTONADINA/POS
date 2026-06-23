package POSUI;

import POSPD.Store;
import POSPD.TaxCategory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TaxCategoryListPanel extends JPanel {
    JButton btnEdit;
    JButton btnDelete;

    /** Create the panel. */
    public TaxCategoryListPanel(JFrame currentFrame, POSPD.StoreService storeService) {
        Store store = storeService.getStore();
        setLayout(null);

        JLabel lblTaxCategoriesList = new JLabel("Tax Categories List");
        lblTaxCategoriesList.setHorizontalAlignment(SwingConstants.CENTER);
        lblTaxCategoriesList.setBounds(12, 32, 776, 16);
        add(lblTaxCategoriesList);

        DefaultListModel listModel = new DefaultListModel(); // AC 2.B
        for (TaxCategory taxCategory : store.getTaxCategories().values()) {
            listModel.addElement(taxCategory);
        }

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(200, 61, 394, 363);
        add(scrollPane);

        JList<TaxCategory> list = new JList<TaxCategory>(listModel);
        scrollPane.setViewportView(list);
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

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(
                                        new TaxCategoryEditPanel(
                                                currentFrame,
                                                storeService,
                                                new TaxCategory(),
                                                true));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnAdd.setBounds(200, 470, 97, 25);
        add(btnAdd);

        btnEdit = new JButton("Edit");
        btnEdit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(
                                        new TaxCategoryEditPanel(
                                                currentFrame,
                                                storeService,
                                                list.getSelectedValue(),
                                                false));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnEdit.setBounds(358, 470, 97, 25);
        add(btnEdit);
        btnEdit.setEnabled(false);

        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        store.removeTaxCategory(list.getSelectedValue());
                        storeService.saveStoreState();

                        listModel.removeElement(list.getSelectedValue());
                        btnDelete.setEnabled(false);
                    }
                });
        btnDelete.setBounds(497, 470, 97, 25);
        add(btnDelete);
        btnDelete.setEnabled(false);
    }
}
