
/**
 * Product.java
 * Purpose: A Model class representing a single inventory item.
 * This class serves as a "Data Transfer Object" (DTO) to move product 
 * information between the database and the User Interface.
 */
public class Product {
    
    // --- Fields ---
    // Using private access modifiers to ensure Encapsulation
    private String name;
    private double price;
    private int stock;

    /**
     * Parameterized Constructor
     * Initializes a new Product object with data retrieved from the database.
     * * @param name  The display name of the product.
     * @param price The unit cost of the product.
     * @param stock The current quantity available in the warehouse/store.
     */
    public Product(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // --- Accessor Methods (Getters) ---
    // These allow other classes to read the data without modifying it directly.

    /**
     * @return The name of the product.
     */
    public String getName() { 
        return name; 
    }

    /**
     * @return The unit price of the product.
     */
    public double getPrice() { 
        return price; 
    }

    /**
     * @return The current stock level (quantity) of the product.
     */
    public int getStock() { 
        return stock; 
    }
}