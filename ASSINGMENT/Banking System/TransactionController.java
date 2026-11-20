import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class TransactionController {

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Account> accountCombo;
    @FXML private TextField amountField;
    @FXML private Label balanceLabel;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> idCol;
    @FXML private TableColumn<Transaction, LocalDate> dateCol;
    @FXML private TableColumn<Transaction, TransactionType> typeCol;
    @FXML private TableColumn<Transaction, Double> amountCol;

    private Bank bank = Bank.getInstance();

    @FXML
    private void initialize() {
        loadCustomers();

        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("P%.2f", value));
            }
        });
    }

    @FXML
    private void onCustomerSelected() {
        accountCombo.getItems().clear();
        accountCombo.setValue(null);
        loadAccounts();
        refreshDisplay();
    }

    // Reloads the customer list each time the drop-down opens, so new customers appear.
    @FXML
    private void onCustomersShowing() {
        loadCustomers();
    }

    // Reloads the list each time the dropdown opens, so accounts opened in the Accounts tab appear.
    @FXML
    private void onAccountsShowing() {
        loadAccounts();
    }

    @FXML
    private void onAccountSelected() {
        refreshDisplay();
    }

    @FXML
    private void onDeposit() {
        Account account = requireAccount();
        Double amount = readAmount();
        if (account == null || amount == null) return;

        String result = account.deposit(amount);
        if (result.startsWith("ERROR")) {
            Dialogs.error(result.substring(7));
        } else {
            amountField.clear();
            refreshDisplay();
        }
    }

    @FXML
    private void onWithdraw() {
        Account account = requireAccount();
        Double amount = readAmount();
        if (account == null || amount == null) return;

        if (account instanceof Withdrawable) {
            Withdrawable withdrawable = (Withdrawable) account;
            String result = withdrawable.withdraw(amount);
            if (result.startsWith("ERROR")) {
                Dialogs.error(result.substring(7));
            } else {
                amountField.clear();
                refreshDisplay();
            }
        } else {
            Dialogs.error("Account " + account.getAccountNumber() + " does not allow withdrawals.");
        }
    }

    @FXML
    private void onCalculateInterest() {
        Account account = requireAccount();
        if (account == null) return;

        if (account instanceof InterestBearing) {
            InterestBearing bearing = (InterestBearing) account;
            Dialogs.info(String.format("Interest on %s: P%.2f",
                    account.getAccountNumber(), bearing.calculateInterest()));
        } else {
            Dialogs.error("Account " + account.getAccountNumber() + " does not earn interest.");
        }
    }

    // Copies the customers array into the drop-down (keeps the current selection)
    private void loadCustomers() {
        if (customerCombo.getItems().size() == bank.getCustomerCount()) {
            return;   // nothing new
        }
        Customer selected = customerCombo.getValue();
        customerCombo.getItems().clear();
        for (int i = 0; i < bank.getCustomerCount(); i++) {
            customerCombo.getItems().add(bank.getCustomer(i));
        }
        if (selected != null) {
            customerCombo.setValue(selected);
        }
    }

    // Copies the selected customer's accounts array into the account drop-down
    private void loadAccounts() {
        Customer customer = customerCombo.getValue();
        if (customer == null || accountCombo.getItems().size() == customer.getAccountCount()) {
            return;   // nothing new
        }
        Account selected = accountCombo.getValue();
        accountCombo.getItems().clear();
        for (int i = 0; i < customer.getAccountCount(); i++) {
            accountCombo.getItems().add(customer.getAccount(i));
        }
        if (selected != null) {
            accountCombo.setValue(selected);
        }
    }

    private void refreshDisplay() {
        transactionsTable.getItems().clear();
        Account account = accountCombo.getValue();
        if (account == null) {
            balanceLabel.setText("Balance: -");
            return;
        }
        balanceLabel.setText(String.format("Balance: P%.2f", account.getBalance()));
        for (int i = 0; i < account.getTransactionCount(); i++) {
            transactionsTable.getItems().add(account.getTransaction(i));
        }
    }

    private Account requireAccount() {
        Account account = accountCombo.getValue();
        if (account == null) {
            Dialogs.error("Select a customer and an account first.");
        }
        return account;
    }

    private Double readAmount() {
        try {
            return Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            Dialogs.error("Amount must be a number.");
            return null;
        }
    }
}
