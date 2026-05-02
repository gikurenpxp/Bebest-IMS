import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainDashboard extends JFrame {
    
    // UI Components
    private JPanel sidebar, header, contentArea;
    private JTextField scanField;
    private JLabel statusLabel;

    public MainDashboard() {
        setTitle("Bebest Store Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (Dark Blue/Gray) ---
        sidebar = new JPanel();
        sidebar.setBackground(new Color(44, 62, 80)); // Professional Navy
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        JLabel logo = new JLabel("BEBEST IMS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        sidebar.add(logo);
        // Add buttons here later...

        // --- 2. HEADER (White with Search) ---
        header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1100, 60));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15));
        
        JLabel lblSearch = new JLabel("Scan Barcode:");
        scanField = new JTextField(30);
        header.add(lblSearch);
        header.add(scanField);

        // --- 3. CONTENT AREA (Light Gray) ---
        contentArea = new JPanel();
        contentArea.setBackground(new Color(236, 240, 241));
        contentArea.setLayout(null); // For absolute positioning

        statusLabel = new JLabel("System Ready. Please scan an item.");
        statusLabel.setBounds(50, 50, 500, 30);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        contentArea.add(statusLabel);

        // --- ADD TO MAIN FRAME ---
        add(sidebar, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);

        // --- THE LOGIC (The Scanner Bridge) ---
        scanField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processScan();
            }
        });
    }

    private void processScan() {
        String code = scanField.getText();
        // Call your backend!
        Product p = InventoryManager.getProductByBarcode(code);
        
        if (p != null) {
            statusLabel.setText("SCANNED: " + p.getName() + " | Price: P" + p.getPrice());
        } else {
            statusLabel.setText("ERROR: Barcode " + code + " not found!");
        }
        scanField.setText(""); // Ready for next scan
    }

    public static void main(String[] args) {
        // Look and Feel (Makes it look modern)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new MainDashboard().setVisible(true);
        });
    }
}