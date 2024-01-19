package com.boku.transfer;

import com.boku.RequestContext;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.*;

import static com.boku.transfer.TransferService.TransferState.PROCESSING;

public class TransferServiceImpl implements TransferService {
    private final ConcurrentMap<TransferId, Transfer> requests = new ConcurrentHashMap<>();// DB
    private final PriorityBlockingQueue<Transfer> processingRequests = new PriorityBlockingQueue<>(256,
            Comparator.comparingLong(Transfer::finaliseAt));
    private final ExecutorService transferEngine;
    private TransferProcessor transferProcessor;

    public TransferServiceImpl(TransferProcessor transferProcessor) {
        transferEngine = Executors.newSingleThreadExecutor();

        Thread dispatcher = new Thread(new Dispatcher());
        dispatcher.start();

        this.transferProcessor = transferProcessor;
    }

    @Override
    public void requestTransfer(TransferId id, Long accountTo, BigDecimal amount) {
        Long currentAccount = RequestContext.get().getCurrentAccount();
        Transfer transfer = new Transfer(PROCESSING, currentAccount, accountTo, finaliseAt(), amount, id);
        final var existing = requests.putIfAbsent(id, transfer);

        if (existing != null && (!Objects.equals(existing.accountTo(), accountTo) || !Objects.equals(existing.amount(), amount))) {
            System.out.println("illegal");
            throw new IllegalStateException("Transfer request with id[%s] is already present".formatted(id));
        } else {
            System.out.println("transfer " + id.value());
            processingRequests.put(transfer);
        }
    }

    private long finaliseAt() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000, 10000);
    }

    @Override
    public TransferState getRequestState(TransferId id) {
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
                    Transfer taken = processingRequests.take();

                    if (System.currentTimeMillis() < taken.finaliseAt()) {
                        processingRequests.put(taken); //back to queue (no blocking peek)
                        Thread.sleep(1L); //avoid spin loop
                    } else {
                        System.out.println("submit");
                        transferEngine.submit(() -> {
                            TransferState result = transferProcessor.process(taken);

                            System.out.println("transfer " + taken.id().value() + " " + result);
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
