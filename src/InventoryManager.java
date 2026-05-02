import java.sql.*;

/**
 * InventoryManager.java
 * Purpose: Handles all backend database interactions for the Bebest Store.
 * This class manages the connection lifecycle and executes CRUD (Create, Read, Update, Delete) operations.
 */
public class InventoryManager {

    /**
     * Establishes a connection to the local MySQL database.
     * @return Connection object to be used for SQL execution.
     * @throws Exception if the driver is missing or credentials are incorrect.
     */
    public static Connection connect() throws Exception {
        // Database credentials - ensures connection to the bebest_ims_db schema
        String url = "jdbc:mysql://localhost:3306/bebest_ims_db"; 
        String user = "root";
        String password = ""; 
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Records a new product into the database inventory.
     * @param barcode Unique identifier from the product packaging.
     * @param name The display name of the item.
     * @param category Grouping for the item (e.g., Canned Goods).
     * @param price Unit price of the item.
     * @param qty Initial stock amount.
     */
    public static void addProduct(String barcode, String name, String category, double price, int qty) {
        // Using Parameterized Query to prevent SQL Injection attacks
        String query = "INSERT INTO products (barcode, product_name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";

        // Try-with-resources: Automatically closes the connection and statement after execution
        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, barcode);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, qty);

            pstmt.executeUpdate(); 
            System.out.println("LOG: Successfully added " + name + " to the database.");

        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Add]: " + e.getMessage());
        }
    }

    /**
     * Retrieves a single product's data based on a barcode scan.
     * Used primarily by the Point of Sale (POS) and Search features.
     * @param code The scanned barcode string.
     * @return A Product object if found, otherwise returns null.
     */
    public static Product getProductByBarcode(String code) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Mapping the database record to a Java Object (Product class)
                return new Product(
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Search]: " + e.getMessage());
        }
        return null; // Signals to the UI that no match was found
    }

    /**
     * Updates the pricing of an existing product via its unique ID.
     */
    public static void updateProductPrice(int id, double newPrice) {
        String query = "UPDATE products SET price = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("LOG: Price updated for Product ID " + id);
            }
        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Update]: " + e.getMessage());
        }
    }

    /**
     * Permanently removes a product from the inventory.
     */
    public static void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("LOG: Product ID " + id + " deleted from system.");
        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Delete]: " + e.getMessage());
        }
    }

    /**
     * Utility method for developer testing.
     * In the final application, this is replaced by UI-triggered actions.
     */
    public static void main(String[] args) {
        // Initial test data injection
        addProduct("48012345", "Canned Tuna", "Canned Goods", 35.50, 100);
        addProduct("48067890", "Instant Noodles", "Pasta/Noodles", 12.00, 200);
    }
}