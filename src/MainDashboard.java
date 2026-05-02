import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * MainDashboard.java
 * * Purpose: Primary Graphical User Interface for the Bebest IMS.
 * Features: Dynamic resizing, input validation, and interactive button styling.
 */
public class MainDashboard extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // UI Structure
    private JPanel sidebar, header, contentArea;
    private JTextField scanField, txtName, txtCategory, txtPrice, txtQty, txtBarcode;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public MainDashboard() {
        setupWindow();
        initSidebar();
        initHeader();
        initContentArea();
        
        refreshTable(); // Initial data load
    }

    private void setupWindow() {
        setTitle("Bebest Store Management System");
        setSize(1100, 700);
        setMinimumSize(new Dimension(1000, 600)); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(new Color(44, 62, 80)); 
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));
        
        JLabel logo = new JLabel("BEBEST IMS");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sidebar.add(logo);
        add(sidebar, BorderLayout.WEST);
    }

    private void initHeader() {
        header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1100, 70));
        
        JLabel lblScan = new JLabel("Barcode Scanner Simulation:");
        lblScan.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        scanField = new JTextField(30);
        
        header.add(lblScan);
        header.add(scanField);
        add(header, BorderLayout.NORTH);

        scanField.addActionListener(e -> processScan());
    }

    private void initContentArea() {
        contentArea = new JPanel(new BorderLayout(25, 0));
        contentArea.setBackground(new Color(236, 240, 241));
        contentArea.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        add(contentArea, BorderLayout.CENTER);

        // --- Table Section ---
        tableModel = new DefaultTableModel(new String[]{"Barcode", "Name", "Category", "Price", "Stock"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        contentArea.add(new JScrollPane(productTable), BorderLayout.CENTER);

        // --- Right Side Control Panel ---
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(320, 600));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setMaximumSize(new Dimension(300, 280));
        form.setOpaque(false);
        form.add(new JLabel("Barcode:")); txtBarcode = new JTextField(); form.add(txtBarcode);
        form.add(new JLabel("Name:"));    txtName = new JTextField();    form.add(txtName);
        form.add(new JLabel("Category:"));txtCategory = new JTextField();form.add(txtCategory);
        form.add(new JLabel("Price:"));   txtPrice = new JTextField();   form.add(txtPrice);
        form.add(new JLabel("Qty:"));     txtQty = new JTextField();     form.add(txtQty);

        // Styled Action Buttons
        JButton btnAdd = new JButton("Add Product");
        styleButton(btnAdd, new Color(46, 204, 113));
        
        JButton btnDelete = new JButton("Delete Product");
        styleButton(btnDelete, new Color(231, 76, 60));

        JButton btnRefresh = new JButton("Refresh Inventory");
        styleButton(btnRefresh, new Color(52, 152, 219));
        
        // Button Layout
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRow.setOpaque(false);
        btnAdd.setPreferredSize(new Dimension(140, 45));
        btnDelete.setPreferredSize(new Dimension(140, 45));
        btnRefresh.setPreferredSize(new Dimension(290, 45));
        btnRow.add(btnAdd); btnRow.add(btnDelete); btnRow.add(btnRefresh);

        rightPanel.add(form);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(btnRow);
        contentArea.add(rightPanel, BorderLayout.EAST);

        // Event Listeners
        btnAdd.addActionListener(e -> handleAdd());
        btnDelete.addActionListener(e -> handleDelete());
        btnRefresh.addActionListener(e -> refreshTable());
        
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { populateFieldsFromTable(); }
        });
    }

    /**
     * Applies professional styling and hover effects to buttons.
     */
    private void styleButton(JButton btn, Color baseColor) {
        btn.setBackground(baseColor);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(baseColor.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(baseColor); }
        });
    }

    private void handleAdd() {
        if (validateInputs()) {
            boolean success = InventoryManager.addProduct(txtBarcode.getText(), txtName.getText(), 
                                        txtCategory.getText(), Double.parseDouble(txtPrice.getText()), 
                                        Integer.parseInt(txtQty.getText()));
            if (success) {
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Product added to database.");
            }
        }
    }

    private void handleDelete() {
        String code = txtBarcode.getText();
        if (code.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm delete SKU: " + code, "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (InventoryManager.deleteProduct(code)) {
                refreshTable();
                clearFields();
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try (Connection conn = InventoryManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{ rs.getString("barcode"), rs.getString("product_name"),
                    rs.getString("category"), rs.getDouble("price"), rs.getInt("stock_quantity") });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void populateFieldsFromTable() {
        int row = productTable.getSelectedRow();
        txtBarcode.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        txtCategory.setText(tableModel.getValueAt(row, 2).toString());
        txtPrice.setText(tableModel.getValueAt(row, 3).toString());
        txtQty.setText(tableModel.getValueAt(row, 4).toString());
    }

    private boolean validateInputs() {
        try {
            if (txtBarcode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) throw new Exception("Empty fields!");
            Double.parseDouble(txtPrice.getText());
            Integer.parseInt(txtQty.getText());
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please check your inputs (Price and Qty must be numbers).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void clearFields() {
        txtBarcode.setText(""); txtName.setText(""); txtCategory.setText("");
        txtPrice.setText(""); txtQty.setText("");
    }

    private void processScan() {
        Product p = InventoryManager.getProductByBarcode(scanField.getText());
        if (p != null) JOptionPane.showMessageDialog(this, "Scan Success: " + p.getName());
        else JOptionPane.showMessageDialog(this, "SKU not found.");
        scanField.setText("");
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}