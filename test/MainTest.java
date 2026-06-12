import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Main.resetNextItemId();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private Scanner scannerFrom(String... lines) {
        String input = String.join("\n", lines) + "\n";
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private String output() {
        return outContent.toString();
    }

    // =========================================================================
    // Full flow tests via runApp()
    // =========================================================================

    @Test
    void fullFlow_addViewTotalCheckout() {
        Main.runApp(scannerFrom(
                "Alice", "IL", "1",
                "1", "Apple", "5.00", "2",
                "3",
                "2",
                "6"
        ));
        assertTrue(output().contains("Apple"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_nextDayShipping() {
        Main.runApp(scannerFrom(
                "Bob", "CA", "2",
                "1", "Widget", "20.00", "1",
                "6"
        ));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_editQuantity() {
        Main.runApp(scannerFrom(
                "Carol", "NY", "1",
                "1", "Pen", "3.00", "1",
                "4", "Pen", "5",
                "6"
        ));
        assertTrue(output().contains("updated to 5"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_removeItem() {
        Main.runApp(scannerFrom(
                "Dave", "IL", "1",
                "1", "Mug", "8.00", "1",
                "1", "Cup", "5.00", "1",
                "5", "Mug",
                "6"
        ));
        assertTrue(output().contains("removed"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_invalidMenuChoice() {
        Main.runApp(scannerFrom(
                "Eve", "IL", "1",
                "9",
                "1", "Book", "12.00", "1",
                "6"
        ));
        assertTrue(output().contains("Enter 1-6"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_checkoutEmptyCart_thenCheckout() {
        Main.runApp(scannerFrom(
                "Frank", "IL", "1",
                "6",
                "1", "Lamp", "15.00", "1",
                "6"
        ));
        assertTrue(output().contains("empty") || output().contains("Cannot checkout"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_viewAndTotal_emptyCart() {
        Main.runApp(scannerFrom(
                "Gina", "IL", "1",
                "3",
                "2",
                "1", "Desk", "40.00", "1",
                "6"
        ));
        assertTrue(output().contains("Cart is empty"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_editQuantity_emptyCart() {
        Main.runApp(scannerFrom(
                "Hank", "IL", "1",
                "4",
                "1", "Chair", "25.00", "1",
                "6"
        ));
        assertTrue(output().contains("Cart is empty"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_removeItem_emptyCart() {
        Main.runApp(scannerFrom(
                "Iris", "IL", "1",
                "5",
                "1", "Shelf", "30.00", "1",
                "6"
        ));
        assertTrue(output().contains("Cart is empty"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_blankItemName() {
        Main.runApp(scannerFrom(
                "Jake", "IL", "1",
                "1", "",
                "1", "Eraser", "1.00", "1",
                "6"
        ));
        assertTrue(output().contains("cannot be blank"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_editItem_notFound() {
        Main.runApp(scannerFrom(
                "Kim", "IL", "1",
                "1", "Tape", "3.00", "1",
                "4", "Glue",
                "6"
        ));
        assertTrue(output().contains("not found") || output().contains("Item not found"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_removeItem_notFound() {
        Main.runApp(scannerFrom(
                "Leo", "IL", "1",
                "1", "Clips", "2.00", "1",
                "5", "Pins",
                "6"
        ));
        assertTrue(output().contains("not found") || output().contains("Item not found"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_freeShipping_over50() {
        Main.runApp(scannerFrom(
                "Mia", "IL", "1",
                "1", "Table", "60.00", "1",
                "2",
                "6"
        ));
        assertTrue(output().contains("0.00"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void fullFlow_unknownState_noTax() {
        Main.runApp(scannerFrom(
                "Nick", "TX", "1",
                "1", "Bulb", "5.00", "1",
                "2",
                "6"
        ));
        assertTrue(output().contains("0.00"));
        assertTrue(output().contains("transaction completed"));
    }

    // =========================================================================
    // Input validation loops
    // =========================================================================

    @Test
    void promptState_invalidThenValid() {
        Main.runApp(scannerFrom(
                "Omar", "X", "IL", "1",
                "1", "Pen", "2.00", "1",
                "6"
        ));
        assertTrue(output().contains("valid 2-letter"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptShipping_invalidThenValid() {
        Main.runApp(scannerFrom(
                "Pam", "IL", "9", "1",
                "1", "Brush", "3.00", "1",
                "6"
        ));
        assertTrue(output().contains("Enter 1 or 2"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptPrice_tooLow_thenValid() {
        Main.runApp(scannerFrom(
                "Quinn", "IL", "1",
                "1", "Item", "0.50", "5.00", "1",
                "6"
        ));
        assertTrue(output().contains("at least $1.00"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptPrice_tooHigh_thenValid() {
        Main.runApp(scannerFrom(
                "Rita", "IL", "1",
                "1", "Item", "100000.00", "5.00", "1",
                "6"
        ));
        assertTrue(output().contains("cannot exceed"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptPrice_nonNumeric_thenValid() {
        Main.runApp(scannerFrom(
                "Sam", "IL", "1",
                "1", "Item", "abc", "5.00", "1",
                "6"
        ));
        assertTrue(output().contains("Invalid price"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptQuantity_decimal_thenValid() {
        Main.runApp(scannerFrom(
                "Tina", "IL", "1",
                "1", "Ruler", "2.00", "1.5", "1",
                "6"
        ));
        assertTrue(output().contains("whole number"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptQuantity_zero_thenValid() {
        Main.runApp(scannerFrom(
                "Uma", "IL", "1",
                "1", "Ruler", "2.00", "0", "1",
                "6"
        ));
        assertTrue(output().contains("at least 1"));
        assertTrue(output().contains("transaction completed"));
    }

    @Test
    void promptQuantity_nonNumeric_thenValid() {
        Main.runApp(scannerFrom(
                "Vera", "IL", "1",
                "1", "Ruler", "2.00", "abc", "1",
                "6"
        ));
        assertTrue(output().contains("positive integer"));
        assertTrue(output().contains("transaction completed"));
    }

    // =========================================================================
    // Direct helper method tests (kills mutants on boundary conditions)
    // =========================================================================

    @Test
    void getTaxRate_knownState_returnsRate() {
        assertEquals(0.06, Main.getTaxRate("IL"), 0.001);
    }

    @Test
    void getTaxRate_unknownState_returnsZero() {
        assertEquals(0.0, Main.getTaxRate("ZZ"), 0.001);
    }

    @Test
    void getShippingCost_standard_under50() {
        assertEquals(10.0, Main.getShippingCost(30.0, ShippingOption.STANDARD), 0.001);
    }

    @Test
    void getShippingCost_standard_over50() {
        assertEquals(0.0, Main.getShippingCost(60.0, ShippingOption.STANDARD), 0.001);
    }

    @Test
    void getShippingCost_standard_exactly50() {
        // exactly 50 should still charge shipping (condition is > 50, not >= 50)
        assertEquals(10.0, Main.getShippingCost(50.0, ShippingOption.STANDARD), 0.001);
    }

    @Test
    void getShippingCost_nextDay_anyAmount() {
        assertEquals(25.0, Main.getShippingCost(100.0, ShippingOption.NEXT_DAY), 0.001);
    }

    @Test
    void findItem_found_caseInsensitive() {
        Cart cart = new Cart(1, 1);
        Item item = new Item(1, "Apple", 2.00);
        cart.addItemToCart(item, 1);
        assertEquals(item, Main.findItem(cart, "apple"));
    }

    @Test
    void findItem_notFound_returnsNull() {
        Cart cart = new Cart(1, 1);
        assertNull(Main.findItem(cart, "Ghost"));
    }

    @Test
    void printMenu_printsAllOptions() {
        Main.printMenu();
        String out = output();
        assertTrue(out.contains("1. Add item"));
        assertTrue(out.contains("2. Get current total"));
        assertTrue(out.contains("3. View cart"));
        assertTrue(out.contains("4. Edit item quantity"));
        assertTrue(out.contains("5. Remove item"));
        assertTrue(out.contains("6. Checkout"));
    }
}