package tools.subathon.timer.bot.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import tools.subathon.timer.bot.SubathonBot;
import tools.subathon.timer.proto.subscription.v1.DonationMessageParser;
import tools.subathon.timer.proto.subscription.v1.EventSubscriptionServiceGrpc;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTarget;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTargetUpdate;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EventSubscriptionService implements HasLogger {

    private static final long MAX_RETRY_MILLIS = 30_000;

    private final SubathonBot bot;
    private final EventSubscriptionServiceGrpc.EventSubscriptionServiceStub stub;

    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService updateExecutor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong  generation = new AtomicLong(0);
    private final AtomicLong retryMillis = new AtomicLong(1000);
    private final AtomicReference<ClientCallStreamObserver<Empty>> activeCall = new AtomicReference<>();

    public EventSubscriptionService(EventSubscriptionServiceGrpc.EventSubscriptionServiceStub stub, SubathonBot bot) {
        this.stub = stub;
        this.bot = bot;
    }

    @PostConstruct
    public void start() {
        if (running.compareAndSet(false, true)) {
            reconnectExecutor.execute(this::connect);
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        getLogger().info("Stopping event subscription stream");
        ClientCallStreamObserver<Empty> call = activeCall.getAndSet(null);
        if (call != null) {
            call.cancel("Bot shutting down", null);
        }
        reconnectExecutor.shutdownNow();
        updateExecutor.shutdownNow();
    }

    private void connect() {
        if (!running.get()) {
            return;
        }

        long streamGeneration = generation.incrementAndGet();
        AtomicBoolean terminated = new AtomicBoolean(false);

        ClientResponseObserver<Empty, SubscriptionTargetUpdate> observer = new ClientResponseObserver<>() {
            @Override
            public void beforeStart(ClientCallStreamObserver<Empty> requestStream) {
                activeCall.set(requestStream);
                if (!running.get()) {
                    requestStream.cancel("Bot shutting down", null);
                }
            }

            @Override
            public void onNext(SubscriptionTargetUpdate value) {
                try {
                    updateExecutor.execute(() -> {
                        if (running.get() && generation.get() == streamGeneration) {
                            apply(value);
                        }
                    });
                } catch (RejectedExecutionException e) {
                    if (running.get()) {
                        getLogger().error("Could not queue subscription update", e);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!running.get()) {
                    if (Status.fromThrowable(t).getCode() != Status.Code.CANCELLED) {
                        getLogger().warn("Subscription stream ended with an unexpected error while shutting down", t);
                    }
                    return;
                }
                getLogger().warn("Subscription stream failed; reconnecting...", t);
                reconnect();
            }

            @Override
            public void onCompleted() {
                if (!running.get()) {
                    return;
                }
                getLogger().warn("Subscription stream completed unexpectedly; reconnecting...");
                reconnect();
            }

            private void reconnect() {
                if (terminated.compareAndSet(false, true) && running.get()) {
                    activeCall.set(null);
                    long delay = retryMillis.getAndUpdate(current -> Math.min(current * 2, MAX_RETRY_MILLIS));
                    reconnectExecutor.schedule(
                            EventSubscriptionService.this::connect, delay, TimeUnit.MILLISECONDS);
                }
            }
        };
        try {
            stub.watchTargets(Empty.getDefaultInstance(), observer);
        } catch (StatusRuntimeException e) {
            getLogger().error("Could not start subscription stream; reconnecting...", e);
            if (running.get()) {
                long delay = retryMillis.getAndUpdate(current -> Math.min(current * 2, MAX_RETRY_MILLIS));
                reconnectExecutor.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
            }
        }
    }


    private void apply(SubscriptionTargetUpdate update) {
        switch (update.getUpdateCase()) {
            case SNAPSHOT -> update.getSnapshot().getTargetsList().forEach(this::applyTarget);
            case CHANGE -> applyTarget(update.getChange().getTarget());
            case UPDATE_NOT_SET -> getLogger().warn("Received update with no update case set");
        }
    }

    private void applyTarget(SubscriptionTarget target) {
        try {
            bot.ensureSubscriptions(target.getBroadcasterId());

            if (target.hasDonationMessageParser()) {
                DonationMessageParser parser = target.getDonationMessageParser();
                if (parser.hasPattern() || parser.hasUser()) {
                    bot.subscribeToDonationMessages(target.getBroadcasterId(), parser.getPattern(), parser.getUser());
                } else {
                    bot.removeDonationMessageParser(target.getBroadcasterId());
                }
            }
        } catch (RuntimeException e) {
            getLogger().error("Failed to apply subscription target for broadcaster '{}.'", target.getBroadcasterId(), e);
        }
    }
}


