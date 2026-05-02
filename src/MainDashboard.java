import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * MainDashboard.java
 * Purpose: Acts as the primary User Interface for the Bebest Store Management System.
 * Features: Inventory visualization, CRUD operations, and barcode scanning simulation.
 */
public class MainDashboard extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // --- UI Structure Components ---
    private JPanel sidebar, header, contentArea;
    private JTextField scanField;
   
    // --- Inventory & Data Components ---
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtCategory, txtPrice, txtQty, txtBarcode;

    /**
     * Constructor: Initializes the frame settings and builds the layout.
     */
    public MainDashboard() {
        // Basic Window Configuration
        setTitle("Bebest Store Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window on screen
        setLayout(new BorderLayout());

        // --- SECTION 1: SIDEBAR NAVIGATION ---
        sidebar = new JPanel();
        sidebar.setBackground(new Color(44, 62, 80)); // Professional Navy Gray
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        JLabel logo = new JLabel("BEBEST IMS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        sidebar.add(logo);

        // --- SECTION 2: HEADER (Action Bar) ---
        header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1100, 60));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15));
        
        JLabel lblSearch = new JLabel("Scan Barcode:");
        scanField = new JTextField(30);
        header.add(lblSearch);
        header.add(scanField);

        // --- SECTION 3: CONTENT VIEWPORT ---
        contentArea = new JPanel();
        contentArea.setBackground(new Color(236, 240, 241)); // Light Gray background
        contentArea.setLayout(null); // Using Absolute Positioning for precise UI design

        // Layout Assembly
        add(sidebar, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);

        // Initialization Logic
        setupInventoryUI();
        refreshTable();

        // Event Listener for Barcode Scanner Simulation
        scanField.addActionListener(e -> processScan());
    }

    /**
     * Sets up the Inventory Management workspace within the content area.
     * Includes the data table, input form, and action buttons.
     */
    private void setupInventoryUI() {
        contentArea.removeAll();

        // 1. DATA TABLE CONFIGURATION
        String[] columns = {"Barcode", "Name", "Category", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBounds(30, 30, 600, 400);
        contentArea.add(scrollPane);

        // 2. INPUT FORM CONFIGURATION
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBounds(650, 30, 300, 250);
        form.setOpaque(false);

        // Adding Form Labels and Fields
        form.add(new JLabel("Barcode:")); txtBarcode = new JTextField(); form.add(txtBarcode);
        form.add(new JLabel("Name:"));    txtName = new JTextField();    form.add(txtName);
        form.add(new JLabel("Category:"));txtCategory = new JTextField();form.add(txtCategory);
        form.add(new JLabel("Price:"));   txtPrice = new JTextField();   form.add(txtPrice);
        form.add(new JLabel("Qty:"));     txtQty = new JTextField();     form.add(txtQty);
        contentArea.add(form);

        // 3. ACTION BUTTONS
        JButton btnAdd = new JButton("Add Product");
        btnAdd.setBackground(new Color(46, 204, 113)); // Emerald Green
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setBounds(650, 300, 145, 40);
        
        JButton btnDelete = new JButton("Delete Product");
        btnDelete.setBackground(new Color(231, 76, 60)); // Alizarin Red
        btnDelete.setForeground(Color.BLACK);
        btnDelete.setBounds(805, 300, 145, 40);

        contentArea.add(btnAdd);
        contentArea.add(btnDelete);

        // --- BUTTON EVENT LOGIC ---
        
        // Add Record Logic
        btnAdd.addActionListener(e -> {
            try {
                InventoryManager.addProduct(txtBarcode.getText(), txtName.getText(), 
                                          txtCategory.getText(), Double.parseDouble(txtPrice.getText()), 
                                          Integer.parseInt(txtQty.getText()));
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Product recorded successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: Please check input formats.");
            }
        });

        // Table Selection Logic: Syncs selected row data back to input fields for editing/viewing
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = productTable.getSelectedRow();
                txtBarcode.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtCategory.setText(tableModel.getValueAt(row, 2).toString());
                txtPrice.setText(tableModel.getValueAt(row, 3).toString());
                txtQty.setText(tableModel.getValueAt(row, 4).toString());
            }
        });
        
        contentArea.repaint();
    }

    /**
     * Fetches the latest product data from the MySQL database and populates the table.
     */
    private void refreshTable() {
        tableModel.setRowCount(0); // Reset table state
        try (Connection conn = InventoryManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("barcode"), 
                    rs.getString("product_name"),
                    rs.getString("category"), 
                    rs.getDouble("price"), 
                    rs.getInt("stock_quantity")
                });
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Helper method to reset input fields after an action.
     */
    private void clearFields() {
        txtBarcode.setText(""); 
        txtName.setText(""); 
        txtCategory.setText("");
        txtPrice.setText(""); 
        txtQty.setText("");
    }

    /**
     * Handles the scanning simulation. Searches for a product by barcode
     * and alerts the user of the result.
     */
    private void processScan() {
        String code = scanField.getText();
        Product p = InventoryManager.getProductByBarcode(code);
        
        if (p != null) {
            JOptionPane.showMessageDialog(this, "Found: " + p.getName() + "\nPrice: P" + p.getPrice());
        } else {
            JOptionPane.showMessageDialog(this, "Product not recognized in database.");
        }
        scanField.setText(""); // Reset scan field for next input
    }

    /**
     * Main Entry Point: Applies system look and feel and launches the dashboard.
     */
    public static void main(String[] args) {
        try { 
            // Matches the UI theme to the user's Operating System
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}