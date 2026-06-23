package POSUI;

import POSPD.StoreService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class StoreEditPanel extends JPanel {
    private JTextField textField;

    /** Create the panel. */
    public StoreEditPanel(JFrame currentFrame, StoreService storeService) {
        setLayout(null);

        JLabel lblStoreEdit = new JLabel("Store Edit");
        lblStoreEdit.setHorizontalAlignment(SwingConstants.CENTER);
        lblStoreEdit.setBounds(12, 53, 776, 16);
        add(lblStoreEdit);

        JLabel lblName = new JLabel("Name:");
        lblName.setBounds(102, 132, 56, 16);
        add(lblName);

        textField = new JTextField(storeService.getStore().getName());
        textField.setBounds(181, 129, 116, 22);
        add(textField);
        textField.setColumns(10);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        storeService.getStore().setName(textField.getText());
                        if (!SaveSupport.saveOrWarn(null, storeService)) {
                            return;
                        }
                        currentFrame.getContentPane().removeAll();
                        currentFrame.getContentPane().add(new POSHome(currentFrame, storeService));
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
                        currentFrame.getContentPane().add(new POSHome(currentFrame, storeService));
                        currentFrame.getContentPane().revalidate();
                    }
                });
        btnCancel.setBounds(497, 445, 97, 25);
        add(btnCancel);
    }
}
