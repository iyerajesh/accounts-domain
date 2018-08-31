package com.xylia.domain.accounts.accountsdomain.aggregate;

import com.xylia.domain.accounts.accountsdomain.command.CloseAccountCommand;
import com.xylia.domain.accounts.accountsdomain.command.CreateAccountCommand;
import com.xylia.domain.accounts.accountsdomain.command.DepositMoneyCommand;
import com.xylia.domain.accounts.accountsdomain.command.WithdrawMoneyCommand;
import com.xylia.domain.accounts.accountsdomain.events.AccountClosedEvent;
import com.xylia.domain.accounts.accountsdomain.events.AccountCreatedEvent;
import com.xylia.domain.accounts.accountsdomain.events.MoneyDepositedEvent;
import com.xylia.domain.accounts.accountsdomain.events.MoneyWithdrawnEvent;
import com.xylia.domain.accounts.accountsdomain.exception.InsufficientFundsException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert;

import java.io.Serializable;

import static org.axonframework.commandhandling.model.AggregateLifecycle.*;
import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class BankAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @AggregateIdentifier
    private String id;
    private double balance;
    private String owner;

    public BankAccount() {
        // constructor needed for reconstruction
    }

    @CommandHandler
    public BankAccount(CreateAccountCommand createAccountCommand) {

        Assert.hasLength(createAccountCommand.id, "ID is missing!");
        Assert.hasLength(createAccountCommand.accountCreator, "Account owner is missing!");

        apply(new AccountCreatedEvent(createAccountCommand.id, createAccountCommand.accountCreator, 0));
    }

    @EventSourcingHandler
    protected void on(AccountCreatedEvent accountCreatedEvent) {
        this.id = accountCreatedEvent.id;
        this.owner = accountCreatedEvent.accountCreator;
        this.balance = accountCreatedEvent.balance;
    }

    @CommandHandler
    protected void on(DepositMoneyCommand depositMoneyCommand) {
        Assert.isTrue(depositMoneyCommand.amount > 0.0, "Deposited amount must be greater than 0!");
        apply(new MoneyDepositedEvent(depositMoneyCommand.id, depositMoneyCommand.amount));
    }

    @EventSourcingHandler
    protected void on(MoneyDepositedEvent moneyDepositedEvent) {
        this.balance += moneyDepositedEvent.amount;
    }

    @CommandHandler
    protected void on(CloseAccountCommand closeAccountCommand) {
        apply(new AccountClosedEvent(closeAccountCommand.id));
    }

    @EventSourcingHandler
    protected void on(AccountClosedEvent accountClosedEvent) {
        markDeleted();
    }

    @CommandHandler
    protected void on(WithdrawMoneyCommand withdrawMoneyCommand) {
        Assert.isTrue(withdrawMoneyCommand.amount > 0.0, "Withdrawal amount should be greater than 0!");

        if (balance - withdrawMoneyCommand.amount < 0.0)
            throw new InsufficientFundsException("Insufficient Funds, trying to withdraw amount:" + withdrawMoneyCommand.amount);

        apply(new MoneyWithdrawnEvent(withdrawMoneyCommand.id, withdrawMoneyCommand.amount));
    }

    @EventSourcingHandler
    protected void on(MoneyWithdrawnEvent moneyWithdrawnEvent) {
        this.balance -= moneyWithdrawnEvent.amount;
    }

    public double getBalance() {
        return this.balance;
    }
}
