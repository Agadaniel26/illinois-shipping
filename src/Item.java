import java.util.Objects;

public class Item {
    private int ID;
    private String name;
    private double price;

    private static final double MIN_PRICE = 1.00;
    private static final double MAX_PRICE = 99_999.99;

    public Item(int ID,String name, double price){
        this.name = name;
        if(price < MIN_PRICE || price > MAX_PRICE){
            throw new IllegalArgumentException(
                    String.format("Price must be between $%.2f and $%.2f", MIN_PRICE, MAX_PRICE));
        }
        this.price = price;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return ID == item.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }

    public String toString(){
        return String.format("%-20s: $%.2f", name, price);
    }
}
