/** Single shared store that all controllers read from and write to (arrays only). */
public class Bank {

    public static final int MAX_CUSTOMERS = 20;

    private static final Bank INSTANCE = new Bank();

    private Customer[] customers     = new Customer[MAX_CUSTOMERS];
    private int        customerCount = 0;

    private Bank() { }

    public static Bank getInstance() { return INSTANCE; }

    public int      getCustomerCount() { return customerCount; }
    public Customer getCustomer(int i) { return customers[i]; }

    // Returns "OK" or "ERROR: ..."
    public String addCustomer(Customer customer) {
        if (customerCount >= MAX_CUSTOMERS) {
            return "ERROR: Customer list is full! Maximum " + MAX_CUSTOMERS + " customers allowed.";
        }
        if (findCustomer(customer.getCustomerId()) != null) {
            return "ERROR: Customer ID " + customer.getCustomerId() + " already exists.";
        }
        if (!Database.saveCustomer(customer)) {
            return "ERROR: Could not save the customer to the database.";
        }
        customers[customerCount] = customer;
        customerCount++;
        return "OK";
    }

    // Loads everything saved in the database (call once when the app starts)
    public String loadFromDatabase() {
        return Database.loadAll(this);
    }

    // Used while loading: adds to the array without saving it to the database again
    public void addLoadedCustomer(Customer customer) {
        if (customerCount < MAX_CUSTOMERS) {
            customers[customerCount] = customer;
            customerCount++;
        }
    }

    // Returns null if not found
    public Account findAccount(String accountNumber) {
        for (int i = 0; i < customerCount; i++) {
            for (int j = 0; j < customers[i].getAccountCount(); j++) {
                if (customers[i].getAccount(j).getAccountNumber().equalsIgnoreCase(accountNumber)) {
                    return customers[i].getAccount(j);
                }
            }
        }
        return null;
    }

    // Returns null if not found
    public Customer findCustomer(String customerId) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getCustomerId().equalsIgnoreCase(customerId)) {
                return customers[i];
            }
        }
        return null;
    }

    public boolean accountNumberExists(String accountNumber) {
        for (int i = 0; i < customerCount; i++) {
            for (int j = 0; j < customers[i].getAccountCount(); j++) {
                if (customers[i].getAccount(j).getAccountNumber().equalsIgnoreCase(accountNumber)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Returns "OK" or "ERROR: ..."
    public String openAccount(Customer customer, Account account) {
        if (accountNumberExists(account.getAccountNumber())) {
            return "ERROR: Account number " + account.getAccountNumber() + " already exists.";
        }
        if (account.getBalance() < 0) {
            return "ERROR: Opening balance cannot be negative.";
        }
        if (account instanceof InvestmentAccount
                && account.getBalance() < InvestmentAccount.MIN_OPENING_BALANCE) {
            return "ERROR: Minimum deposit P" + (int) InvestmentAccount.MIN_OPENING_BALANCE + " required.";
        }
        if (customer.getAccountCount() >= Customer.MAX_ACCOUNTS) {
            return "ERROR: This customer already has the maximum of " + Customer.MAX_ACCOUNTS + " accounts.";
        }
        if (!Database.saveAccount(customer.getCustomerId(), account)) {
            return "ERROR: Could not save the account to the database.";
        }
        customer.addAccount(account);
        return "OK";
    }
}
