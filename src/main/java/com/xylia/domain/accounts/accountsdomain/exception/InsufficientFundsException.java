package com.xylia.domain.accounts.accountsdomain.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
