public class InvestmentAccount extends Account implements Withdrawable, InterestBearing {

    public static final double MIN_OPENING_BALANCE = 500;
    private static final double INTEREST_RATE = 0.05; // 5%

    public InvestmentAccount(String accountNumber, double balance, String branch) {
        super(accountNumber, balance, branch);
    }

    @Override
    public String withdraw(double amount) {
        return performWithdrawal(amount);
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}
