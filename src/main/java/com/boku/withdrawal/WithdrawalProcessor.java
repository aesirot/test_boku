package com.boku.withdrawal;

import com.boku.account.AccountHolder;

import static com.boku.withdrawal.WithdrawalService.WithdrawalState.COMPLETED;
import static com.boku.withdrawal.WithdrawalService.WithdrawalState.FAILED;

public class WithdrawalProcessor {

    private final AccountHolder accountHolder;

    public WithdrawalProcessor(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
    }

    public WithdrawalService.WithdrawalState process(Withdrawal withdrawal) {
        try {
            boolean r = accountHolder.processWithdraw(withdrawal.account(), withdrawal.amount());

            return r ? COMPLETED : FAILED;
        } catch (Exception e) {
            e.printStackTrace();
            return FAILED;
        }
    }
}
