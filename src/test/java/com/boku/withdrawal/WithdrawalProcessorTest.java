package com.boku.withdrawal;

import com.boku.account.AccountHolder;
import com.boku.account.Address;
import com.boku.transfer.Transfer;
import com.boku.transfer.TransferProcessor;
import com.boku.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class WithdrawalProcessorTest {

    @Test
    public void test() {
        AccountHolder accountHolder = Mockito.mock(AccountHolder.class);
        when(accountHolder.processWithdraw(anyLong(), any())).thenReturn(true);

        WithdrawalProcessor withdrawalProcessor = new WithdrawalProcessor(accountHolder);

        WithdrawalService.WithdrawalState result = withdrawalProcessor.process(new Withdrawal(WithdrawalService.WithdrawalState.PROCESSING, 1L, 1L, new Address(""), BigDecimal.ONE, null));

        assertEquals(WithdrawalService.WithdrawalState.COMPLETED, result);
    }

    @Test
    public void testFail() {
        AccountHolder accountHolder = Mockito.mock(AccountHolder.class);
        when(accountHolder.processWithdraw(anyLong(), any())).thenReturn(false);

        WithdrawalProcessor withdrawalProcessor = new WithdrawalProcessor(accountHolder);

        WithdrawalService.WithdrawalState result = withdrawalProcessor.process(new Withdrawal(WithdrawalService.WithdrawalState.PROCESSING, 1L, 1L, new Address(""), BigDecimal.ONE, null));

        assertEquals(WithdrawalService.WithdrawalState.FAILED, result);
    }

}