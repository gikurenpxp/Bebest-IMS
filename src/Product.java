/**
 * Represents a product entity within the inventory system.
 * This is a data-carrying object used to pass product information 
 * between the database and the user interface.
 */
public class Product {
    private String barcode;
    private String name;
    private String category;
    private double price;
    private int stock;

    /**
     * Constructs a new Product with full details.
     * * @param barcode  Unique identifier (Primary Key).
     * @param name     Display name of the product.
     * @param category Product classification (e.g., Electronics, Food).
     * @param price    Unit price.
     * @param stock    Current quantity available in inventory.
     */
    public Product(String barcode, String name, String category, double price, int stock) {
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    /**
     * Overloaded constructor for basic product tracking (if category is not required).
     */
    public Product(String barcode, String name, double price, int stock) {
        this(barcode, name, "Uncategorized", price, stock);
    }

    /** @return The unique barcode identifier. */
    public String getBarcode() { return barcode; }

    /** @return The name of the product. */
    public String getName() { return name; }

    /** @return The category classification. */
    public String getCategory() { return category; }

    /** @return The current unit price. */
    public double getPrice() { return price; }

    /** @return The quantity currently in stock. */
    public int getStock() { return stock; }

    // Setters (Optional - add these if you need to modify the object after creation)
    public void setStock(int stock) { this.stock = stock; }
    public void setPrice(double price) { this.price = price; }
}