package com.xylia.domain.accounts.accountsdomain.api;

import com.xylia.domain.accounts.accountsdomain.aggregate.BankAccount;
import com.xylia.domain.accounts.accountsdomain.command.CloseAccountCommand;
import com.xylia.domain.accounts.accountsdomain.command.CreateAccountCommand;
import com.xylia.domain.accounts.accountsdomain.command.DepositMoneyCommand;
import com.xylia.domain.accounts.accountsdomain.command.WithdrawMoneyCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateEntityNotFoundException;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class AccountAPI {

    private final CommandGateway commandGateway;
    private final EventStore eventStore;

    public AccountAPI(CommandGateway commandGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.eventStore = eventStore;
    }

    @PostMapping
    public CompletableFuture<String> createAccount(@RequestBody AccountOwner user) {
        String id = UUID.randomUUID().toString();
        return commandGateway.send(new CreateAccountCommand(id, user.name));
    }

    private static class AccountOwner {
        public String name;
    }

    @GetMapping("{id}/events")
    public List<Object> getEvents(@PathVariable String id) {

        return eventStore.readEvents(id)
                .asStream().map(s -> ((DomainEventMessage) s).getPayload())
                .collect(Collectors.toList());
    }

    @PutMapping(path = "{accountId}/balance")
    public CompletableFuture<String> deposit(@RequestBody double amount, @PathVariable String accountId) {
        if (amount > 0)
            return commandGateway.send(new DepositMoneyCommand(accountId, amount));
        else
            return commandGateway.send(new WithdrawMoneyCommand(accountId, amount));
    }

    @DeleteMapping("{id}")
    public CompletableFuture<String> delete(@PathVariable String id) {
        return commandGateway.send(new CloseAccountCommand(id));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AggregateNotFoundException.class)
    public void notFound() {
    }

}
