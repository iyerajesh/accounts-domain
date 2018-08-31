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
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.eventsourcing.eventstore.EventStoreException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class BankAccountTest {

    private FixtureConfiguration fixture;

    @Before
    public void setUp() {
        fixture = new AggregateTestFixture(BankAccount.class);
    }

    @Test
    public void createAccount() {
        fixture.given()
                .when(new CreateAccountCommand("id", "Max"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountCreatedEvent("id", "Max", 0.0));
    }

    @Test
    public void createExistingAccount() {
        fixture.given(new AccountCreatedEvent("id", "Max", 0.0))
                .when(new CreateAccountCommand("id", "Max"))
                .expectException(EventStoreException.class);
    }

    @Test
    public void depositMoney() {
        fixture.given(new AccountCreatedEvent("id", "Max", 0.0))
                .when(new DepositMoneyCommand("id", 12.0))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new MoneyDepositedEvent("id", 12.0));
    }

    @Test
    public void depositMoneyOnInexistentAccount() {
        fixture.given()
                .when(new DepositMoneyCommand("id", 12.0))
                .expectException(AggregateNotFoundException.class);
    }

    @Test
    public void withdrawMoney() {
        fixture.given(new AccountCreatedEvent("id", "Max", 0.0),
                new MoneyDepositedEvent("id", 10.0))
                .when(new WithdrawMoneyCommand("id", 5.0))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new MoneyWithdrawnEvent("id", 5.0));
    }

    @Test
    public void overdrawAccount() {
        fixture.given(new AccountCreatedEvent("id", "Max", 0.0),
                new MoneyDepositedEvent("id", 10.0))
                .when(new WithdrawMoneyCommand("id", 20.0))
                .expectException(InsufficientFundsException.class);
    }

    @Test
    public void closeAccount() {
        fixture.given(new AccountCreatedEvent("id", "Max", 0.0),
                new MoneyDepositedEvent("id", 10.0))
                .when(new CloseAccountCommand("id"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountClosedEvent("id"));
    }

    @Test
    public void createAndDeposit() {
        BankAccount account = new BankAccount();
        account.on(new AccountCreatedEvent("id", "Max", 0.0));
        account.on(new MoneyDepositedEvent("id", 10.0));

        assertEquals(10.0, account.getBalance(), 0);
    }

    @Test
    public void noAggregateLifecycle() {
        BankAccount account = new BankAccount();
        account.on(new AccountCreatedEvent("id", "Max", 0.0));

        assertThrows(IllegalStateException.class, () -> account.on(new DepositMoneyCommand("id", 10.0)));
    }
}