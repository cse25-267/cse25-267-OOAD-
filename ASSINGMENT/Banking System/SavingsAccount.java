public class SavingsAccount extends Account implements InterestBearing {

    private static final double INTEREST_RATE = 0.0005; // 0.05%

    public SavingsAccount(String accountNumber, double balance, String branch) {
        super(accountNumber, balance, branch);   // calls Account constructor
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}
