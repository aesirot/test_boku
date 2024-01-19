package com.boku.transfer;

import com.boku.account.AccountHolder;

import static com.boku.transfer.TransferService.TransferState.COMPLETED;
import static com.boku.transfer.TransferService.TransferState.FAILED;

public class TransferProcessor {

    private final AccountHolder accountHolder;

    public TransferProcessor(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
    }

    public TransferService.TransferState process(Transfer transfer) {
        try {
            boolean r = accountHolder.processTransfer(transfer.accountFrom(), transfer.accountTo(), transfer.amount());

            return r ? COMPLETED : FAILED;
        } catch (Exception e) {
            e.printStackTrace();
            return FAILED;
        }
    }
}
