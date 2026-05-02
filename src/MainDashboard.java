import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * MainDashboard.java
 * * Purpose: Serves as the primary graphical interface for the Bebest Store Management System.
 * Architecture: Utilizes a BorderLayout with a fixed sidebar for navigation and an 
 * absolute-positioned content area for modular inventory management.
 */
public class MainDashboard extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // --- UI Layout Components ---
    private JPanel sidebar, header, contentArea;
    private JTextField scanField;
   
    // --- Inventory Management Components ---
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtCategory, txtPrice, txtQty, txtBarcode;

    /**
     * Constructor: Orchestrates the initialization of the dashboard layout,
     * theme application, and initial data loading.
     */
    public MainDashboard() {
        // Window Meta-data
        setTitle("Bebest Store Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());

        // --- SECTION 1: SIDEBAR (Navigation Branding) ---
        sidebar = new JPanel();
        sidebar.setBackground(new Color(44, 62, 80)); 
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        JLabel logo = new JLabel("BEBEST IMS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        sidebar.add(logo);

        // --- SECTION 2: HEADER (Search & Scanning Bar) ---
        header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1100, 60));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15));
        
        JLabel lblSearch = new JLabel("Scan Barcode:");
        scanField = new JTextField(30);
        header.add(lblSearch);
        header.add(scanField);

        // --- SECTION 3: CONTENT VIEWPORT (Active Module Area) ---
        contentArea = new JPanel();
        contentArea.setBackground(new Color(236, 240, 241)); 
        contentArea.setLayout(null); 

        // Component Assembly
        add(sidebar, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);

        // Module Initialization
        setupInventoryUI();
        refreshTable();

        // Global Event Listeners
        scanField.addActionListener(e -> processScan());
    }

    /**
     * Constructs the Inventory Management module.
     * Features include a synchronized data table and a product entry form.
     */
    private void setupInventoryUI() {
        contentArea.removeAll();

        // 1. DATA TABLE CONFIGURATION
        String[] columns = {"Barcode", "Name", "Category", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBounds(30, 30, 600, 450); 
        contentArea.add(scrollPane);

        // 2. ENTRY FORM CONFIGURATION
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBounds(650, 30, 300, 250);
        form.setOpaque(false);

        form.add(new JLabel("Barcode:")); txtBarcode = new JTextField(); form.add(txtBarcode);
        form.add(new JLabel("Name:"));    txtName = new JTextField();    form.add(txtName);
        form.add(new JLabel("Category:"));txtCategory = new JTextField();form.add(txtCategory);
        form.add(new JLabel("Price:"));   txtPrice = new JTextField();   form.add(txtPrice);
        form.add(new JLabel("Qty:"));     txtQty = new JTextField();     form.add(txtQty);
        contentArea.add(form);

        // 3. CONTROL BUTTONS
        JButton btnAdd = new JButton("Add Product");
        btnAdd.setBackground(new Color(46, 204, 113)); // Success Green
        btnAdd.setBounds(650, 300, 145, 40);
        
        JButton btnDelete = new JButton("Delete Product");
        btnDelete.setBackground(new Color(231, 76, 60)); // Alert Red
        btnDelete.setBounds(805, 300, 145, 40);

        JButton btnRefresh = new JButton("Refresh Table");
        btnRefresh.setBackground(new Color(52, 152, 219)); // Info Blue
        btnRefresh.setBounds(650, 350, 300, 40);

        contentArea.add(btnAdd);
        contentArea.add(btnDelete);
        contentArea.add(btnRefresh);

        // --- BUTTON EVENT LOGIC ---
        
        // Add Record Logic: Captures form data and persists it to the database
        btnAdd.addActionListener(e -> {
            try {
                InventoryManager.addProduct(txtBarcode.getText(), txtName.getText(), 
                                          txtCategory.getText(), Double.parseDouble(txtPrice.getText()), 
                                          Integer.parseInt(txtQty.getText()));
                refreshTable();
                clearFields();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Validation Error: Price and Qty must be numeric.");
            }
        });

        // Delete Logic: Removes the selected product based on barcode identification
        btnDelete.addActionListener(e -> {
            String barcode = txtBarcode.getText();
            if(!barcode.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, "Confirm deletion of Barcode: " + barcode + "?");
                if(confirm == JOptionPane.YES_OPTION) {
                    InventoryManager.deleteProduct(barcode); 
                    refreshTable();
                    clearFields();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Operation Error: No product selected.");
            }
        });

        // Refresh Logic: Manually triggers a database sync to update the table view
        btnRefresh.addActionListener(e -> {
            refreshTable();
            JOptionPane.showMessageDialog(this, "Inventory synchronization complete.");
        });

        // Selection Listener: Populates the entry form with data from a clicked table row
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
     * Synchronizes the UI table with the MySQL database records.
     */
    private void refreshTable() {
        tableModel.setRowCount(0); 
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
            System.err.println("Table Sync Error: " + e.getMessage());
        }
    }

    /**
     * Resets entry form fields to an empty state.
     */
    private void clearFields() {
        txtBarcode.setText(""); 
        txtName.setText(""); 
        txtCategory.setText("");
        txtPrice.setText(""); 
        txtQty.setText("");
    }

    /**
     * Executes the scanning logic by querying the database for a specific barcode.
     */
    private void processScan() {
        String code = scanField.getText();
        Product p = InventoryManager.getProductByBarcode(code);
        
        if (p != null) {
            JOptionPane.showMessageDialog(this, "SKU Found: " + p.getName() + "\nUnit Price: P" + p.getPrice());
        } else {
            JOptionPane.showMessageDialog(this, "Scanning Error: Barcode not found in database.");
        }
        scanField.setText(""); 
    }

    /**
     * Launch Point: Configures system look and feel and instantiates the dashboard.
     */
    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}