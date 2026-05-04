import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static int nextItemId = 1;

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("         Welcome to the Shop            ");
        System.out.println("========================================");

        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine().trim();

        String state = promptState();
        ShippingOption shipping = promptShippingOption();

        Cart     cart     = new Cart(1, 1);
        Customer customer = new Customer(1, name, state, shipping, cart);

        System.out.printf("%nHello %s! Shipping: %s | State: %s%n", name, shipping, state);

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": handleAddItem(customer);             break;
                case "2": handleGetTotal(customer);            break;
                case "3": handleViewCart(customer);            break;
                case "4": handleEditQuantity(customer);        break;
                case "5": handleRemoveItem(customer);          break;
                case "6": running = !handleCheckout(customer); break;
                default:  System.out.println("Enter 1-6.");
            }
        }

        scanner.close();
    }

    // ── Action handlers ───────────────────────────────────────────────────────

    private static void handleAddItem(Customer customer) {
        System.out.print("\nItem name: ");
        String itemName = scanner.nextLine().trim();
        if (itemName.isEmpty()) { System.out.println("Name cannot be blank."); return; }

        double price = promptPrice(); // validated in Item constructor
        int    qty   = promptQuantity();

        try {
            Item item = new Item(nextItemId++, itemName, price);
            customer.getCart().addItemToCart(item, qty);
            System.out.printf("\"%s\" added. Cart now has %d item(s).%n",
                    itemName, customer.getCart().getTotalItemCount());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleGetTotal(Customer customer) {
        Cart cart = customer.getCart();
        if (cart.getCartItems().isEmpty()) { System.out.println("\nCart is empty."); return; }

        double sub      = cart.getSubTotal();
        double tax      = sub * getTaxRate(customer.getStateOfResidence());
        double shipping = getShippingCost(sub, customer.getShippingOption());
        double total    = sub + tax + shipping;

        System.out.println("\n-------- Current Total --------");
        System.out.printf("Subtotal : $%8.2f%n", sub);
        System.out.printf("Tax      : $%8.2f%n", tax);
        System.out.printf("Shipping : $%8.2f%n", shipping);
        System.out.println("------------------------------");
        System.out.printf("Total    : $%8.2f%n", total);
    }

    private static void handleViewCart(Customer customer) {
        Map<Item, Integer> items = customer.getCart().getCartItems();
        if (items.isEmpty()) { System.out.println("\nCart is empty."); return; }

        System.out.println("\n-------- Cart Contents --------");
        int i = 1;
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            int  qty  = entry.getValue();
            System.out.printf("%d. %-20s qty: %-4d line total: $%.2f%n",
                    i++, item.getName(), qty, item.getPrice() * qty);
        }
        System.out.printf("Subtotal: $%.2f%n", customer.getCart().getSubTotal());
        System.out.println("------------------------------");
    }

    private static void handleEditQuantity(Customer customer) {
        if (customer.getCart().getCartItems().isEmpty()) {
            System.out.println("\nCart is empty."); return;
        }
        handleViewCart(customer);

        System.out.print("Item name to edit: ");
        Item found = findItem(customer.getCart(), scanner.nextLine().trim());
        if (found == null) { System.out.println("Item not found."); return; }

        int newQty = promptQuantity();
        customer.getCart().editQuantity(found, newQty);
        System.out.printf("Quantity for \"%s\" updated to %d.%n", found.getName(), newQty);
    }

    private static void handleRemoveItem(Customer customer) {
        if (customer.getCart().getCartItems().isEmpty()) {
            System.out.println("\nCart is empty."); return;
        }
        handleViewCart(customer);

        System.out.print("Item name to remove: ");
        Item found = findItem(customer.getCart(), scanner.nextLine().trim());
        if (found == null) { System.out.println("Item not found."); return; }

        customer.getCart().removeItem(found);
        System.out.printf("\"%s\" removed. Cart now has %d item(s).%n",
                found.getName(), customer.getCart().getTotalItemCount());
    }

    private static boolean handleCheckout(Customer customer) {
        if (customer.getCart().getCartItems().isEmpty()) {
            System.out.println("\nCannot checkout — cart is empty."); return false;
        }

        handleGetTotal(customer);
        System.out.println("\ntransaction completed");
        return true;
    }

    // ── Input helpers ─────────────────────────────────────────────────────────

    private static String promptState() {
        while (true) {
            System.out.print("State abbreviation (e.g. IL, TX): ");
            String s = scanner.nextLine().trim().toUpperCase();
            if (s.matches("[A-Z]{2}")) return s;
            System.out.println("Enter a valid 2-letter state.");
        }
    }

    private static ShippingOption promptShippingOption() {
        System.out.println("Shipping: 1. Standard ($10, free over $50)  2. Next Day ($25)");
        while (true) {
            System.out.print("Select (1 or 2): ");
            switch (scanner.nextLine().trim()) {
                case "1": return ShippingOption.STANDARD;
                case "2": return ShippingOption.NEXT_DAY;
                default:  System.out.println("Enter 1 or 2.");
            }
        }
    }

    private static double promptPrice() {
        while (true) {
            System.out.print("Unit price ($): ");
            try {
                double p = Double.parseDouble(scanner.nextLine().trim());
                if (p > 0) return p;
                System.out.println("Price must be greater than $0.00.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid price.");
            }
        }
    }

    private static int promptQuantity() {
        while (true) {
            System.out.print("Quantity: ");
            String input = scanner.nextLine().trim();
            if (input.contains(".")) {
                System.out.println("Error: quantity must be a whole number."); continue;
            }
            try {
                int qty = Integer.parseInt(input);
                if (qty >= 1) return qty;
                System.out.println("Error: quantity must be at least 1.");
            } catch (NumberFormatException e) {
                System.out.println("Error: quantity must be a positive integer.");
            }
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static Item findItem(Cart cart, String name) {
        for (Item item : cart.getCartItems().keySet())
            if (item.getName().equalsIgnoreCase(name)) return item;
        return null;
    }

    private static double getTaxRate(String state) {
        try { return Taxes.valueOf(state).getRate(); }
        catch (IllegalArgumentException e) { return 0.0; }
    }

    private static double getShippingCost(double subTotal, ShippingOption option) {
        if (option == ShippingOption.STANDARD) return subTotal > 50.0 ? 0.0 : 10.0;
        return 25.0;
    }

    private static void printMenu() {
        System.out.println("\n========== Menu ==========");
        System.out.println("1. Add item to cart");
        System.out.println("2. Get current total");
        System.out.println("3. View cart");
        System.out.println("4. Edit item quantity");
        System.out.println("5. Remove item");
        System.out.println("6. Checkout");
        System.out.print("Select: ");
    }
}
