package com.boku.transfer;

import com.boku.RequestContext;
import com.boku.account.Address;
import com.boku.withdrawal.WithdrawalProcessor;
import com.boku.withdrawal.WithdrawalService;
import com.boku.withdrawal.WithdrawalServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static com.boku.transfer.TransferService.TransferState.COMPLETED;
import static com.boku.transfer.TransferService.TransferState.PROCESSING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransferServiceImplTest {

    @Test
    public void ok() {
        TransferProcessor processor = Mockito.mock(TransferProcessor.class);
        when(processor.process(any())).thenReturn(COMPLETED);
        RequestContext.init(1L);

        TransferServiceImpl service = new TransferServiceImpl(processor);

        TransferService.TransferId id = new TransferService.TransferId(UUID.randomUUID());
        service.requestTransfer(id, 2L, BigDecimal.ONE);

        assertEquals(PROCESSING, service.getRequestState(id));
        assertThrows(IllegalArgumentException.class, () -> service.getRequestState(new TransferService.TransferId(UUID.randomUUID())));

        long start = System.currentTimeMillis();
        long until = start + 11000;
        TransferService.TransferState requestState = null;

        while (System.currentTimeMillis() < until) {
            requestState = service.getRequestState(id);
            if (requestState != PROCESSING) {
                break;
            }
        }

        assertTrue(System.currentTimeMillis() - start > 100L);
        Mockito.verify(processor, Mockito.times(1)).process(any());
        assertEquals(COMPLETED, requestState);
    }

    @Test
    public void idempotent() {
        TransferProcessor processor = Mockito.mock(TransferProcessor.class);
        when(processor.process(any())).thenReturn(COMPLETED);
        RequestContext.init(1L);

        TransferServiceImpl service = new TransferServiceImpl(processor);

        TransferService.TransferId id = new TransferService.TransferId(UUID.randomUUID());
        service.requestTransfer(id, 2L, BigDecimal.ONE);
        service.requestTransfer(id, 2L, BigDecimal.ONE);

        assertEquals(PROCESSING, service.getRequestState(id));
        assertThrows(IllegalArgumentException.class, () -> service.getRequestState(new TransferService.TransferId(UUID.randomUUID())));

        long start = System.currentTimeMillis();
        long until = start + 11000;
        TransferService.TransferState requestState = null;

        while (System.currentTimeMillis() < until) {
            requestState = service.getRequestState(id);
            if (requestState != PROCESSING) {
                break;
            }
        }

        assertTrue(System.currentTimeMillis() - start > 100L);
        Mockito.verify(processor, Mockito.times(1)).process(any());
        assertEquals(COMPLETED, requestState);
    }

    @Test
    public void sameIdButIllegal() {
        TransferProcessor processor = Mockito.mock(TransferProcessor.class);
        when(processor.process(any())).thenReturn(COMPLETED);
        RequestContext.init(1L);

        TransferServiceImpl service = new TransferServiceImpl(processor);

        TransferService.TransferId id = new TransferService.TransferId(UUID.randomUUID());
        service.requestTransfer(id, 2L, BigDecimal.ONE);
        assertThrows(IllegalStateException.class, () -> service.requestTransfer(id, 3L, BigDecimal.ONE));
        assertThrows(IllegalStateException.class, () -> service.requestTransfer(id, 2L, BigDecimal.TEN));
    }

}