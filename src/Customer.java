public class Customer {
    private int Id;
    private String name;
    private String stateOfResidence;
    private Cart cart;
    private ShippingOption shippingOption;

    public Customer(int Id, String name, String stateOfResidence, ShippingOption shippingOption, Cart cart){
        this.Id = Id;
        this.name = name;
        this.stateOfResidence = stateOfResidence;
        this.shippingOption = shippingOption;
        this.cart = cart;
    }

    public double getCartTotal(){
        return cart.getTotal(stateOfResidence, shippingOption);
    }

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        Id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStateOfResidence() {
        return stateOfResidence;
    }
    public void setStateOfResidence(String stateOfResidence) {
        this.stateOfResidence = stateOfResidence;
    }
    public Cart getCart() {
        return cart;
    }
    public void setCart(Cart cart) {
        this.cart = cart;
    }
    public ShippingOption getShippingOption() {
        return shippingOption;
    }
    public void setShippingOption(ShippingOption shippingOption) {
        this.shippingOption = shippingOption;
    }


}
