-- Run this once in MySQL (Workbench or the mysql command line) before starting the app.

CREATE DATABASE IF NOT EXISTS banking_system;
USE banking_system;

CREATE TABLE IF NOT EXISTS customers (
    customer_id   VARCHAR(30)  PRIMARY KEY,
    customer_type VARCHAR(20)  NOT NULL,          -- 'Individual' or 'Company'
    address       VARCHAR(150) NOT NULL,
    first_name    VARCHAR(50),
    surname       VARCHAR(50),
    company_name  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number   VARCHAR(30)   PRIMARY KEY,
    customer_id      VARCHAR(30)   NOT NULL,
    account_type     VARCHAR(20)   NOT NULL,      -- 'Savings', 'Investment' or 'Cheque'
    branch           VARCHAR(50)   NOT NULL,
    balance          DECIMAL(15,2) NOT NULL,
    employer_name    VARCHAR(100),
    employer_address VARCHAR(150),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    seq            INT AUTO_INCREMENT PRIMARY KEY,  -- keeps transactions in the order they happened
    transaction_id VARCHAR(50)   NOT NULL UNIQUE,
    account_number VARCHAR(30)   NOT NULL,
    txn_date       DATE          NOT NULL,
    txn_type       VARCHAR(20)   NOT NULL,          -- 'DEPOSIT' or 'WITHDRAWAL'
    amount         DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);
