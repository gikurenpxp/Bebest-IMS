import java.sql.*;

/**
 * InventoryManager.java
 * Purpose: Provides a centralized data access layer for the Bebest Store Management System.
 * This class handles the lifecycle of MySQL connections and executes secure CRUD operations
 * to ensure data integrity across the inventory.
 */
public class InventoryManager {

    /**
     * Establishes a connection to the MySQL database.
     * @return A valid Connection object.
     * @throws Exception if connection strings are invalid or the driver is missing.
     */
    public static Connection connect() throws Exception {
        String url = "jdbc:mysql://localhost:3306/bebest_ims_db"; 
        String user = "root";
        String password = ""; 
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Persists a new product record into the database.
     * @param barcode The unique identifier for the SKU.
     * @param name The descriptive name of the product.
     * @param category The product grouping (e.g., Beverage, Canned Goods).
     * @param price The unit selling price.
     * @param qty The initial quantity available for sale.
     */
    public static void addProduct(String barcode, String name, String category, double price, int qty) {
        // SQL query using placeholders to prevent SQL Injection
        String query = "INSERT INTO products (barcode, product_name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, barcode);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, qty);

            pstmt.executeUpdate(); 
            System.out.println("LOG [Add]: Successfully recorded " + name + " in system.");

        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Add]: " + e.getMessage());
        }
    }

    /**
     * Queries the database for a specific product using its barcode.
     * @param code The scanned barcode string.
     * @return A Product model object if found; otherwise, null.
     */
    public static Product getProductByBarcode(String code) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Map the ResultSet row to a Product Object
                return new Product(
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Search]: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing product's pricing and stock levels.
     * @param barcode Unique barcode of the item to modify.
     * @param newPrice Updated unit price.
     * @param newQty Updated stock quantity.
     */
    public static void updateProductByBarcode(String barcode, double newPrice, int newQty) {
        String query = "UPDATE products SET price = ?, stock_quantity = ? WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, newQty);
            pstmt.setString(3, barcode);

            pstmt.executeUpdate();
            System.out.println("LOG [Update]: Records updated for SKU " + barcode);
        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Update]: " + e.getMessage());
        }
    }

    /**
     * Removes a product record from the database based on its unique barcode.
     * @param barcode The barcode string used as the deletion key.
     */
    public static void deleteProduct(String barcode) {
        String query = "DELETE FROM products WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, barcode); 
            pstmt.executeUpdate();
            System.out.println("LOG [Delete]: Successfully removed product " + barcode);
            
        } catch (Exception e) {
            System.err.println("DATABASE ERROR [Delete]: " + e.getMessage());
        }
    }

    /**
     * Main method used for unit testing the database logic.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Testing database connection...");
            connect();
            System.out.println("Connection successful!");
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}