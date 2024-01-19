package com.boku.withdrawal;

import com.boku.RequestContext;
import com.boku.account.Address;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WithdrawalServiceImplTest {

    @Test
    public void ok() {
        WithdrawalProcessor mock = Mockito.mock(WithdrawalProcessor.class);
        when(mock.process(any())).thenReturn(WithdrawalService.WithdrawalState.COMPLETED);
        RequestContext.init(1L);

        WithdrawalServiceImpl withdrawalService = new WithdrawalServiceImpl(mock);

        WithdrawalService.WithdrawalId id = new WithdrawalService.WithdrawalId(UUID.randomUUID());
        withdrawalService.requestWithdrawal(id, new Address(""), BigDecimal.ONE);

        assertEquals(WithdrawalService.WithdrawalState.PROCESSING, withdrawalService.getRequestState(id));

        long start = System.currentTimeMillis();
        long until = start + 11000;
        WithdrawalService.WithdrawalState requestState = null;

        while (System.currentTimeMillis() < until) {
            requestState = withdrawalService.getRequestState(id);
            if (requestState != WithdrawalService.WithdrawalState.PROCESSING) {
                break;
            }
        }

        assertTrue(System.currentTimeMillis() - start > 100L);
        Mockito.verify(mock, Mockito.times(1)).process(any());
        assertEquals(WithdrawalService.WithdrawalState.COMPLETED, requestState);
    }

    @Test
    public void idempotent() {
        WithdrawalProcessor mock = Mockito.mock(WithdrawalProcessor.class);
        when(mock.process(any())).thenReturn(WithdrawalService.WithdrawalState.COMPLETED);
        RequestContext.init(1L);

        WithdrawalServiceImpl withdrawalService = new WithdrawalServiceImpl(mock);

        WithdrawalService.WithdrawalId id = new WithdrawalService.WithdrawalId(UUID.randomUUID());
        withdrawalService.requestWithdrawal(id, new Address(""), BigDecimal.ONE);
        withdrawalService.requestWithdrawal(id, new Address(""), BigDecimal.ONE);

        assertEquals(WithdrawalService.WithdrawalState.PROCESSING, withdrawalService.getRequestState(id));

        long until = System.currentTimeMillis() + 11000;
        WithdrawalService.WithdrawalState requestState = null;

        while (System.currentTimeMillis() < until) {
            requestState = withdrawalService.getRequestState(id);
            if (requestState != WithdrawalService.WithdrawalState.PROCESSING) {
                break;
            }
        }

        Mockito.verify(mock, Mockito.times(1)).process(any());
        assertEquals(WithdrawalService.WithdrawalState.COMPLETED, requestState);
    }

    @Test
    public void sameIdButIllegal() {
        WithdrawalProcessor mock = Mockito.mock(WithdrawalProcessor.class);
        when(mock.process(any())).thenReturn(WithdrawalService.WithdrawalState.COMPLETED);
        RequestContext.init(1L);

        WithdrawalServiceImpl withdrawalService = new WithdrawalServiceImpl(mock);

        WithdrawalService.WithdrawalId id = new WithdrawalService.WithdrawalId(UUID.randomUUID());
        withdrawalService.requestWithdrawal(id, new Address(""), BigDecimal.ONE);
        assertThrows(IllegalStateException.class, () -> withdrawalService.requestWithdrawal(id, new Address("1"), BigDecimal.ONE));
        assertThrows(IllegalStateException.class, () -> withdrawalService.requestWithdrawal(id, new Address(""), BigDecimal.valueOf(2)));
    }

}