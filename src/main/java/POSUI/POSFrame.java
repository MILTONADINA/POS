package POSUI;

import POSPD.StoreService;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class POSFrame extends JFrame {

    private static final Logger LOG = Logger.getLogger(POSFrame.class.getName());

    private JPanel contentPane;

    /** Launch the application. */
    public static void run(StoreService storeService) {
        EventQueue.invokeLater(
                () -> {
                    try {
                        POSFrame frame = new POSFrame(storeService);
                        frame.setVisible(true);
                    } catch (RuntimeException e) {
                        // Route through the same user-facing handling as the rest of the app rather
                        // than letting the main window silently fail to a stderr stack trace.
                        LOG.log(Level.SEVERE, "Failed to construct the main window", e);
                        JOptionPane.showMessageDialog(
                                null,
                                "The application could not start: " + e.getMessage(),
                                "Startup error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
    }

    /** Create the frame. */
    public POSFrame(StoreService storeService) {
        JFrame currentFrame = this;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Point of Sale");
        setSize(800, 600);
        setLocationRelativeTo(null);
        loadIcon();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnMaintenance = new JMenu("Maintain");
        menuBar.add(mnMaintenance);

        JMenuItem mntmStore = new JMenuItem("Store");
        mntmStore.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new StoreEditPanel(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnMaintenance.add(mntmStore);

        JMenuItem mntmTaxcategories = new JMenuItem("TaxCategories");
        mntmTaxcategories.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new TaxCategoryListPanel(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnMaintenance.add(mntmTaxcategories);

        JMenuItem mntmRegisters = new JMenuItem("Registers");
        mntmRegisters.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new RegisterListPanel(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnMaintenance.add(mntmRegisters);

        JMenuItem mntmCashiers = new JMenuItem("Cashiers");
        mntmCashiers.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new CashierListPanel(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnMaintenance.add(mntmCashiers);

        JMenuItem mntmItems = new JMenuItem("Items");
        mntmItems.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new ItemListPanel(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnMaintenance.add(mntmItems);

        JMenu mnPos = new JMenu("POS");
        menuBar.add(mnPos);

        JMenuItem mntmLogin = new JMenuItem("Login");
        mntmLogin.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new POSLogin(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnPos.add(mntmLogin);

        JMenu mnReports = new JMenu("Reports");
        menuBar.add(mnReports);

        JMenuItem mntmCashierReport = new JMenuItem("Cashier Report");
        mntmCashierReport.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        getContentPane().removeAll();
                        getContentPane().add(new CashierReport(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnReports.add(mntmCashierReport);

        JMenuItem mntmItemReport = new JMenuItem("Item Report");
        mntmItemReport.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new ItemReport(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnReports.add(mntmItemReport);

        JMenuItem mntmDailySalesReport = new JMenuItem("Daily Sales Report");
        mntmDailySalesReport.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getContentPane().removeAll();
                        getContentPane().add(new DailySalesReport(currentFrame, storeService));
                        getContentPane().revalidate();
                    }
                });
        mnReports.add(mntmDailySalesReport);
        contentPane = new POSHome(currentFrame, storeService);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
    }

    /** Loads the bundled window icon from the classpath; a missing icon is non-fatal. */
    private void loadIcon() {
        try (InputStream in = getClass().getResourceAsStream("/icon.png")) {
            if (in != null) {
                Image icon = ImageIO.read(in);
                if (icon != null) {
                    setIconImage(icon);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not load application icon", e);
        }
    }
}
