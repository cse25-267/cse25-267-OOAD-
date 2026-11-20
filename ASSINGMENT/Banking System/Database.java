// Step 1: Import the database - java.sql is the built-in package used for JDBC
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * All SQL lives here. Every method follows the JDBC steps:
 *   1. Import the database      (the imports above)
 *   2. Load and register driver (connect())
 *   3. Create a connection      (connect())
 *   4. Create a statement       (prepareStatement)
 *   5. Execute the query        (executeUpdate / executeQuery)
 *   6. Process the results      (while rs.next())
 *   7. Close the connection     (try-with-resources closes it automatically)
 */
public final class Database {
    private Database() { }

    // ---- CHANGE THESE to match your MySQL setup ----
    // (Codespaces sets the DB_* environment variables for you, so nothing to change there.)
    private static final String URL      = setting("DB_URL", "jdbc:mysql://localhost:3306/banking_system");
    private static final String USER     = setting("DB_USER", "root");
    private static final String PASSWORD = setting("DB_PASSWORD", "your_password");

    // Uses the environment variable if it is set, otherwise the default above
    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isEmpty()) ? fallback : value;
    }

    // Steps 2 and 3: load the driver and create a connection
    private static Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found. Add mysql-connector-j to the project.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ---------------------------------------------------------------- SAVE

    // Returns true if saved
    public static boolean saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers "
                   + "(customer_id, customer_type, address, first_name, surname, company_name) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {          // Step 4

            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getCustomerType());
            ps.setString(3, customer.getAddress());
            if (customer instanceof IndividualCustomer) {
                IndividualCustomer ind = (IndividualCustomer) customer;
                ps.setString(4, ind.getFirstName());
                ps.setString(5, ind.getSurname());
                ps.setNull(6, java.sql.Types.VARCHAR);
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setString(6, ((CompanyCustomer) customer).getCompanyName());
            }
            ps.executeUpdate();                                          // Step 5
            return true;
        } catch (SQLException e) {
            System.out.println("Database error (saveCustomer): " + e.getMessage());
            return false;
        }                                                                // Step 7
    }

    public static boolean saveAccount(String customerId, Account account) {
        String sql = "INSERT INTO accounts "
                   + "(account_number, customer_id, account_type, branch, balance, "
                   + "employer_name, employer_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setString(2, customerId);
            ps.setString(3, account.getAccountType());
            ps.setString(4, account.getBranch());
            ps.setDouble(5, account.getBalance());
            if (account instanceof ChequeAccount) {
                ChequeAccount cheque = (ChequeAccount) account;
                ps.setString(6, cheque.getEmployerName());
                ps.setString(7, cheque.getEmployerAddress());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
                ps.setNull(7, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error (saveAccount): " + e.getMessage());
            return false;
        }
    }

    // Saves the transaction AND the new balance together (both succeed or neither does)
    public static boolean saveTransaction(String accountNumber, Transaction t, double newBalance) {
        String insert = "INSERT INTO transactions "
                      + "(transaction_id, account_number, txn_date, txn_type, amount) "
                      + "VALUES (?, ?, ?, ?, ?)";
        String update = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try (PreparedStatement ins = con.prepareStatement(insert);
                 PreparedStatement upd = con.prepareStatement(update)) {

                ins.setString(1, t.getTransactionId());
                ins.setString(2, accountNumber);
                ins.setDate(3, java.sql.Date.valueOf(t.getDate()));
                ins.setString(4, t.getType().name());
                ins.setDouble(5, t.getAmount());
                ins.executeUpdate();

                upd.setDouble(1, newBalance);
                upd.setString(2, accountNumber);
                upd.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database error (saveTransaction): " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------- LOAD

    // Reads everything from the database into the Bank's arrays. Returns "OK" or "ERROR: ..."
    public static String loadAll(Bank bank) {
        try (Connection con = connect()) {

            // Customers
            try (PreparedStatement ps = con.prepareStatement(
                         "SELECT * FROM customers ORDER BY customer_id");
                 ResultSet rs = ps.executeQuery()) {                     // Step 5

                while (rs.next()) {                                      // Step 6
                    String id      = rs.getString("customer_id");
                    String address = rs.getString("address");
                    Customer customer;
                    if ("Individual".equals(rs.getString("customer_type"))) {
                        customer = new IndividualCustomer(id, address,
                                rs.getString("first_name"), rs.getString("surname"));
                    } else {
                        customer = new CompanyCustomer(id, address, rs.getString("company_name"));
                    }
                    bank.addLoadedCustomer(customer);
                }
            }

            // Accounts (each one is added to its customer)
            try (PreparedStatement ps = con.prepareStatement(
                         "SELECT * FROM accounts ORDER BY account_number");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Customer owner = bank.findCustomer(rs.getString("customer_id"));
                    if (owner == null) continue;

                    String number  = rs.getString("account_number");
                    String branch  = rs.getString("branch");
                    double balance = rs.getDouble("balance");
                    String type    = rs.getString("account_type");

                    Account account;
                    if ("Savings".equals(type)) {
                        account = new SavingsAccount(number, balance, branch);
                    } else if ("Investment".equals(type)) {
                        account = new InvestmentAccount(number, balance, branch);
                    } else {
                        account = new ChequeAccount(number, balance, branch,
                                rs.getString("employer_name"), rs.getString("employer_address"));
                    }
                    owner.addAccount(account);
                }
            }

            // Transactions (oldest first, added to the matching account)
            try (PreparedStatement ps = con.prepareStatement(
                         "SELECT * FROM transactions ORDER BY seq");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Account account = bank.findAccount(rs.getString("account_number"));
                    if (account == null) continue;

                    LocalDate date = rs.getDate("txn_date").toLocalDate();
                    account.loadTransaction(new Transaction(
                            rs.getString("transaction_id"),
                            date,
                            TransactionType.valueOf(rs.getString("txn_type")),
                            rs.getDouble("amount")));
                }
            }
            return "OK";

        } catch (SQLException e) {                                       // Step 7: connection closed above
            return "ERROR: Could not load from the database: " + e.getMessage();
        }
    }
}
