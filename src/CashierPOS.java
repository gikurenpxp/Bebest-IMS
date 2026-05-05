import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * CashierPOS.java
 * <p>
 * Provides a modern, high-performance Point of Sale (POS) interface for the BEBEST IMS system.
 * This class handles the front-end sales workflow, including:
 * <ul>
 * <li>Real-time barcode scanning and product retrieval via {@link InventoryManager}.</li>
 * <li>Dynamic cart management using a CardLayout to toggle between empty states and data tables.</li>
 * <li>Automatic tax/discount calculations with live UI updates.</li>
 * <li>Database synchronization for inventory deduction upon payment.</li>
 * </ul>
 * * @version 1.0
 */
public class CashierPOS extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // --- Custom Color Palette ---
    private final Color SIDEBAR_BG = new Color(21, 34, 46);
    private final Color SIDEBAR_ACTIVE = new Color(31, 47, 60);
    private final Color MAIN_BG = new Color(244, 246, 248);
    private final Color ACCENT_GREEN = new Color(39, 174, 96);
    private final Color TEXT_DARK = new Color(44, 62, 80);
    private final Color TEXT_MUTED = new Color(149, 165, 166);

    // --- Active UI Components ---
    private JTextField scanField, txtDiscount, txtCash;
    private JLabel lblTotal, lblSub, lblItems, lblChange;
    private JPanel cartArea;
    private CardLayout cartCardLayout;
    private JTable cartTable;
    private DefaultTableModel cartModel;

    /**
     * Constructs the CashierPOS frame and initializes the UI components.
     */
    public CashierPOS() {
        setupWindow();
        
        // Navigation Sidebar
        add(createSidebar(), BorderLayout.WEST);
        
        // Main Wrapper for content and checkout
        JPanel mainWrapper = new JPanel(new BorderLayout(20, 20));
        mainWrapper.setBackground(MAIN_BG);
        mainWrapper.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        mainWrapper.add(createMainContent(), BorderLayout.CENTER);
        mainWrapper.add(createCheckoutPanel(), BorderLayout.EAST);
        
        add(mainWrapper, BorderLayout.CENTER);

        // Bind logic to UI events
        setupActionListeners();
    }

    /**
     * Configures the main window properties such as size, title, and centering.
     */
    private void setupWindow() {
        setTitle("BEBEST - Cashier POS");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // ==========================================
    // 1. UI ASSEMBLY: SIDEBAR
    // ==========================================
    
    /**
     * Creates the left navigation sidebar containing the logo and menu links.
     * @return A styled JPanel representing the sidebar.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 800));

        JPanel topMenu = new JPanel();
        topMenu.setLayout(new BoxLayout(topMenu, BoxLayout.Y_AXIS));
        topMenu.setOpaque(false);

        // Logo Section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setOpaque(false);
        logoPanel.add(new JLabel("<html><b style='color:white; font-size:14px;'>BEBEST</b><br><span style='color:gray; font-size:10px;'>IMS PRO v1.0</span></html>"));
        
        topMenu.add(logoPanel);
        topMenu.add(Box.createVerticalStrut(20));
        
        // Navigation Buttons
        topMenu.add(createSidebarMenuButton("🛒 Cashier POS", true, null));
        topMenu.add(createSidebarMenuButton("⚙ Admin Panel", false, () -> {
            new AdminLogin().setVisible(true); 
            this.dispose(); 
        }));

        sidebar.add(topMenu, BorderLayout.NORTH);

        // Bottom Exit Section
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
     * Helper to create styled sidebar menu items with hover effects.
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
    // 2. UI ASSEMBLY: MAIN CONTENT
    // ==========================================

    /**
     * Creates the central panel containing the scanner input and the shopping cart display.
     * @return A JPanel configured for scanning and viewing items.
     */
    private JPanel createMainContent() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setOpaque(false);

        // --- Header (Title & Scanner) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("🛒 Cashier POS");
        title.setFont(new Font("Segoe UI Emoji", isActive() ? Font.BOLD : Font.PLAIN, 24));
        title.setForeground(TEXT_DARK);
        
        JLabel subtitle = new JLabel("Scan products to add them to the cart");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        scanField = new JTextField(35);
        scanField.setPreferredSize(new Dimension(scanField.getPreferredSize().width, 40));
        scanField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scanField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 224, 228), 1, true), new EmptyBorder(5, 10, 5, 10)));
        
        JLabel lblEnter = new JLabel("   Press Enter to add");
        lblEnter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEnter.setForeground(TEXT_MUTED);

        searchRow.add(scanField);
        searchRow.add(lblEnter);

        headerPanel.add(title);
        headerPanel.add(subtitle);
        headerPanel.add(searchRow);

        // --- Cart Display Area (Uses CardLayout) ---
        cartCardLayout = new CardLayout();
        cartArea = new JPanel(cartCardLayout);
        cartArea.setBackground(Color.WHITE);
        cartArea.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        
        // Empty State View
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setOpaque(false);
        emptyPanel.add(new JLabel("<html><center><span style='font-size:40px; color:#D0D3D4;'>🛒</span><br><br><b style='color:#34495E; font-size:14px;'>Cart is empty</b><br><span style='color:#95A5A6; font-size:11px;'>Scan a barcode to start</span></center></html>"));

        // Active Cart Table View
        String[] columns = {"Barcode", "Item Name", "Qty", "Total (₱)"};
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(35);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        cartTable.getTableHeader().setBackground(new Color(240, 240, 240));
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        cartArea.add(emptyPanel, "EMPTY");
        cartArea.add(scrollPane, "TABLE");

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(cartArea, BorderLayout.CENTER);

        return contentPanel;
    }

    // ==========================================
    // 3. UI ASSEMBLY: CHECKOUT PANEL
    // ==========================================

    /**
     * Creates the right-side summary panel for totals, discounts, and payment buttons.
     * @return A JPanel containing checkout calculation logic visuals.
     */
    private JPanel createCheckoutPanel() {
        JPanel checkoutPanel = new JPanel(new BorderLayout());
        checkoutPanel.setBackground(Color.WHITE);
        checkoutPanel.setPreferredSize(new Dimension(320, 0));
        checkoutPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(230, 230, 230), 1, true), new EmptyBorder(25, 20, 25, 20)));

        JPanel totalsPanel = new JPanel();
        totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
        totalsPanel.setOpaque(false);

        lblSub = new JLabel("SUBTOTAL: ₱0.00");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSub.setForeground(TEXT_MUTED);

        lblTotal = new JLabel("₱0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblTotal.setForeground(ACCENT_GREEN);

        lblItems = new JLabel("0 items");
        lblItems.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblItems.setForeground(TEXT_MUTED);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(300, 1));
        separator.setForeground(new Color(230, 230, 230));

        JLabel lblDiscountLabel = new JLabel("% DISCOUNT");
        lblDiscountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDiscountLabel.setForeground(TEXT_MUTED);

        JPanel discountRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        discountRow.setOpaque(false);
        txtDiscount = new JTextField("0", 18);
        txtDiscount.setHorizontalAlignment(JTextField.CENTER);
        txtDiscount.setPreferredSize(new Dimension(txtDiscount.getPreferredSize().width, 35));
        txtDiscount.setBorder(new LineBorder(new Color(220, 224, 228), 1));
        
        discountRow.add(txtDiscount);
        discountRow.add(new JLabel("  %"));
        
        
        // --- NEW CASH UI ---
        JLabel lblCashLabel = new JLabel("CASH INPUT");
        lblCashLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCashLabel.setForeground(TEXT_MUTED);

        JPanel cashRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cashRow.setOpaque(false);
        txtCash = new JTextField("0", 18);
        txtCash.setHorizontalAlignment(JTextField.CENTER);
        txtCash.setPreferredSize(new Dimension(txtCash.getPreferredSize().width, 35));
        txtCash.setBorder(new LineBorder(new Color(220, 224, 228), 1));
        
        cashRow.add(txtCash);
        

        lblChange = new JLabel("CHANGE: ₱0.00");
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChange.setForeground(TEXT_MUTED);

        totalsPanel.add(lblSub);
        totalsPanel.add(lblTotal);
        totalsPanel.add(lblItems);
        totalsPanel.add(Box.createVerticalStrut(15));
        totalsPanel.add(separator);
        totalsPanel.add(Box.createVerticalStrut(15));
        totalsPanel.add(lblDiscountLabel);
        totalsPanel.add(Box.createVerticalStrut(8));
        totalsPanel.add(discountRow);
        
        // Add Cash and Change to checkout panel
        totalsPanel.add(Box.createVerticalStrut(15));
        totalsPanel.add(lblCashLabel);
        totalsPanel.add(Box.createVerticalStrut(8));
        totalsPanel.add(cashRow);
        totalsPanel.add(Box.createVerticalStrut(15));
        totalsPanel.add(lblChange);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonPanel.setOpaque(false);

        JButton btnPay = createStyledButton("💳 PAY NOW", ACCENT_GREEN, Color.WHITE, true);
        JButton btnClear = createStyledButton("🗑 CLEAR CART", Color.WHITE, TEXT_MUTED, false);

        btnPay.addActionListener(e -> processPayment());
        btnClear.addActionListener(e -> clearCart());

        buttonPanel.add(btnPay);
        buttonPanel.add(btnClear);

        checkoutPanel.add(totalsPanel, BorderLayout.NORTH);
        checkoutPanel.add(buttonPanel, BorderLayout.SOUTH);

        return checkoutPanel;
    }

    /**
     * Helper to generate consistent button styles for the checkout area.
     */
    private JButton createStyledButton(String text, Color bg, Color fg, boolean isSolid) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", isActive() ? Font.BOLD : Font.PLAIN, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(280, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isSolid) {
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
                public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
            });
        } else {
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
        }
        return btn;
    }

    // ==========================================
    // 4. BUSINESS LOGIC & EVENT ROUTING
    // ==========================================

    /**
     * Attaches listeners to the scanner field and discount field to handle user interaction.
     */
    private void setupActionListeners() {
        // Handle Barcode Scanning
        scanField.addActionListener(e -> {
            String barcode = scanField.getText().trim();
            if (!barcode.isEmpty()) {
                Product p = InventoryManager.getProductByBarcode(barcode);
                if (p != null) {
                    addToCart(barcode, p);
                } else {
                    JOptionPane.showMessageDialog(this, "Barcode not found in inventory!", "Scan Error", JOptionPane.ERROR_MESSAGE);
                }
                scanField.setText(""); 
            }
        });

        // Handle Discount Real-time Updates
        txtDiscount.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTotals(); }
            public void removeUpdate(DocumentEvent e) { updateTotals(); }
            public void changedUpdate(DocumentEvent e) { updateTotals(); }
        });

        // Handle Cash Input Real-time Updates for Change
        txtCash.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            public void changedUpdate(DocumentEvent e) { updateChange(); }
        });
    }

    /**
     * Adds an item to the shopping cart model or increments the quantity if already present.
     * @param barcode The unique identifier of the product.
     * @param p The Product object retrieved from the database.
     */
    private void addToCart(String barcode, Product p) {
        cartCardLayout.show(cartArea, "TABLE");

        boolean itemExists = false;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(barcode)) {

                int currentQty = (int) cartModel.getValueAt(i, 2);

                if (currentQty >= p.getStock()) {
                    JOptionPane.showMessageDialog(this, "Not enough stock!", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int newQty = currentQty + 1;
                cartModel.setValueAt(newQty, i, 2);
                cartModel.setValueAt(newQty * p.getPrice(), i, 3);

                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            // 🔥 ADD THIS BLOCK (prevent adding out-of-stock item)
            if (p.getStock() <= 0) {
                JOptionPane.showMessageDialog(this, "Item out of stock!", "Stock Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            cartModel.addRow(new Object[]{ barcode, p.getName(), 1, p.getPrice() });
        }

        updateTotals();
    }

    /**
     * Calculates the subtotal, item count, and final total based on the cart and discount.
     */
    private void updateTotals() {
        double subtotal = 0;
        int totalItems = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalItems += (int) cartModel.getValueAt(i, 2);
            subtotal += (double) cartModel.getValueAt(i, 3);
        }

        double discountPercent = 0;
        try {
            if (!txtDiscount.getText().trim().isEmpty()) {
                discountPercent = Double.parseDouble(txtDiscount.getText().trim());
            }
        } catch (NumberFormatException e) {
            discountPercent = 0;
        }

        // clamp discount
        discountPercent = Math.max(0, Math.min(100, discountPercent));

        double finalTotal = subtotal - (subtotal * (discountPercent / 100.0));

        lblSub.setText(String.format("SUBTOTAL: ₱%.2f", subtotal));
        lblItems.setText(totalItems + " items");
        lblTotal.setText(String.format("₱%.2f", finalTotal));

        // Update change automatically whenever totals update
        updateChange();
    }

    /**
     * Dynamically calculates change based on cash input and current total.
     */
    private void updateChange() {
        try {
            double finalTotal = Double.parseDouble(lblTotal.getText().replace("₱", "").replace(",", ""));
            double cash = 0;
            
            if (!txtCash.getText().trim().isEmpty()) {
                cash = Double.parseDouble(txtCash.getText().trim());
            }

            double change = cash - finalTotal;
            if (change < 0 || finalTotal == 0) {
                lblChange.setText("CHANGE: ₱0.00");
            } else {
                lblChange.setText(String.format("CHANGE: ₱%.2f", change));
            }
        } catch (NumberFormatException e) {
            lblChange.setText("CHANGE: ₱0.00");
        }
    }

    /**
     * Resets the cart UI and data model to its initial empty state.
     */
    private void clearCart() {
        cartModel.setRowCount(0); 
        txtDiscount.setText("0");
        txtCash.setText("0");
        lblChange.setText("CHANGE: ₱0.00");
        updateTotals();
        cartCardLayout.show(cartArea, "EMPTY"); 
        scanField.requestFocus();
    }

    /**
     * Processes the sale by deducting quantities from the database and clearing the cart.
     */
    private void processPayment() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate Cash Payment
        try {
            double finalTotal = Double.parseDouble(lblTotal.getText().replace("₱", "").replace(",", ""));
            double cash = Double.parseDouble(txtCash.getText().trim().isEmpty() ? "0" : txtCash.getText().trim());

            if (cash < finalTotal) {
                JOptionPane.showMessageDialog(this, "Insufficient cash amount!", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid cash amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean allSuccessful = true;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String barcode = cartModel.getValueAt(i, 0).toString();
            int qty = (int) cartModel.getValueAt(i, 2);
            
            // Sync with Database
            boolean deducted = InventoryManager.reduceStock(barcode, qty);
            
            if (!deducted) {
                allSuccessful = false;
                System.err.println("Warning: Failed to deduct stock for item " + barcode);
            }
        }

        if (allSuccessful) {
            JOptionPane.showMessageDialog(this, 
                "Payment processed successfully!\n\nAmount Due: " + lblTotal.getText() + 
                "\nAmount Paid: ₱" + txtCash.getText() + 
                "\n" + lblChange.getText(), 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Payment processed, but some inventory failed to update. Check console logs.", 
                "Partial Success", 
                JOptionPane.WARNING_MESSAGE);
        }
        
        clearCart();
    }

    // ==========================================
    // 5. APPLICATION LAUNCHER
    // ==========================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception e) { e.printStackTrace(); }
        
        SwingUtilities.invokeLater(() -> new CashierPOS().setVisible(true));
    }
}