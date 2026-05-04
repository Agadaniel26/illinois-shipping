import java.util.HashMap;
import java.util.Map;

public class Cart{
    private int ID;
    private int customerID;
    private Map<Item, Integer> cartItems = new HashMap<>();


    public Cart(int ID, int customerID){
        this.ID = ID;
        this.customerID = customerID; //a cart should be tied to a customer
        this.cartItems = new HashMap<>();
    }

    public void addItemToCart(Item item, int quantity){
        cartItems.merge(item, quantity, Integer::sum);
    }

    public void removeItem(Item item){
        cartItems.remove(item);
    }

    public Map<Item, Integer> viewCart(){
        return cartItems;
    }
    //we need to iterate through the items in the cart and calcualte the total based on the prices and the quantity

    public double getTotal(String state, ShippingOption shippingOption){
        double subTotal = 0;
        for(Map.Entry<Item, Integer> entry : cartItems.entrySet()) {
            subTotal += entry.getKey().getPrice() * entry.getValue();
        }
            //calculate taxes
            double tax = subTotal * calculateTaxes(state);
            double shipping  = calculateShipping(subTotal, shippingOption);

            return subTotal + tax + shipping;

    }

    private double calculateShipping(double subTotal, ShippingOption shippingOption) {
        if(shippingOption == ShippingOption.STANDARD){
            return subTotal > 50.00 ? 0.00 : 10.00;
        } else {
            return 25.00;
        }
    }

    public double getSubTotal() {
        double subTotal = 0;
        for (Map.Entry<Item, Integer> entry : cartItems.entrySet()) {
            subTotal += entry.getKey().getPrice() * entry.getValue();
        }
        return subTotal;
    }

    private  static double calculateTaxes(String state){
            try{
                return Taxes.valueOf(state.toUpperCase()).getRate();
            } catch (IllegalArgumentException e){
                return 0.00;
            }
        }


    public void editQuantity(Item item, int newQuantity){
        cartItems.put(item, newQuantity);
    }

    public void clearCart(){
        cartItems.clear();
    }

    public int getTotalItemCount() {
        int count = 0;
        for (int qty : cartItems.values()) count += qty;
        return count;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public Map<Item, Integer> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Map<Item, Integer> cartItems) {
        this.cartItems = cartItems;
    }


}
