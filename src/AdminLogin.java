import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * AdminLogin.java
 * <p>
 * Provides a sleek, modern login gateway for the BEBEST IMS PRO system.
 * This class serves as the security entry point, featuring:
 * <ul>
 * <li>A split-pane UI design (Branding on the left, Form on the right).</li>
 * <li>Custom color palettes and hover effects to match the system's aesthetic.</li>
 * <li>Keyboard event handling for rapid login using the 'Enter' key.</li>
 * <li>Authentication logic to transition users to the Admin Panel.</li>
 * </ul>
 * * @version 1.0
 */
public class AdminLogin extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // --- Custom Color Palette (Matching the system's UI design) ---
    private final Color SIDEBAR_BG = new Color(21, 34, 46);
    private final Color ACCENT_BLUE = new Color(133, 179, 219);
    private final Color TEXT_DARK = new Color(44, 62, 80);
    private final Color TEXT_MUTED = new Color(149, 165, 166);

    // --- UI Components ---
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    /**
     * Constructs the AdminLogin frame and assembles the split-layout UI.
     */
    public AdminLogin() {
        setupWindow();

        // Create a split layout: Branding on the left, Form on the right
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(createBrandingPanel());
        mainPanel.add(createFormPanel());

        add(mainPanel);
    }

    /**
     * Configures JFrame properties such as size, title, and positioning.
     */
    private void setupWindow() {
        setTitle("BEBEST - Admin Login");
        setSize(600, 400); // Compact size for a login screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers on screen
        setResizable(false);
    }

    // ==========================================
    // 1. LEFT SIDE: BRANDING PANEL
    // ==========================================
    
    /**
     * Creates the branding panel featuring the company logo and versioning.
     * @return A JPanel styled with company colors and branding.
     */
    private JPanel createBrandingPanel() {
        JPanel brandingPanel = new JPanel();
        brandingPanel.setLayout(new GridBagLayout()); // Centers contents easily
        brandingPanel.setBackground(SIDEBAR_BG);

        // Logo and branding text using HTML for stylized multi-line labels
        JLabel lblLogo = new JLabel("<html><center><span style='color:white; font-size:24px;'><b>BEBEST</b></span><br>"
                + "<span style='color:#95A5A6; font-size:12px;'>IMS v1.0</span><br><br>"
                + "<span style='color:#85b3db; font-size:40px;'>⚙</span></center></html>");
        
        brandingPanel.add(lblLogo);
        return brandingPanel;
    }

    // ==========================================
    // 2. RIGHT SIDE: LOGIN FORM
    // ==========================================
    
    /**
     * Creates the interactive login form including input fields and the action button.
     * @return A JPanel containing the username/password fields and login button.
     */
    private JPanel createFormPanel() {
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Form Header Labels
        JLabel lblWelcome = new JLabel("Welcome Back");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(TEXT_DARK);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Please log in to your account");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        formPanel.add(lblWelcome);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(lblSubtitle);
        formPanel.add(Box.createVerticalStrut(30));

        // Initialize Input Fields
        txtUsername = createTextField("Username");
        txtPassword = createPasswordField("Password");

        // Allow pressing 'Enter' on the password field to trigger login automatically
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        formPanel.add(createInputRow("👤 Username", txtUsername));
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createInputRow("🔒 Password", txtPassword));
        formPanel.add(Box.createVerticalStrut(25));

        // Styled Login Button
        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(ACCENT_BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Interactive Hover effect logic
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(ACCENT_BLUE.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(ACCENT_BLUE);
            }
        });

        btnLogin.addActionListener(e -> handleLogin());

        formPanel.add(btnLogin);
        formWrapper.add(formPanel);

        return formWrapper;
    }

    /**
     * Helper to create consistently styled text fields.
     */
    private JTextField createTextField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 224, 228), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    /**
     * Helper to create consistently styled password fields.
     */
    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField txt = new JPasswordField();
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 224, 228), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    /**
     * Creates a vertical input group consisting of an icon label and the input component.
     * @param labelText The text (and icon) to display above the field.
     * @param inputField The JComponent (Text field or Password field).
     * @return A JPanel grouping the label and field.
     */
    private JPanel createInputRow(String labelText, JComponent inputField) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12)); // Emoji font support for icons
        lbl.setForeground(TEXT_DARK);

        row.add(lbl);
        row.add(Box.createVerticalStrut(5));
        row.add(inputField);

        return row;
    }

    // ==========================================
    // 3. AUTHENTICATION LOGIC
    // ==========================================
    
    /**
     * Validates credentials entered by the user.
     * On success: Disposes the login window and launches the Admin Panel.
     * On failure: Displays a security warning and resets the password field.
     */
   private void handleLogin() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());

    // ADMIN
    if (username.equals("admin") && password.equals("admin123")) {
        new AdminPanel().setVisible(true);
        this.dispose();
    }
    // CASHIER
    else if (username.equals("cashier") && password.equals("cashier123")) {
        new CashierPOS().setVisible(true);
        this.dispose();
    }
    else {
        JOptionPane.showMessageDialog(this,
            "Invalid username or password.",
            "Authentication Failed",
            JOptionPane.ERROR_MESSAGE);

        txtPassword.setText("");
        txtPassword.requestFocus();
    }
}

    /**
     * Application entry point. Applies system look and feel and launches the login UI.
     */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception e) { e.printStackTrace(); }
        
        SwingUtilities.invokeLater(() -> new AdminLogin().setVisible(true));
    }
}