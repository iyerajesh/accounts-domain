package com.xylia.domain.accounts.accountsdomain.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;
import static org.springframework.util.Assert.notNull;

public class BaseCommand<T> {

    @TargetAggregateIdentifier
    public final T id;

    public BaseCommand(T id) {
        notNull(id, "ID cannot be null!");
        this.id = id;
    }
}
