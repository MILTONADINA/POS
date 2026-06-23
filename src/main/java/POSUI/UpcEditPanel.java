package POSUI;

import POSPD.Item;
import POSPD.Store;
import POSPD.UPC;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class UpcEditPanel extends JPanel {
    private JTextField textField;

    /** Create the panel. */
    public UpcEditPanel(
            JFrame currentFrame,
            JPanel currentPanel,
            POSPD.StoreService storeService,
            Item item,
            UPC upc,
            Boolean isAdd) {
        Store store = storeService.getStore();
        setLayout(null);
        JLabel lblUpcEdit = new JLabel("UPC Edit");
        lblUpcEdit.setHorizontalAlignment(SwingConstants.CENTER);
        lblUpcEdit.setBounds(12, 57, 776, 16);
        add(lblUpcEdit);

        JLabel lblUpc = new JLabel("UPC:");
        lblUpc.setBounds(102, 132, 28, 16);
        add(lblUpc);

        textField = new JTextField(upc.getUPC());
        textField.setBounds(200, 129, 116, 22);
        add(textField);
        textField.setColumns(10);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String code = textField.getText().trim();
                        if (code.isEmpty()
                                || (!code.equals(upc.getUPC())
                                        && store.findItemForUPC(code) != null)) {
                            JOptionPane.showMessageDialog(
                                    UpcEditPanel.this,
                                    "Enter a unique, non-blank UPC.",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        // Re-key in the code-keyed maps (Item.upcs and Store.upcs): remove under
                        // the
                        // old code before changing it, then (re)insert under the new code for both
                        // add and edit — otherwise an edited code is filed under the stale key and
                        // the register cannot resolve it until the next reload.
                        if (!isAdd) {
                            item.removeUPC(upc);
                            store.removeUPC(upc);
                        }
                        upc.setUPC(code);
                        upc.setItem(item);
                        item.addUPC(upc);
                        store.addUPC(upc);
                        if (!SaveSupport.saveOrWarn(null, storeService)) {
                            return;
                        }
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
