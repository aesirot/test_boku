package com.boku;

import com.boku.account.AccountHolder;
import com.boku.account.Address;
import com.boku.transfer.TransferProcessor;
import com.boku.transfer.TransferService;
import com.boku.transfer.TransferServiceImpl;
import com.boku.withdrawal.WithdrawalProcessor;
import com.boku.withdrawal.WithdrawalService;
import com.boku.withdrawal.WithdrawalServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class AppController {

    public static final String ACCOUNT = "ACCOUNT";
    //in real app use dependency injection - Spring, Guice
    AccountHolder accountHolder = new AccountHolder();
    WithdrawalService withdrawalService = new WithdrawalServiceImpl(new WithdrawalProcessor(accountHolder));
    TransferService transferService = new TransferServiceImpl(new TransferProcessor(accountHolder));

    public AppController() {
        //playground
        accountHolder.processIncome(1L, BigDecimal.valueOf(100));
        accountHolder.processIncome(2L, BigDecimal.valueOf(10));
    }

    @PostMapping("/withdraw")
    void withdraw(HttpServletRequest request, @RequestBody Withdraw withdraw) {
        initContext(request);

        withdrawalService.requestWithdrawal(new WithdrawalService.WithdrawalId(withdraw.id), new Address(withdraw.address), withdraw.amount);
    }

    @GetMapping("/withdraw/{id}")
    WithdrawalService.WithdrawalState withdrawalState(HttpServletRequest request, @PathVariable("id") UUID uuid) {
        initContext(request);

        return withdrawalService.getRequestState(new WithdrawalService.WithdrawalId(uuid));
    }

    @PostMapping("/transfer")
    void transfer(HttpServletRequest request, @RequestBody Transfer transfer) {
        initContext(request);

        transferService.requestTransfer(new TransferService.TransferId(transfer.id), transfer.accountTo, transfer.amount);
    }

    @GetMapping("/transfer/{id}")
    TransferService.TransferState transfer(HttpServletRequest request, @PathVariable("id") UUID uuid) {
        initContext(request);

        return transferService.getRequestState(new TransferService.TransferId(uuid));
    }

    private static void initContext(HttpServletRequest request) {
        Long account = (Long) request.getSession().getAttribute(ACCOUNT);
        if (account == null) {
            throw new RuntimeException("No init");
        }
        RequestContext.init(account);
    }

    @GetMapping("/balance")
    BigDecimal balance(HttpServletRequest request) {
        Long account = (Long) request.getSession().getAttribute(ACCOUNT);

        return accountHolder.getBalance(account);
    }

    @PostMapping("/init")
    void withdraw(HttpServletRequest request, @RequestBody UserData userData) {
        request.getSession().setAttribute(ACCOUNT, userData.currentAccount);
    }

    public static class UserData {
        private Long currentAccount;

        public UserData() {
        }

        public void setCurrentAccount(Long currentAccount) {
            this.currentAccount = currentAccount;
        }

        public Long getCurrentAccount() {
            return currentAccount;
        }
    }

    public static class Withdraw {
        private UUID id;
        private String address;
        private BigDecimal amount;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class Transfer {
        private UUID id;
        private Long accountTo;
        private BigDecimal amount;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Long getAccountTo() {
            return accountTo;
        }

        public void setAccountTo(Long accountTo) {
            this.accountTo = accountTo;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

}
