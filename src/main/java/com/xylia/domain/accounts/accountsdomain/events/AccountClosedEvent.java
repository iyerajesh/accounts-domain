package com.xylia.domain.accounts.accountsdomain.events;

import org.springframework.util.Assert;

public class AccountClosedEvent {

    public final String id;

    public AccountClosedEvent(String id) {

        Assert.hasLength(id, "ID cannot be null!");
        this.id = id;
    }
}
