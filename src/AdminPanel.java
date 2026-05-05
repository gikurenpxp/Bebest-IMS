import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.TableRowSorter;

/**
 * AdminPanel.java
 * <p>
 * This class serves as the primary administrative dashboard for the BEBEST IMS PRO system.
 * It provides a comprehensive interface for managing the product database, including 
 * adding, viewing, and deleting inventory items.
 * <p>
 * Key features include:
 * <ul>
 * <li>Dynamic data retrieval from SQLite via {@link InventoryManager}.</li>
 * <li>A sophisticated JTable implementation
 * badges and low-stock visual alerts.</li>
 * <li>A sorting mechanism that remains synchronized with the database during deletion.</li>
 * <li>A sidebar-based navigation system for switching between POS and Admin modes.</li>
 * </ul>
 * * @version 1.0
 */
public class AdminPanel extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // --- Custom Color Palette (Modern/Clean aesthetic) ---
    private final Color SIDEBAR_BG = new Color(21, 34, 46);
    private final Color SIDEBAR_ACTIVE = new Color(31, 47, 60);
    private final Color MAIN_BG = new Color(244, 246, 248);
    private final Color ACCENT_GREEN = new Color(39, 174, 96);
    private final Color TEXT_DARK = new Color(44, 62, 80);
    private final Color TEXT_MUTED = new Color(149, 165, 166);
    private final Color DANGER_RED = new Color(231, 76, 60);

    // --- UI Components ---
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtBarcode, txtName, txtCategory, txtPrice, txtStock;

    /**
     * Initializes the AdminPanel, sets up the layout, and populates initial data.
     */
    public AdminPanel() {
        setupWindow();

        // Add Navigation Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Main Content Area Wrapper
        JPanel mainWrapper = new JPanel(new BorderLayout(20, 20));
        mainWrapper.setBackground(MAIN_BG);
        mainWrapper.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Add Central Table and Right-side Form
        mainWrapper.add(createMainContent(), BorderLayout.CENTER);
        mainWrapper.add(createAddProductForm(), BorderLayout.EAST);

        add(mainWrapper, BorderLayout.CENTER);
        
        // Load initial records from the database
        refreshTableData();
    }

    /**
     * Configures JFrame properties like title, size, and centering.
     */
    private void setupWindow() {
        setTitle("BEBEST - Admin Panel");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // ==========================================
    // 1. SIDEBAR ASSEMBLY
    // ==========================================
    
    /**
     * Constructs the sidebar navigation.
     * @return A JPanel containing the logo and navigation links.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 800));

        JPanel topMenu = new JPanel();
        topMenu.setLayout(new BoxLayout(topMenu, BoxLayout.Y_AXIS));
        topMenu.setOpaque(false);

        // Branding / Logo Section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setOpaque(false);
        logoPanel.add(new JLabel("<html><b style='color:white; font-size:14px;'>BEBEST</b><br><span style='color:gray; font-size:10px;'>IMS PRO v1.0</span></html>"));

        topMenu.add(logoPanel);
        topMenu.add(Box.createVerticalStrut(20));
        
        // Navigation Buttons
        topMenu.add(createSidebarMenuButton("🛒 Cashier POS", false, () -> {
            new CashierPOS().setVisible(true);
            this.dispose(); 
        }));
        
        topMenu.add(createSidebarMenuButton("⚙ Admin Panel", true, null));

        sidebar.add(topMenu, BorderLayout.NORTH);

        // Footer / Sign Out Section
        JPanel bottomMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        bottomMenu.setOpaque(false);
        JLabel lblSignOut = new JLabel("❌ Quit");
        lblSignOut.setForeground(Color.WHITE);
        lblSignOut.setFont(new Font("Segoe UI Emoji", isActive() ? Font.BOLD : Font.PLAIN, 14));
        lblSignOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblSignOut.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.exit(0); 
            }
        });
        
        bottomMenu.add(lblSignOut);
        sidebar.add(bottomMenu, BorderLayout.SOUTH);
        return sidebar;
    }

    /**
     * Helper to create sidebar menu items with hover effects and click actions.
     */
    private JPanel createSidebarMenuButton(String text, boolean isActive, Runnable onClick) {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        btnPanel.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        btnPanel.setMaximumSize(new Dimension(220, 45));

        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI Emoji", isActive ? Font.BOLD : Font.PLAIN, 14));
        btnPanel.add(lbl);
        
        if (!isActive && onClick != null) {
            btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { onClick.run(); }
                public void mouseEntered(MouseEvent e) { btnPanel.setBackground(SIDEBAR_ACTIVE); }
                public void mouseExited(MouseEvent e) { btnPanel.setBackground(SIDEBAR_BG); }
            });
        }
        return btnPanel;
    }

    @SuppressWarnings("unused")
	private JPanel createSidebarMenuButton(String text, boolean isActive) {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        btnPanel.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        btnPanel.setMaximumSize(new Dimension(220, 45));

        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 14));
        btnPanel.add(lbl);
        return btnPanel;
    }

    // ==========================================
    // 2. MAIN CONTENT (Product Table)
    // ==========================================
    
    /**
     * Builds the center content area, including the product table and its custom renders.
     * @return A JPanel containing the product management table.
     */
    private JPanel createMainContent() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setOpaque(false);

        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel title = new JLabel("Admin Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        
        JLabel subtitle = new JLabel("Manage your product inventory");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        
        titlePanel.add(title);
        titlePanel.add(subtitle);

        JButton btnRefresh = new JButton("↩ Refresh");
        btnRefresh.setFont(new Font("Segoe UI Emoji", isActive() ? Font.BOLD : Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(new LineBorder(new Color(220, 224, 228), 1, true));
        btnRefresh.setPreferredSize(new Dimension(100, 35));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshTableData());

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        // --- Table Section ---
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));

        String[] columns = {"BARCODE", "NAME", "CATEGORY", "PRICE", "STOCK", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        productTable = new JTable(tableModel);
        
        // Sorting Support
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);

        // Table Styling
        productTable.setRowHeight(40);
        productTable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        productTable.setGridColor(new Color(245, 245, 245));
        productTable.setShowVerticalLines(false);
        
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        productTable.getTableHeader().setForeground(TEXT_MUTED);
        productTable.getTableHeader().setBackground(Color.WHITE);
        productTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Column Renderers
        productTable.getColumnModel().getColumn(2).setCellRenderer(new CategoryBadgeRenderer());
        productTable.getColumnModel().getColumn(4).setCellRenderer(new StockRenderer());
        productTable.getColumnModel().getColumn(5).setMaxWidth(50);
        productTable.getColumnModel().getColumn(5).setCellRenderer(new DeleteIconRenderer());
        
        // Deletion Event Handling
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = productTable.getColumnModel().getColumnIndexAtX(e.getX());
                int viewRow = productTable.rowAtPoint(e.getPoint()); // Fixed to get row based on point
                
                if (viewRow < productTable.getRowCount() && viewRow >= 0 && column == 5) {
                    // Convert visual row to actual data model row to handle sorted views correctly
                    int modelRow = productTable.convertRowIndexToModel(viewRow);
                    String barcode = tableModel.getValueAt(modelRow, 0).toString();
                    handleDeleteClick(barcode);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableContainer.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(tableContainer, BorderLayout.CENTER);

        return contentPanel;
    }

    // ==========================================
    // 3. PRODUCT FORM (Right-side management)
    // ==========================================
    
    /**
     * Builds the input form for adding new items.
     * @return A JPanel containing the input fields and save button.
     */
    private JPanel createAddProductForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(320, 0));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel formTitle = new JLabel("ADD PRODUCT");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(TEXT_MUTED);
        
        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(20));

        // Data Entry Fields
        txtBarcode = createFormField(formPanel, "Barcode", "e.g. 001");
        txtName = createFormField(formPanel, "Name", "Product name");
        txtCategory = createFormField(formPanel, "Category", "e.g. Beverages");
        txtPrice = createFormField(formPanel, "Price (₱)", "0.00");
        txtStock = createFormField(formPanel, "Stock", "0");

        formPanel.add(Box.createVerticalStrut(10));

        JButton btnSave = new JButton("💾 SAVE PRODUCT");
        btnSave.setFont(new Font("Segoe UI Emoji", isActive() ? Font.BOLD : Font.PLAIN, 13));
        btnSave.setBackground(ACCENT_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setOpaque(true);
        btnSave.setPreferredSize(new Dimension(280, 45));
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSave.addActionListener(e -> handleSaveProduct());

        formPanel.add(btnSave);
        return formPanel;
    }

    /**
     * Helper method to generate consistent vertical labels and styled text fields.
     */
    private JTextField createFormField(JPanel parent, String labelText, String placeholder) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_DARK);
        
        JTextField txt = new JTextField();
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 224, 228), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(5));
        parent.add(txt);
        parent.add(Box.createVerticalStrut(15));
        
        return txt;
    }

    // ==========================================
    // 4. DATABASE & CRUD LOGIC
    // ==========================================
    
    /**
     * Syncs the JTable with the current state of the SQLite database.
     */
    private void refreshTableData() {
        tableModel.setRowCount(0);
        try (Connection conn = InventoryManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
             
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("barcode"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    "₱" + String.format("%.2f", rs.getDouble("price")),
                    rs.getInt("stock_quantity"),
                    "🗑" // Glyph placeholder for deletion
                });
            }
        } catch (Exception e) {
            System.err.println("Database connection failed or table is empty.");
        }
    }

    /**
     * Validates input fields and saves a new product to the database via {@link InventoryManager}.
     */
    private void handleSaveProduct() {
        try {
            String barcode = txtBarcode.getText().trim();
            String name = txtName.getText().trim();
            String category = txtCategory.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            if (barcode.isEmpty() || name.isEmpty() || category.isEmpty()) {
                throw new IllegalArgumentException("Fields cannot be empty.");
            }

            boolean success = InventoryManager.addProduct(barcode, name, category, price, stock);
            
            if (success) {
                refreshTableData();
                txtBarcode.setText(""); txtName.setText(""); txtCategory.setText("");
                txtPrice.setText(""); txtStock.setText("");
                JOptionPane.showMessageDialog(this, "Product saved successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save. Does this barcode already exist?", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Triggers a confirmation dialog and deletes the specified record by barcode.
     */
    private void handleDeleteClick(String barcode) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete product " + barcode + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (InventoryManager.deleteProduct(barcode)) {
                refreshTableData();
            }
        }
    }

    // ==========================================
    // 5. CUSTOM TABLE RENDERING LOGIC
    // ==========================================
    
    /**
     * Custom renderer that turns the Category text into a green Pill/Badge.
     */
    class CategoryBadgeRenderer extends JPanel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private JLabel label;

        public CategoryBadgeRenderer() {
            setLayout(new GridBagLayout()); 
            setBackground(Color.WHITE);
            label = new JLabel();
            label.setOpaque(true);
            label.setBackground(ACCENT_GREEN);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setBorder(new EmptyBorder(4, 10, 4, 10)); 
            add(label);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) setBackground(table.getSelectionBackground());
            else setBackground(Color.WHITE);
            
            label.setText(value != null ? value.toString() : "");
            return this;
        }
    }

    /**
     * Custom renderer that highlights low stock levels (<= 10) in red.
     */
    class StockRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int stock = value != null ? Integer.parseInt(value.toString()) : 0;
            
            if (stock <= 10) {
                c.setForeground(DANGER_RED);
            } else {
                c.setForeground(TEXT_DARK);
            }
            return c;
        }
    }

    /**
     * Custom renderer for the deletion column using Emoji/Glyph symbols.
     */
    class DeleteIconRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		public DeleteIconRenderer() {
            setHorizontalAlignment(CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setForeground(TEXT_MUTED);
            c.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); 
            return c;
        }
    }

    /**
     * Launches the Admin Panel using the system Look and Feel.
     */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new AdminPanel().setVisible(true));
    }
}
