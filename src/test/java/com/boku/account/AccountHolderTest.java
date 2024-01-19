package com.boku.account;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountHolderTest {

    AccountHolder accountHolder;

    @BeforeEach
    public void init(){
        accountHolder = new AccountHolder();
        accountHolder.processIncome(1L, BigDecimal.valueOf(100));
    }

    @Test
    public void balance() {
        assertEquals(BigDecimal.valueOf(100), accountHolder.getBalance(1L));
    }

    @Test
    public void withdraw() {
        assertTrue(accountHolder.processWithdraw(1L, BigDecimal.ONE));
        assertEquals(BigDecimal.valueOf(99), accountHolder.getBalance(1L));
    }

    @Test
    public void withdrawMoreThenBalance() {
        assertFalse(accountHolder.processWithdraw(1L, BigDecimal.valueOf(101)));
    }

    @Test
    public void withdrawNegative() {
        assertFalse(accountHolder.processWithdraw(1L, BigDecimal.valueOf(-1)));
    }

    @Test
    public void withdrawFromNowhere() {
        assertFalse(accountHolder.processWithdraw(2L, BigDecimal.valueOf(1)));
    }

    @Test
    public void transfer() {
        assertTrue(accountHolder.processTransfer(1L, 2L, BigDecimal.ONE));
        assertEquals(BigDecimal.valueOf(99), accountHolder.getBalance(1L));
        assertEquals(BigDecimal.valueOf(1), accountHolder.getBalance(2L));
    }

    @Test
    public void transferMoreThenBalance() {
        assertFalse(accountHolder.processTransfer(1L, 2L, BigDecimal.valueOf(101)));
        assertEquals(BigDecimal.valueOf(100), accountHolder.getBalance(1L));
        assertEquals(BigDecimal.valueOf(0), accountHolder.getBalance(2L));
    }

    @Test
    public void transferNegative() {
        assertFalse(accountHolder.processTransfer(1L, 2L, BigDecimal.valueOf(-1)));
        assertEquals(BigDecimal.valueOf(100), accountHolder.getBalance(1L));
        assertEquals(BigDecimal.valueOf(0), accountHolder.getBalance(2L));
    }

    @Test
    public void transferFromNowhere() {
        assertFalse(accountHolder.processWithdraw(2L, BigDecimal.valueOf(1)));
        assertEquals(BigDecimal.valueOf(0), accountHolder.getBalance(2L));
    }

}