package ru.otus.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.utils.Contracts;

public class GrpcClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcClient.class);

    private final ManagedChannel channel;

    public GrpcClient(final ManagedChannel channel) {
        this.channel = Contracts.ensureNonNullArgument(channel);
    }

    public static void main(final String[] args) throws InterruptedException {
        final var channel = ManagedChannelBuilder.forAddress("localhost", 8090)
                .usePlaintext()
                .build();
        try (GrpcClient client = new GrpcClient(channel)) {
            client.makeCall();
        }
    }

    public void makeCall() throws InterruptedException {
        final var observer = new IntObserver();
        final var stub = IntStreamingServiceGrpc.newStub(channel);
        int previousStreamedValue = observer.getLastStreamedValue();
        int currentValue = 0;
        stub.streamInts(createRange(1, 30), observer);
        for (int i = 0; i < 50; i++) {
            Thread.sleep(1000);
            synchronized (observer) {
                final int lastValue = observer.getLastStreamedValue();
                if (lastValue != previousStreamedValue) {
                    currentValue = currentValue + 1 + lastValue;
                    previousStreamedValue = lastValue;
                } else {
                    currentValue = currentValue + 1;
                }
                LOGGER.info("Current value = {}", currentValue);
            }
        }
    }

    private static IntRange createRange(final int start, final int end) {
        Contracts.requireThat(start <= end);
        return IntRange.newBuilder()
                .setStart(start)
                .setEnd(end)
                .build();
    }

    @Override
    public void close() {
        channel.shutdown();
    }

    private static class IntObserver implements StreamObserver<Int> {

        private static final Logger LOGGER = LoggerFactory.getLogger(IntObserver.class);

        private int lastStreamedValue = -1;

        @Override
        public synchronized void onNext(final Int value) {
            LOGGER.info("Got {} from server", value.getAnInt());
            lastStreamedValue = value.getAnInt();
        }

        @Override
        public void onError(final Throwable t) {
            LOGGER.error("Error reading stream.", t);
        }

        @Override
        public void onCompleted() {
            LOGGER.info("Request completed");
        }

        int getLastStreamedValue() {
            return lastStreamedValue;
        }
    }
}
