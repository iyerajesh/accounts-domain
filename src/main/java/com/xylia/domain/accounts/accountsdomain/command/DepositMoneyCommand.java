package com.xylia.domain.accounts.accountsdomain.command;

public class DepositMoneyCommand extends BaseCommand<String> {

    public final double amount;

    public DepositMoneyCommand(String id, double amount) {
        super(id);
        this.amount = amount;
    }
}
