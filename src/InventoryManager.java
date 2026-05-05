import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages database operations for the Inventory Management System.
 * Handles CRUD operations and stock adjustments for the 'products' table.
 * * @author BEBEST DEVELOPER
 * @version 1.0
 */
public class InventoryManager {

    private static final String URL = "jdbc:mysql://localhost:3306/bebest_ims_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Establishes a connection to the MySQL database.
     * * @return A {@link Connection} object to the database.
     * @throws Exception If database access fails or credentials are incorrect.
     */
    public static Connection connect() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Inserts a new product record into the database.
     * * @param barcode  Unique identifier for the product.
     * @param name     Name of the product.
     * @param category Category classification.
     * @param price    Unit price of the product.
     * @param qty      Initial stock quantity.
     * @return {@code true} if insertion was successful; {@code false} if barcode exists or an error occurred.
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

            pstmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Barcode already exists. Product not added.");
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increments the stock quantity for a specific product.
     * * @param barcode       The barcode of the product to update.
     * @param additionalQty The amount to add to existing stock.
     * @return {@code true} if the update was successful and the product exists.
     */
    public static boolean addStockOnly(String barcode, int additionalQty) {
        String query = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, additionalQty);
            pstmt.setString(2, barcode);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the name, category, and price of an existing product.
     * * @param barcode  The barcode identifying the product.
     * @param name     New name of the product.
     * @param category New category of the product.
     * @param price    New unit price.
     * @return {@code true} if the product was updated; {@code false} otherwise.
     */
    public static boolean updateProduct(String barcode, String name, String category, double price) {
        String query = "UPDATE products SET product_name = ?, category = ?, price = ? WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setString(4, barcode);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decreases the stock quantity for a specific product.
     * * @param barcode The barcode of the product.
     * @param qty     The amount to subtract from stock.
     * @return {@code true} if the update was successful.
     */
    public static boolean reduceStock(String barcode, int qty) {
        String query = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, qty);
            pstmt.setString(2, barcode);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a single product's details based on its barcode.
     * * @param code The barcode to search for.
     * @return A {@link Product} object if found, or {@code null} if no match exists.
     */
    public static Product getProductByBarcode(String code) {
        String query = "SELECT * FROM products WHERE barcode = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, code);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getString("barcode"),
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
     * Fetches all products currently stored in the database.
     * * @return A {@link List} of all {@link Product} objects.
     */
    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new Product(
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Permanently deletes a product record from the database.
     * * @param barcode The barcode of the product to be removed.
     * @return {@code true} if the record was deleted; {@code false} if it didn't exist or an error occurred.
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
