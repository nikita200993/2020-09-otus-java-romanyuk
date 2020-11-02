package ru.otus.gc;

import com.sun.management.GarbageCollectionNotificationInfo;
import ru.otus.utils.Contracts;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class AppWithMemoryLeak {

    private final int tempObjectsToAllocate;
    private final int longLiveObjectsToAllocate;
    private final Queue<Object> objects = new ArrayDeque<>();

    AppWithMemoryLeak(
            final int tempObjectsToAllocate,
            final int longLiveObjectsToAllocate) {
        Contracts.requireThat(tempObjectsToAllocate > 0);
        Contracts.requireThat(longLiveObjectsToAllocate > 0);
        this.tempObjectsToAllocate = tempObjectsToAllocate;
        this.longLiveObjectsToAllocate = longLiveObjectsToAllocate;
    }

    public static void main(final String[] args) {
        addListenersOnGCEvents(new GCEventsLogger(), null);
        new AppWithMemoryLeak(50_000, 1000).run();
    }

    public void run() {
        try {
            while (true) {
                for (int i = 0; i < tempObjectsToAllocate + longLiveObjectsToAllocate; i++) {
                    objects.offer(new String(new char[0]));
                }
                for (int i = 0; i < tempObjectsToAllocate; i++) {
                    objects.poll();
                }
                Thread.sleep(10);
            }
        } catch (final InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void addListenersOnGCEvents(final NotificationListener listener, final Object handback) {
        final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        final NotificationFilter filter = notification -> notification.getType().equals(
                GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION);
        for (final var gcBean : gcBeans) {
            System.out.println("GC bean name: " + gcBean.getName());
            final var emitter = (NotificationEmitter) gcBean;
            emitter.addNotificationListener(listener, filter, handback);
        }
    }

    private static class GCEventsLogger implements NotificationListener {

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            Contracts.requireNonNullArgument(notification);

            final GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from(
                    (CompositeData) notification.getUserData());
            final String message = String.format(
                    "event id: %s start time: %sms, name: %s, action: %s, cause: %s, duration (%sms)",
                    info.getGcInfo().getId(),
                    info.getGcInfo().getStartTime(),
                    info.getGcName(),
                    info.getGcAction(),
                    info.getGcCause(),
                    info.getGcInfo().getDuration());
            System.out.println(message);
        }
    }
}
