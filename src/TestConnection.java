import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        // Change "bebest_db" to your database name
        String url = "jdbc:mysql://localhost:3306/bebest_ims_db"; 
        String user = "root"; // Default MySQL user
        String password = ""; // Your MySQL password

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Success! Bebest Store system is connected to the database.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}