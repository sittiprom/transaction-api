package com.saya.transaction.api.utils;

public class TransactionConstants {
    public enum AccountStatus {
        ACTIVE,
        FROZEN,
        CLOSED;
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED;

    }
    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }

    public enum AccountType {
        SAVINGS,
        CHECKING,
        CREDIT
    }


}
