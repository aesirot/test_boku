package com.boku.account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ZERO;

public class AccountHolder {

    private final Map<Long, BigDecimal> balance = new ConcurrentHashMap<>();
    private final Map<Long, Long> locks = new ConcurrentHashMap<>();//lock identity

    public boolean processTransfer(Long accountFrom, Long accountTo, BigDecimal amount) {
        if (amount.compareTo(ZERO) <= 0) {
            // error
            return false;
        }

        Object lock1;
        Object lock2;

        if (accountFrom.compareTo(accountTo) <= 0) {
            lock1 = locks.computeIfAbsent(accountFrom, (a) -> a);
            lock2 = locks.computeIfAbsent(accountTo, (a) -> a);
        } else {
            lock1 = locks.computeIfAbsent(accountTo, (a) -> a);
            lock2 = locks.computeIfAbsent(accountFrom, (a) -> a);
        }

        synchronized (lock1) {
            synchronized (lock2) {
                BigDecimal from = balance.getOrDefault(accountFrom, ZERO);
                if (from.compareTo(amount) < 0) {
                    //not enough
                    return false;
                } else {
                    balance.put(accountFrom, from.subtract(amount));
                    balance.merge(accountTo, amount, BigDecimal::add);

                    return true;
                }
            }
        }
    }

    public boolean processWithdraw(Long account, BigDecimal amount) {
        if (amount.compareTo(ZERO) <= 0) {
            return false;
        }

        Object lock = locks.computeIfAbsent(account, (a) -> a);
        synchronized (lock) {
            BigDecimal from = balance.getOrDefault(account, ZERO);
            if (from.compareTo(amount) < 0) {
                return false;
            } else {
                balance.put(account, from.subtract(amount));

                return true;
            }
        }
    }

    public boolean processIncome(Long account, BigDecimal amount) {
        if (amount.compareTo(ZERO) <= 0) {
            return false;
        }

        Object lock = locks.computeIfAbsent(account, (a) -> a);
        synchronized (lock) {
            BigDecimal from = balance.merge(account, amount, BigDecimal::add);
        }

        return true;
    }

    public BigDecimal getBalance(Long account) {
        return balance.getOrDefault(account, ZERO);
    }
}
