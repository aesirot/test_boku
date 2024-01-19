package com.boku.transfer;

import com.boku.account.AccountHolder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class TransferProcessorTest {

    @Test
    public void test() {
        AccountHolder accountHolder = Mockito.mock(AccountHolder.class);
        when(accountHolder.processTransfer(anyLong(), anyLong(), any())).thenReturn(true);

        TransferProcessor transferProcessor = new TransferProcessor(accountHolder);

        TransferService.TransferState result = transferProcessor.process(new Transfer(TransferService.TransferState.PROCESSING, 1L, 2L, 1L, BigDecimal.ONE, null));

        assertEquals(TransferService.TransferState.COMPLETED, result);
    }

    @Test
    public void testFail() {
        AccountHolder accountHolder = Mockito.mock(AccountHolder.class);
        when(accountHolder.processTransfer(anyLong(), anyLong(), any())).thenReturn(false);

        TransferProcessor transferProcessor = new TransferProcessor(accountHolder);

        TransferService.TransferState result = transferProcessor.process(new Transfer(TransferService.TransferState.PROCESSING, 1L, 2L, 1L, BigDecimal.ONE, null));

        assertEquals(TransferService.TransferState.FAILED, result);
    }

}