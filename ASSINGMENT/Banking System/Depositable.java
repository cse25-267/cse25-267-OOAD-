public interface Depositable {
    // Returns "OK" or a message starting with "ERROR"
    String deposit(double amount);
}
