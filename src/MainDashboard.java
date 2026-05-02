import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MainDashboard.java
 * * PURPOSE: 
 * Acts as the primary Graphical User Interface (GUI) for the Bebest Inventory Management System.
 * It facilitates real-time interaction between the user and the MySQL database.
 * * KEY ARCHITECTURAL FEATURES:
 * - Model-View-Controller (MVC) Influence: Separates UI logic from database transactions.
 * - Dynamic Data Typing: Overrides TableModel classes to allow mathematical sorting of currency/stock.
 * - Event-Driven Architecture: Uses ActionListeners and MouseListeners for real-time data synchronization.
 * * @author Bebest Development Team
 * @version 2.2 (Finalized Technical Release)
 */
public class MainDashboard extends JFrame {
    private static final long serialVersionUID = 1L;

    // --- UI Structure Components ---
    private JPanel sidebar, header, contentArea;
    private JTextField scanField, txtName, txtCategory, txtPrice, txtQty, txtBarcode;
    private JTable productTable;
    private DefaultTableModel tableModel;

    /**
     * Constructor: Orchestrates the assembly of the GUI.
     * Sets up window properties, initializes sub-panels, and performs the initial database fetch.
     */
    public MainDashboard() {
        setupWindow();
        initSidebar();
        initHeader();
        initContentArea();
        refreshTable(); // Syncs UI with MySQL on startup
    }

    /**
     * Configures the main JFrame properties.
     * Uses BorderLayout to manage the three primary zones: Sidebar (West), Header (North), and Content (Center).
     */
    private void setupWindow() {
        setTitle("Bebest Store Management System");
        setSize(1100, 700);
        setMinimumSize(new Dimension(1000, 600)); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
    }

    /**
     * Creates the left-hand navigation sidebar.
     * Uses a deep navy theme (33, 47, 61) to provide high contrast against the content area.
     */
    private void initSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 47, 61)); 
        sidebar.setPreferredSize(new Dimension(180, 700));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 40));
        
        JLabel logo = new JLabel("BEBEST IMS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        sidebar.add(logo);
        add(sidebar, BorderLayout.WEST);
    }

    /**
     * Initializes the top header containing the Barcode Scanner simulation.
     * The scanField is programmed to trigger processScan() immediately upon 'Enter'.
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

        scanField.addActionListener(e -> processScan());
    }

    /**
     * Assembles the main dashboard workspace.
     * 1. Configures a custom DefaultTableModel that treats Price/Stock as numbers, not text.
     * 2. Implements a TableRowSorter to enable manual and programmatic sorting.
     * 3. Sets a default SortKey to prioritize high-value inventory at the top.
     */
    private void initContentArea() {
        contentArea = new JPanel(new BorderLayout(25, 0));
        contentArea.setBackground(new Color(242, 244, 244)); 
        contentArea.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        add(contentArea, BorderLayout.CENTER);

        // --- Model Definition with Numeric Logic ---
        String[] columns = {"Barcode", "Name", "Category", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 3: return Double.class;  // Forces numeric sort for Price
                    case 4: return Integer.class; // Forces numeric sort for Stock
                    default: return String.class;
                }
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(35);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- Sorting Engine Configuration ---
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);
        
        // Initial Sort State: Price (Column 3) Descending
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("INVENTORY DATABASE"));
        contentArea.add(scrollPane, BorderLayout.CENTER);

        // --- Right-Side Form Panel ---
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(320, 600));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

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

        // --- Action Control Group ---
        JButton btnAdd = new JButton("ADD PRODUCT");
        styleButton(btnAdd, new Color(40, 180, 99)); 
        
        JButton btnDelete = new JButton("DELETE");
        styleButton(btnDelete, new Color(203, 67, 53)); 

        JButton btnRefresh = new JButton("REFRESH TABLE");
        styleButton(btnRefresh, new Color(52, 152, 219)); 
        
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

        // --- Event Binding ---
        btnAdd.addActionListener(e -> handleAdd());
        btnDelete.addActionListener(e -> handleDelete());
        btnRefresh.addActionListener(e -> refreshTable());
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { populateFieldsFromTable(); }
        });
    }

    /**
     * Standardizes button appearance across the application.
     * Includes a MouseListener to simulate modern web-style hover highlights.
     */
    private void styleButton(JButton btn, Color baseColor) {
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(baseColor.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(baseColor); }
        });
    }

    /**
     * CRUD: CREATE. 
     * Validates form inputs before passing them to the InventoryManager for SQL insertion.
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
                JOptionPane.showMessageDialog(this, 
                    "Error: Barcode '" + txtBarcode.getText() + "' already exists in database.", 
                    "Database Integrity Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * CRUD: DELETE.
     * Identifies the target SKU from the text field and executes removal after user confirmation.
     */
    private void handleDelete() {
        String code = txtBarcode.getText();
        if (code.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete SKU: " + code + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (InventoryManager.deleteProduct(code)) {
                refreshTable();
                clearFields();
            }
        }
    }

    /**
     * DATA SYNC: REFRESH.
     * Clears the current table view and re-populates it with the latest result set from MySQL.
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
            e.printStackTrace(); 
        }
    }

    /**
     * UI HELPER: POPULATE.
     * When a row is selected, this method transfers that data into the side-panel text fields.
     */
    private void populateFieldsFromTable() {
        int row = productTable.getSelectedRow();
        if (row != -1) {
            // Converts internal model index to view index (essential when table is sorted)
            int modelRow = productTable.convertRowIndexToModel(row);
            txtBarcode.setText(tableModel.getValueAt(modelRow, 0).toString());
            txtName.setText(tableModel.getValueAt(modelRow, 1).toString());
            txtCategory.setText(tableModel.getValueAt(modelRow, 2).toString());
            txtPrice.setText(tableModel.getValueAt(modelRow, 3).toString());
            txtQty.setText(tableModel.getValueAt(modelRow, 4).toString());
        }
    }

    /**
     * VALIDATION: Logic check to ensure no null values are sent to the database
     * and that Price/Qty are valid numbers.
     */
    private boolean validateInputs() {
        try {
            if (txtBarcode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) throw new Exception();
            Double.parseDouble(txtPrice.getText());
            Integer.parseInt(txtQty.getText());
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Validation Failed: Ensure all fields are filled and numeric fields are valid.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void clearFields() {
        txtBarcode.setText(""); txtName.setText(""); txtCategory.setText("");
        txtPrice.setText(""); txtQty.setText("");
    }

    /**
     * SIMULATION: Logic for the Barcode Scanner.
     * Searches for a SKU match in the Product list and notifies the user.
     */
    private void processScan() {
        Product p = InventoryManager.getProductByBarcode(scanField.getText());
        if (p != null) JOptionPane.showMessageDialog(this, "MATCH FOUND\nProduct: " + p.getName() + "\nPrice: " + p.getPrice());
        else JOptionPane.showMessageDialog(this, "SKU NOT RECOGNIZED");
        scanField.setText("");
    }

    /**
     * APP LAUNCHER: Sets the look and feel to the system native (Windows/Mac) for a cleaner UI experience.
     */
    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}