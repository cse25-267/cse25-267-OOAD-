public class ChequeAccount extends Account implements Withdrawable {

    private String employerName;
    private String employerAddress;

    public ChequeAccount(String accountNumber, double balance, String branch,
                         String employerName, String employerAddress) {
        super(accountNumber, balance, branch);
        this.employerName    = employerName;
        this.employerAddress = employerAddress;
    }

    public String getEmployerName()    { return employerName; }
    public String getEmployerAddress() { return employerAddress; }

    @Override
    public String withdraw(double amount) {
        return performWithdrawal(amount);
    }
}
