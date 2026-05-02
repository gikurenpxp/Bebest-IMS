import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * MainDashboard.java
 * * Purpose: Primary Graphical User Interface for the Bebest Inventory Management System (IMS).
 * * Features:
 * - Responsive Layout: Utilizes BorderLayout and BoxLayout for dynamic window resizing.
 * - Data Integrity: Implements input validation and SQL error feedback for duplicate entries.
 * - UX Design: Custom-styled action buttons with interactive hover animations.
 * - Integration: Real-time synchronization with the MySQL database via the InventoryManager.
 * * @author Bebest Development Team
 * @version 2.2 (Finalized UI & Documentation)
 */
public class MainDashboard extends JFrame {
    private static final long serialVersionUID = 1L;

    // UI Structure Components
    private JPanel sidebar, header, contentArea;
    private JTextField scanField, txtName, txtCategory, txtPrice, txtQty, txtBarcode;
    private JTable productTable;
    private DefaultTableModel tableModel;

    /**
     * Constructor: Initializes the application window, assembles the UI components,
     * and triggers the initial data fetch from the database.
     */
    public MainDashboard() {
        setupWindow();
        initSidebar();
        initHeader();
        initContentArea();
        refreshTable(); // Loads current inventory into the table on startup
    }

    /**
     * Configures the main JFrame properties including title, size limits, and centering.
     */
    private void setupWindow() {
        setTitle("Bebest Store Management System");
        setSize(1100, 700);
        setMinimumSize(new Dimension(1000, 600)); // Prevents UI overlapping on small windows
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the application on the screen
        setLayout(new BorderLayout());
    }

    /**
     * Initializes the side navigation panel with a modern "Enterprise" navy theme.
     */
    private void initSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 47, 61)); // Deep Navy
        sidebar.setPreferredSize(new Dimension(180, 700));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 40));
        
        JLabel logo = new JLabel("BEBEST IMS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        sidebar.add(logo);
        add(sidebar, BorderLayout.WEST);
    }

    /**
     * Initializes the top header featuring the barcode scanner simulation input.
     */
    private void initHeader() {
        header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1100, 70));
        
        JLabel lblScan = new JLabel("Barcode Scanner:");
        lblScan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        scanField = new JTextField(30);
        
        header.add(lblScan);
        header.add(scanField);
        add(header, BorderLayout.NORTH);

        // Listener for scanning: executes search logic on 'Enter' key press
        scanField.addActionListener(e -> processScan());
    }

    /**
     * Main UI container area featuring the Inventory Table and the Product Details "Card."
     */
    private void initContentArea() {
        contentArea = new JPanel(new BorderLayout(25, 0));
        contentArea.setBackground(new Color(242, 244, 244)); // Light Cloud Gray
        contentArea.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        add(contentArea, BorderLayout.CENTER);

        // --- Table Section: Data Visualization ---
        tableModel = new DefaultTableModel(new String[]{"Barcode", "Name", "Category", "Price", "Stock"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(35);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("INVENTORY DATABASE"));
        contentArea.add(scrollPane, BorderLayout.CENTER);

        // --- Right Side Control Panel: Data Entry and Action Controls ---
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(320, 600));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Product Form "Card" Styling
        JPanel formCard = new JPanel(new GridLayout(6, 2, 10, 15));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("PRODUCT DETAILS"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        formCard.setMaximumSize(new Dimension(320, 300));

        formCard.add(new JLabel("Barcode:")); txtBarcode = new JTextField(); formCard.add(txtBarcode);
        formCard.add(new JLabel("Name:"));    txtName = new JTextField();    formCard.add(txtName);
        formCard.add(new JLabel("Category:"));txtCategory = new JTextField();formCard.add(txtCategory);
        formCard.add(new JLabel("Price:"));   txtPrice = new JTextField();   formCard.add(txtPrice);
        formCard.add(new JLabel("Qty:"));     txtQty = new JTextField();     formCard.add(txtQty);

        // Action Buttons (Color Coded)
        JButton btnAdd = new JButton("ADD PRODUCT");
        styleButton(btnAdd, new Color(40, 180, 99)); // Success Emerald Green
        
        JButton btnDelete = new JButton("DELETE");
        styleButton(btnDelete, new Color(203, 67, 53)); // Soft Alizarin Red

        JButton btnRefresh = new JButton("REFRESH TABLE");
        styleButton(btnRefresh, new Color(52, 152, 219)); // Action Sky Blue
        
        // Button Row Assembly
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        btnRow.setOpaque(false);
        btnAdd.setPreferredSize(new Dimension(140, 45));
        btnDelete.setPreferredSize(new Dimension(140, 45));
        btnRefresh.setPreferredSize(new Dimension(288, 45));
        btnRow.add(btnAdd); btnRow.add(btnDelete); btnRow.add(btnRefresh);

        rightPanel.add(formCard);
        rightPanel.add(Box.createVerticalStrut(25));
        rightPanel.add(btnRow);
        contentArea.add(rightPanel, BorderLayout.EAST);

        // Event Listeners for CRUD actions
        btnAdd.addActionListener(e -> handleAdd());
        btnDelete.addActionListener(e -> handleDelete());
        btnRefresh.addActionListener(e -> refreshTable());
        
        // Table Selection Listener: Automatically fills the form with selected row data
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { populateFieldsFromTable(); }
        });
    }

    /**
     * Styles buttons with modern flat aesthetics, white text, and hover animations.
     * @param btn The JButton to style.
     * @param baseColor The theme color for the button.
     */
    private void styleButton(JButton btn, Color baseColor) {
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Mouse listener for "Brighter on Hover" effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(baseColor.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(baseColor); }
        });
    }

    /**
     * Logic for adding a product. Validates input and provides feedback for database errors.
     */
    private void handleAdd() {
        if (validateInputs()) {
            boolean success = InventoryManager.addProduct(
                    txtBarcode.getText(), txtName.getText(), txtCategory.getText(), 
                    Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtQty.getText())
            );

            if (success) {
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Product saved successfully.");
            } else {
                // Catches SQL integrity violations (e.g., duplicate barcode)
                JOptionPane.showMessageDialog(this, 
                    "Error: Barcode '" + txtBarcode.getText() + "' already exists.", 
                    "Entry Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Logic for deleting a product. Requires user confirmation before database execution.
     */
    private void handleDelete() {
        String code = txtBarcode.getText();
        if (code.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete SKU: " + code + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (InventoryManager.deleteProduct(code)) {
                refreshTable();
                clearFields();
            }
        }
    }

    /**
     * Re-fetches all records from the products table and refreshes the JTable UI.
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        try (Connection conn = InventoryManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{ rs.getString("barcode"), rs.getString("product_name"),
                        rs.getString("category"), rs.getDouble("price"), rs.getInt("stock_quantity") });
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Utility: Maps data from a selected JTable row back to the input fields for editing or deletion.
     */
    private void populateFieldsFromTable() {
        int row = productTable.getSelectedRow();
        if (row != -1) {
            txtBarcode.setText(tableModel.getValueAt(row, 0).toString());
            txtName.setText(tableModel.getValueAt(row, 1).toString());
            txtCategory.setText(tableModel.getValueAt(row, 2).toString());
            txtPrice.setText(tableModel.getValueAt(row, 3).toString());
            txtQty.setText(tableModel.getValueAt(row, 4).toString());
        }
    }

    /**
     * Input Sanitization: Ensures mandatory fields are filled and numeric fields are valid.
     * @return true if all validations pass.
     */
    private boolean validateInputs() {
        try {
            if (txtBarcode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) throw new Exception("Empty fields!");
            Double.parseDouble(txtPrice.getText());
            Integer.parseInt(txtQty.getText());
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please check your inputs (Numbers only for Price/Qty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Utility: Resets the form fields after a successful operation.
     */
    private void clearFields() {
        txtBarcode.setText(""); txtName.setText(""); txtCategory.setText("");
        txtPrice.setText(""); txtQty.setText("");
    }

    /**
     * Simulation of a barcode scan. Searches the database and notifies the user of the result.
     */
    private void processScan() {
        Product p = InventoryManager.getProductByBarcode(scanField.getText());
        if (p != null) JOptionPane.showMessageDialog(this, "Product Identified: " + p.getName());
        else JOptionPane.showMessageDialog(this, "Barcode not found.");
        scanField.setText("");
    }

    /**
     * Application Entry Point: Configures the OS-native Look & Feel and launches the GUI.
     */
    public static void main(String[] args) {
        try { 
            // Makes the UI match the user's Operating System (Windows/Mac/Linux)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}