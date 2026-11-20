import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class AccountController {

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField accountNumberField, openingBalanceField, branchField;
    @FXML private TextField employerNameField, employerAddressField;
    @FXML private HBox chequeBox;
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> typeCol, numberCol, branchCol;
    @FXML private TableColumn<Account, Double> balanceCol;

    private Bank bank = Bank.getInstance();

    @FXML
    private void initialize() {
        loadCustomers();
        typeCombo.getItems().add("Savings");
        typeCombo.getItems().add("Investment");
        typeCombo.getItems().add("Cheque");
        typeCombo.getSelectionModel().selectFirst();
        onTypeChanged();

        typeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getAccountType()));
        numberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branch"));
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceCol.setCellFactory(col -> new TableCell<Account, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("P%.2f", value));
            }
        });
    }

    @FXML
    private void onTypeChanged() {
        boolean cheque = "Cheque".equals(typeCombo.getValue());
        chequeBox.setVisible(cheque);
        chequeBox.setManaged(cheque);
    }

    @FXML
    private void onCustomerSelected() {
        onRefresh();
    }

    // Reloads the customer list each time the drop-down opens, so new customers appear.
    @FXML
    private void onCustomersShowing() {
        loadCustomers();
    }

    @FXML
    private void onRefresh() {
        accountsTable.getItems().clear();
        Customer customer = customerCombo.getValue();
        if (customer == null) {
            return;
        }
        for (int i = 0; i < customer.getAccountCount(); i++) {
            accountsTable.getItems().add(customer.getAccount(i));
        }
        accountsTable.refresh();
    }

    @FXML
    private void onOpenAccount() {
        Customer customer = customerCombo.getValue();
        if (customer == null) {
            Dialogs.error("Select a customer first.");
            return;
        }

        String number = accountNumberField.getText().trim();
        String branch = branchField.getText().trim();
        if (number.isEmpty() || branch.isEmpty()) {
            Dialogs.error("Account number and branch are required.");
            return;
        }

        try {
            double opening = Double.parseDouble(openingBalanceField.getText().trim());

            String type = typeCombo.getValue();
            Account account;
            if ("Savings".equals(type)) {
                account = new SavingsAccount(number, opening, branch);
            } else if ("Investment".equals(type)) {
                account = new InvestmentAccount(number, opening, branch);
            } else {
                String employerName    = employerNameField.getText().trim();
                String employerAddress = employerAddressField.getText().trim();
                if (employerName.isEmpty() || employerAddress.isEmpty()) {
                    Dialogs.error("Employer name and address are required for a cheque account.");
                    return;
                }
                account = new ChequeAccount(number, opening, branch, employerName, employerAddress);
            }

            String result = bank.openAccount(customer, account);
            if (result.startsWith("ERROR")) {
                Dialogs.error(result.substring(7));   // e.g. investment minimum of P500
            } else {
                onRefresh();
                accountNumberField.clear();
                openingBalanceField.clear();
                employerNameField.clear();
                employerAddressField.clear();
            }
        } catch (NumberFormatException e) {
            Dialogs.error("Opening balance must be a number.");
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
}
