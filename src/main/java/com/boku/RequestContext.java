package com.boku;

import com.boku.account.AccountHolder;

public class RequestContext {
    private static final ThreadLocal<RequestContext> current = new ThreadLocal<>();
    private final Long currentAccount;

    public static RequestContext get() {
        return current.get();
    }

    private RequestContext(Long currentAccount) {
        this.currentAccount = currentAccount;
    }

    public static void init(Long currentAccount) {
        current.set(new RequestContext(currentAccount));
    }

    public Long getCurrentAccount() {
        return currentAccount;
    }
}
