import java.sql.*;

/**
 * InventoryManager.java
 * * Purpose: Provides a secure Data Access Object (DAO) for the Bebest IMS.
 * Handles MySQL connection lifecycles and CRUD operations using Parameterized Queries.
 * * @author Bebest Development Team
 * @version 2.1
 */
public class InventoryManager {

    // Database Credentials - Externalized as constants for easy maintenance
    private static final String URL = "jdbc:mysql://localhost:3306/bebest_ims_db"; 
    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    /**
     * Establishes a connection to the MySQL database.
     * @return Connection object.
     * @throws Exception if driver is missing or credentials fail.
     */
    public static Connection connect() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Inserts a new product record into the database.
     * @return true if insertion was successful.
     */
    public static boolean addProduct(String barcode, String name, String category, double price, int qty) {
        String query = "INSERT INTO products (barcode, product_name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, barcode);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, qty);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace(); 
            return false;
        }
    }

    /**
     * Retrieves a single product via barcode. Automatically closes ResultSet.
     * @param code The scanned barcode.
     * @return Product object or null if not found.
     */
    public static Product getProductByBarcode(String code) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Permanently removes a product from the database.
     * @param barcode The unique identifier for deletion.
     * @return true if a record was actually deleted.
     */
    public static boolean deleteProduct(String barcode) {
        String query = "DELETE FROM products WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, barcode); 
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}