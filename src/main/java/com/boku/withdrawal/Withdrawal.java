package com.boku.withdrawal;

import com.boku.account.Address;

import java.math.BigDecimal;

import static com.boku.withdrawal.WithdrawalService.WithdrawalState.COMPLETED;
import static com.boku.withdrawal.WithdrawalService.WithdrawalState.FAILED;

record Withdrawal(WithdrawalService.WithdrawalState state, Long account, long finaliseAt, Address externalAddress, BigDecimal amount,
                  WithdrawalService.WithdrawalId id) {

    public Withdrawal fail() {
        return new Withdrawal(FAILED, account, finaliseAt, externalAddress, amount, id);
    }

    public Withdrawal complete() {
        return new Withdrawal(COMPLETED, account, finaliseAt, externalAddress, amount, id);
    }

    public Withdrawal moveTo(WithdrawalService.WithdrawalState state) {
        return new Withdrawal(state, account, finaliseAt, externalAddress, amount, id);
    }
}
