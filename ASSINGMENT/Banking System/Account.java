import java.time.LocalDate;

public abstract class Account implements Depositable {

    public static final int MAX_TRANSACTIONS = 100;

    // Private fields - Encapsulation
    private String        accountNumber;
    private String        branch;
    private double        balance;
    private Transaction[] transactions     = new Transaction[MAX_TRANSACTIONS];
    private int           transactionCount = 0;

    // Validation (empty number, negative balance) is done in Bank.openAccount
    protected Account(String accountNumber, double balance, String branch) {
        this.accountNumber = accountNumber;
        this.balance       = balance;
        this.branch        = branch;
    }

    // Implements Depositable - returns "OK" or "ERROR: ..."
    @Override
    public String deposit(double amount) {
        if (amount <= 0) {
            return "ERROR: Deposit amount must be positive.";
        }
        if (transactionCount >= MAX_TRANSACTIONS) {
            return "ERROR: Transaction list is full for account " + accountNumber + ".";
        }
        return recordTransaction(TransactionType.DEPOSIT, amount, balance + amount);
    }

    // Shared withdrawal logic so subclasses don't duplicate it
    protected String performWithdrawal(double amount) {
        if (amount <= 0) {
            return "ERROR: Withdrawal amount must be positive.";
        }
        if (amount > balance) {
            return "ERROR: Insufficient funds in " + accountNumber
                 + ": balance P" + String.format("%.2f", balance)
                 + ", requested P" + String.format("%.2f", amount);
        }
        if (transactionCount >= MAX_TRANSACTIONS) {
            return "ERROR: Transaction list is full for account " + accountNumber + ".";
        }
        return recordTransaction(TransactionType.WITHDRAWAL, amount, balance - amount);
    }

    // Saves to the database first; the balance and list only change if that worked
    private String recordTransaction(TransactionType type, double amount, double newBalance) {
        // Account number in the ID keeps it unique across accounts
        String id = accountNumber + "-TR" + (transactionCount + 1);
        Transaction transaction = new Transaction(id, LocalDate.now(), type, amount);

        if (!Database.saveTransaction(accountNumber, transaction, newBalance)) {
            return "ERROR: Could not save the transaction to the database.";
        }
        balance = newBalance;
        transactions[transactionCount] = transaction;
        transactionCount++;
        return "OK";
    }

    // Used while loading from the database: adds a saved transaction (balance is already correct)
    public void loadTransaction(Transaction transaction) {
        if (transactionCount < MAX_TRANSACTIONS) {
            transactions[transactionCount] = transaction;
            transactionCount++;
        }
    }

    // ---- Getters (names must match the table columns) ----
    public String      getAccountNumber()    { return accountNumber; }
    public String      getBranch()           { return branch; }
    public double      getBalance()          { return balance; }
    public int         getTransactionCount() { return transactionCount; }
    public Transaction getTransaction(int i) { return transactions[i]; }

    // e.g. "Savings", "Investment", "Cheque"
    public String getAccountType() {
        return getClass().getSimpleName().replace("Account", "");
    }

    public void displayTransactions() {
        for (int i = 0; i < transactionCount; i++) {
            System.out.println(transactions[i]);
            System.out.println("----------------");
        }
    }

    @Override
    public String toString() {
        return accountNumber + " (" + getAccountType() + ")";
    }
}
