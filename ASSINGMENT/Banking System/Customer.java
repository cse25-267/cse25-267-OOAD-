public abstract class Customer {

    public static final int MAX_ACCOUNTS = 10;

    // Private fields - Encapsulation
    private String    customerId;
    private String    address;
    private Account[] accounts     = new Account[MAX_ACCOUNTS];
    private int       accountCount = 0;

    protected Customer(String customerId, String address) {
        this.customerId = customerId;
        this.address    = address;
    }

    public abstract String getDisplayName();
    public abstract String getCustomerType();

    // Returns true if the account was added, false if the list is full
    public boolean addAccount(Account account) {
        if (account == null || accountCount >= MAX_ACCOUNTS) {
            return false;
        }
        accounts[accountCount] = account;
        accountCount++;
        return true;
    }

    // ---- Getters ----
    public String  getCustomerId()   { return customerId; }
    public String  getAddress()      { return address; }
    public int     getAccountCount() { return accountCount; }
    public Account getAccount(int i) { return accounts[i]; }

    public void displayAccounts() {
        for (int i = 0; i < accountCount; i++) {
            System.out.println(accounts[i].getAccountNumber()
                + " Balance: P" + String.format("%.2f", accounts[i].getBalance()));
        }
    }

    // Shown in the drop-down lists
    @Override
    public String toString() {
        return customerId + " - " + getDisplayName();
    }
}
