import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartAppTest {

    // ── Shared fixtures ───────────────────────────────────────────────────────
    private Item apple;
    private Item banana;
    private Cart cart;
    private Customer customer;

    @BeforeEach
    void setUp() {
        apple   = new Item(1, "Apple",  2.00);
        banana  = new Item(2, "Banana", 1.50);
        cart    = new Cart(1, 1);
        customer = new Customer(1, "Alice", "IL", ShippingOption.STANDARD, cart);
    }

    // =========================================================================
    // Item Tests
    // =========================================================================

    @Test
    void item_validConstruction() {
        assertEquals(1,       apple.getID());
        assertEquals("Apple", apple.getName());
        assertEquals(2.00,    apple.getPrice(), 0.001);
    }

    @Test
    void item_priceTooLow_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Item(99, "Cheap", 0.50));
    }

    @Test
    void item_priceTooHigh_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Item(99, "Pricey", 100_000.00));
    }

    @Test
    void item_minBoundaryPrice_valid() {
        assertDoesNotThrow(() -> new Item(10, "Min", 1.00));
    }

    @Test
    void item_maxBoundaryPrice_valid() {
        assertDoesNotThrow(() -> new Item(11, "Max", 99_999.99));
    }

    @Test
    void item_setters() {
        apple.setID(99);
        apple.setName("Mango");
        apple.setPrice(5.00);
        assertEquals(99,      apple.getID());
        assertEquals("Mango", apple.getName());
        assertEquals(5.00,    apple.getPrice(), 0.001);
    }

    @Test
    void item_equals_sameID() {
        Item copy = new Item(1, "Apple Copy", 3.00);
        assertEquals(apple, copy);
    }

    @Test
    void item_equals_differentID() {
        assertNotEquals(apple, banana);
    }

    @Test
    void item_equals_null() {
        assertNotEquals(apple, null);
    }

    @Test
    void item_equals_differentClass() {
        assertNotEquals(apple, "not an item");
    }

    @Test
    void item_hashCode_sameID() {
        Item copy = new Item(1, "Apple Copy", 3.00);
        assertEquals(apple.hashCode(), copy.hashCode());
    }

    @Test
    void item_toString_format() {
        String s = apple.toString();
        assertTrue(s.contains("Apple"));
        assertTrue(s.contains("2.00"));
    }

    // =========================================================================
    // Cart Tests
    // =========================================================================

    @Test
    void cart_startsEmpty() {
        assertTrue(cart.getCartItems().isEmpty());
    }

    @Test
    void cart_addItem_singleItem() {
        cart.addItemToCart(apple, 3);
        assertEquals(3, cart.getTotalItemCount());
    }

    @Test
    void cart_addItem_accumulatesQuantity() {
        cart.addItemToCart(apple, 2);
        cart.addItemToCart(apple, 3);
        assertEquals(5, (int) cart.getCartItems().get(apple));
    }

    @Test
    void cart_removeItem() {
        cart.addItemToCart(apple, 2);
        cart.removeItem(apple);
        assertFalse(cart.getCartItems().containsKey(apple));
    }

    @Test
    void cart_editQuantity() {
        cart.addItemToCart(apple, 2);
        cart.editQuantity(apple, 10);
        assertEquals(10, (int) cart.getCartItems().get(apple));
    }

    @Test
    void cart_clearCart() {
        cart.addItemToCart(apple, 2);
        cart.addItemToCart(banana, 1);
        cart.clearCart();
        assertTrue(cart.getCartItems().isEmpty());
    }

    @Test
    void cart_viewCart_returnsSameMap() {
        cart.addItemToCart(apple, 1);
        assertEquals(cart.getCartItems(), cart.viewCart());
    }

    @Test
    void cart_getSubTotal() {
        cart.addItemToCart(apple, 2);   // 4.00
        cart.addItemToCart(banana, 4);  // 6.00
        assertEquals(10.00, cart.getSubTotal(), 0.001);
    }

    @Test
    void cart_getTotalItemCount_multipleItems() {
        cart.addItemToCart(apple, 3);
        cart.addItemToCart(banana, 2);
        assertEquals(5, cart.getTotalItemCount());
    }

    // ── getTotal: tax + shipping combinations ─────────────────────────────────

    @Test
    void cart_getTotal_IL_standard_under50_chargesShipping() {
        cart.addItemToCart(apple, 2); // subtotal = 4.00
        // tax: 4.00 * 0.06 = 0.24, shipping = 10.00  → 14.24
        assertEquals(14.24, cart.getTotal("IL", ShippingOption.STANDARD), 0.001);
    }

    @Test
    void cart_getTotal_IL_standard_over50_freeShipping() {
        cart.addItemToCart(apple, 30); // subtotal = 60.00
        // tax: 60 * 0.06 = 3.60, shipping = 0  → 63.60
        assertEquals(63.60, cart.getTotal("IL", ShippingOption.STANDARD), 0.001);
    }

    @Test
    void cart_getTotal_nextDay_alwaysCharges25() {
        cart.addItemToCart(apple, 30); // subtotal = 60.00
        // tax: 3.60, shipping = 25  → 88.60
        assertEquals(88.60, cart.getTotal("IL", ShippingOption.NEXT_DAY), 0.001);
    }

    @Test
    void cart_getTotal_nextDay_under50_charges25() {
        cart.addItemToCart(apple, 2); // subtotal = 4.00
        // tax: 0.24, shipping = 25  → 29.24
        assertEquals(29.24, cart.getTotal("IL", ShippingOption.NEXT_DAY), 0.001);
    }

    @Test
    void cart_getTotal_CA_taxApplied() {
        cart.addItemToCart(apple, 2); // subtotal = 4.00
        // CA rate = 0.06, tax = 0.24, shipping = 10  → 14.24
        assertEquals(14.24, cart.getTotal("CA", ShippingOption.STANDARD), 0.001);
    }

    @Test
    void cart_getTotal_NY_taxApplied() {
        cart.addItemToCart(apple, 2); // subtotal = 4.00
        // NY rate = 0.06 → 14.24
        assertEquals(14.24, cart.getTotal("NY", ShippingOption.STANDARD), 0.001);
    }

    @Test
    void cart_getTotal_unknownState_noTax() {
        cart.addItemToCart(apple, 2); // subtotal = 4.00
        // no tax, shipping = 10  → 14.00
        assertEquals(14.00, cart.getTotal("ZZ", ShippingOption.STANDARD), 0.001);
    }

    @Test
    void cart_getTotal_unknownState_nextDay() {
        cart.addItemToCart(apple, 2); // subtotal = 4.00, tax = 0, shipping = 25
        assertEquals(29.00, cart.getTotal("ZZ", ShippingOption.NEXT_DAY), 0.001);
    }

    // ── Cart getters / setters ────────────────────────────────────────────────

    @Test
    void cart_gettersSetters() {
        cart.setID(42);
        cart.setCustomerID(99);
        assertEquals(42, cart.getID());
        assertEquals(99, cart.getCustomerID());
    }

    @Test
    void cart_setCartItems() {
        java.util.Map<Item, Integer> newItems = new java.util.HashMap<>();
        newItems.put(apple, 5);
        cart.setCartItems(newItems);
        assertEquals(5, (int) cart.getCartItems().get(apple));
    }

    // =========================================================================
    // Customer Tests
    // =========================================================================

    @Test
    void customer_getCartTotal_delegatesToCart() {
        cart.addItemToCart(apple, 2); // subtotal 4.00 → total 14.24 (IL, STANDARD)
        assertEquals(14.24, customer.getCartTotal(), 0.001);
    }

    @Test
    void customer_gettersSetters() {
        customer.setId(7);
        customer.setName("Bob");
        customer.setStateOfResidence("CA");
        customer.setShippingOption(ShippingOption.NEXT_DAY);

        Cart newCart = new Cart(2, 7);
        customer.setCart(newCart);

        assertEquals(7,                        customer.getId());
        assertEquals("Bob",                    customer.getName());
        assertEquals("CA",                     customer.getStateOfResidence());
        assertEquals(ShippingOption.NEXT_DAY,  customer.getShippingOption());
        assertEquals(newCart,                  customer.getCart());
    }

    // =========================================================================
    // Taxes Enum Tests
    // =========================================================================

    @Test
    void taxes_IL_rate() {
        assertEquals(0.06, Taxes.IL.getRate(), 0.001);
    }

    @Test
    void taxes_CA_rate() {
        assertEquals(0.06, Taxes.CA.getRate(), 0.001);
    }

    @Test
    void taxes_NY_rate() {
        assertEquals(0.06, Taxes.NY.getRate(), 0.001);
    }

    @Test
    void taxes_valueOf_valid() {
        assertEquals(Taxes.IL, Taxes.valueOf("IL"));
    }

    @Test
    void taxes_valueOf_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> Taxes.valueOf("ZZ"));
    }

    // =========================================================================
    // ShippingOption Enum Tests
    // =========================================================================

    @Test
    void shippingOption_values_exist() {
        assertNotNull(ShippingOption.STANDARD);
        assertNotNull(ShippingOption.NEXT_DAY);
    }

    @Test
    void shippingOption_valueOf() {
        assertEquals(ShippingOption.STANDARD, ShippingOption.valueOf("STANDARD"));
        assertEquals(ShippingOption.NEXT_DAY, ShippingOption.valueOf("NEXT_DAY"));
    }
}
