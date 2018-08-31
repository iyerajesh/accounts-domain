package com.xylia.domain.accounts.accountsdomain.command;

public class WithdrawMoneyCommand extends BaseCommand<String> {

    public final double amount;

    public WithdrawMoneyCommand(String id, double amount) {
        super(id);
        this.amount = amount;
    }
}
