package ru.otus.grpc;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GrpcServer {

    public static void main(final String[] args) throws IOException, InterruptedException {
        ServerBuilder.forPort(8090)
                .addService(new IntStreamingService())
                .build()
                .start()
                .awaitTermination();
    }

    private static class IntStreamingService extends IntStreamingServiceGrpc.IntStreamingServiceImplBase {

        @Override
        public void streamInts(final IntRange request, final StreamObserver<Int> responseObserver) {
            if (request.getStart() > request.getEnd()) {
                responseObserver.onCompleted();
            }
            final var executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(
                    new StreamingTask(
                            anInt -> responseObserver.onNext(primitiveIntToIntMessage(anInt)),
                            () -> {
                                responseObserver.onCompleted();
                                executor.shutdown();
                            },
                            request.getEnd(),
                            request.getStart()
                    ),
                    0,
                    2,
                    TimeUnit.SECONDS
            );
        }

        private static Int primitiveIntToIntMessage(final int anInt) {
            return Int.newBuilder()
                    .setAnInt(anInt)
                    .build();
        }
    }

    private static class StreamingTask implements Runnable {
        private final Consumer<Integer> consumer;
        private final Runnable onFinish;
        private final int lastValue;
        private int currentValue;

        private StreamingTask(
                final Consumer<Integer> consumer,
                final Runnable runnable,
                int lastValue,
                int currentValue) {
            this.consumer = consumer;
            this.onFinish = runnable;
            this.lastValue = lastValue;
            this.currentValue = currentValue;
        }

        @Override
        public void run() {
            if (currentValue > lastValue) {
                return;
            }
            consumer.accept(currentValue);
            currentValue++;
            if (currentValue > lastValue) {
                onFinish.run();
            }
        }
    }
}
