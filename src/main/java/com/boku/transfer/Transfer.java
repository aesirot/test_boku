package com.boku.transfer;

import com.boku.account.Address;

import java.math.BigDecimal;

import static com.boku.transfer.TransferService.TransferState.COMPLETED;
import static com.boku.transfer.TransferService.TransferState.FAILED;

public record Transfer(TransferService.TransferState state, Long accountFrom, Long accountTo, long finaliseAt, BigDecimal amount,
                TransferService.TransferId id) {

    public Transfer fail() {
        return new Transfer(FAILED, accountFrom, accountTo, finaliseAt, amount, id);
    }

    public Transfer complete() {
        return new Transfer(COMPLETED, accountFrom, accountTo, finaliseAt, amount, id);
    }

    public Transfer moveTo(TransferService.TransferState state) {
        return new Transfer(state, accountFrom, accountTo, finaliseAt, amount, id);
    }
}
