package com.boku.withdrawal;

import com.boku.RequestContext;
import com.boku.account.Address;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.*;

import static com.boku.withdrawal.WithdrawalService.WithdrawalState.*;

public class WithdrawalServiceImpl implements WithdrawalService {
    private final ConcurrentMap<WithdrawalId, Withdrawal> requests = new ConcurrentHashMap<>();// DB
    private final PriorityBlockingQueue<Withdrawal> processingRequests = new PriorityBlockingQueue<>(256,
            Comparator.comparingLong(Withdrawal::finaliseAt));
    private final ExecutorService withdrawalEngine;
    private WithdrawalProcessor withdrawalProcessor;

    public WithdrawalServiceImpl(WithdrawalProcessor withdrawalProcessor) {
        withdrawalEngine = Executors.newSingleThreadExecutor();

        Thread dispatcher = new Thread(new Dispatcher());
        dispatcher.start();

        this.withdrawalProcessor = withdrawalProcessor;
    }

    @Override
    public void requestWithdrawal(WithdrawalId id, Address address, BigDecimal amount) { // Please substitute T with prefered type
        Long currentAccount = RequestContext.get().getCurrentAccount();
        Withdrawal withdrawal = new Withdrawal(PROCESSING, currentAccount, finaliseAt(), address, amount, id);
        final var existing = requests.putIfAbsent(id, withdrawal);
        if (existing != null && (!Objects.equals(existing.externalAddress(), address) || !Objects.equals(existing.amount(), amount))) {
            throw new IllegalStateException("Withdrawal request with id[%s] is already present".formatted(id));
        } else {
            processingRequests.put(withdrawal);
        }
    }

    private long finaliseAt() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000, 10000);
    }

    @Override
    public WithdrawalState getRequestState(WithdrawalId id) {
        final var request = requests.get(id);
        if (request == null)
            throw new IllegalArgumentException("Request %s is not found".formatted(id));
        return request.state();
    }

    private class Dispatcher implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Withdrawal taken = processingRequests.take();

                    if (System.currentTimeMillis() < taken.finaliseAt()) {
                        processingRequests.put(taken); //back to queue (no blocking peek)
                        Thread.sleep(1L); //avoid spin loop
                    } else {
                        withdrawalEngine.submit(() -> {
                            WithdrawalState result = withdrawalProcessor.process(taken);

                            requests.put(taken.id(), taken.moveTo(result));
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
