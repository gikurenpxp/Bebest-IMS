import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryManager {

    public static Connection connect() throws Exception {
        String url = "jdbc:mysql://localhost:3306/bebest_ims_db"; 
        String user = "root";
        String password = ""; 
        return DriverManager.getConnection(url, user, password);
    }

    // UPDATED: Added barcode to the parameters
    public static void addProduct(String barcode, String name, String category, double price, int qty) {
        String query = "INSERT INTO products (barcode, product_name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, barcode);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, qty);

            pstmt.executeUpdate(); 
            System.out.println("Success: " + name + " added!");

        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    public static Product getProductByBarcode(String code) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Product(
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity") // Changed from 'stock' to 'stock_quantity'
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Keep your update and delete methods as they were...

    public static void main(String[] args) {
        // Test with barcodes!
        addProduct("48012345", "Canned Tuna", "Canned Goods", 35.50, 100);
        addProduct("48067890", "Instant Noodles", "Pasta/Noodles", 12.00, 200);
    }
}