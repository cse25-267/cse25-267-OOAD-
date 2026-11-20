import java.time.LocalDate;

public class Transaction {

    // Private fields - Encapsulation
    private String          transactionId;
    private LocalDate       date;
    private TransactionType type;
    private double          amount;

    public Transaction(String transactionId, LocalDate date,
                       TransactionType type, double amount) {
        this.transactionId = transactionId;
        this.date          = date;
        this.type          = type;
        this.amount        = amount;
    }

    // ---- Getters ----
    public String          getTransactionId() { return transactionId; }
    public LocalDate       getDate()          { return date; }
    public TransactionType getType()          { return type; }
    public double          getAmount()        { return amount; }

    @Override
    public String toString() {
        return "Transaction ID : " + transactionId
             + "\nDate           : " + date
             + "\nType           : " + type
             + "\nAmount         : P" + String.format("%.2f", amount);
    }
}
