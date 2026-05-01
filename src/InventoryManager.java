import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

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

    // 3. The MAIN method is where you "run" the code to test it
    public static void main(String[] args) {
        // Here we "call" the method we wrote above
        addProduct("Canned Tuna", "Canned Goods", 35.50, 100);
        addProduct("Instant Noodles", "Pasta/Noodles", 12.00, 200);
    }
}