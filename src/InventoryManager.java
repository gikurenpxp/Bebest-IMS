import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryManager {

    // 1. Create a "Connection" helper so we don't repeat the URL/User/Pass every time
    public static Connection connect() throws Exception {
        String url = "jdbc:mysql://localhost:3306/bebest_ims_db"; // Double check your port!
        String user = "root";
        String password = ""; 
        return DriverManager.getConnection(url, user, password);
    }

    // 2. This is the METHOD (The Room) that adds the product
    public static void addProduct(String name, String category, double price, int qty) {
        String query = "INSERT INTO products (product_name, category, price, stock_quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // This replaces the "?" with actual data safely
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, qty);

            pstmt.executeUpdate(); // This actually sends the data to MySQL
            System.out.println("Success: " + name + " added to Bebest Store inventory!");

        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }
    
    public static void updateProductPrice(int id, double newPrice) {
        String query = "UPDATE products SET price = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Success: Price updated for Product ID " + id);
            }
        } catch (Exception e) {
            System.out.println("Error updating price: " + e.getMessage());
        }
    }
    
    public static void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);

            pstmt.executeUpdate();
            System.out.println("Success: Product ID " + id + " has been removed.");
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }
    
    public static Product getProductByBarcode(String code) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Return a Product object with name, price, etc.
                return new Product(
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Product not found
    }
    
    

    // 3. The MAIN method is where you "run" the code to test it
    public static void main(String[] args) {
        // Here we "call" the method we wrote above
        addProduct("Canned Tuna", "Canned Goods", 35.50, 100);
        addProduct("Instant Noodles", "Pasta/Noodles", 12.00, 200);
    }
}