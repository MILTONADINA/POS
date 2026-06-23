package POSUI;

import POSPD.Register;
import POSPD.Store;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class RegisterEditPanel extends JPanel {
    private JTextField textField;

    /** Create the panel. */
    public RegisterEditPanel(
            JFrame currentFrame,
            POSPD.StoreService storeService,
            Register register,
            Boolean isAdd) {
        Store store = storeService.getStore();
        setLayout(null);

        JLabel lblRegisterEdit = new JLabel("Register Edit");
        lblRegisterEdit.setHorizontalAlignment(SwingConstants.CENTER);
        lblRegisterEdit.setBounds(12, 53, 776, 16);
        add(lblRegisterEdit);

        JLabel lblNumber = new JLabel("Number:");
        lblNumber.setBounds(102, 132, 56, 16);
        add(lblNumber);

        textField = new JTextField(register.getNumber());
        textField.setBounds(181, 129, 116, 22);
        add(textField);
        textField.setColumns(10);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String number = textField.getText().trim();
                        if (number.isEmpty()
                                || (!number.equals(register.getNumber())
                                        && store.getRegisters().get(number) != null)) {
                            JOptionPane.showMessageDialog(
                                    RegisterEditPanel.this,
                                    "Enter a unique, non-blank register number.",
                                    "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        // Re-key: remove under the old number before changing it, then (re)insert.
                        if (!isAdd) {
                            store.removeRegister(register);
                        }
                        register.setNumber(number);
                        store.addRegister(register);
                        if (!SaveSupport.saveOrWarn(null, storeService)) {
                            return;
                        }
                        currentFrame.getContentPane().removeAll();
                        currentFrame
                                .getContentPane()
                                .add(new RegisterListPanel(currentFrame, storeService));
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
                                .add(new RegisterListPanel(currentFrame, storeService));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnCancel.setBounds(497, 445, 97, 25);
        add(btnCancel);
    }
}
