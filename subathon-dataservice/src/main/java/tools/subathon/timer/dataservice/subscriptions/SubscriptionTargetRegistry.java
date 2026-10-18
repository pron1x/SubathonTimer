package tools.subathon.timer.dataservice.subscriptions;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;
import tools.subathon.timer.dataservice.data.repository.UserConfigurationRepository;
import tools.subathon.timer.proto.subscription.v1.DonationMessageParser;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTarget;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTargetChange;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTargetSnapshot;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTargetUpdate;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SubscriptionTargetRegistry implements HasLogger {

    private final Object lock = new Object();
    private final Map<UUID, ServerCallStreamObserver<SubscriptionTargetUpdate>> clients = new HashMap<>();
    private final UserConfigurationRepository userConfigurationRepository;

    public SubscriptionTargetRegistry(UserConfigurationRepository userConfigurationRepository) {
        this.userConfigurationRepository = userConfigurationRepository;
    }

    public void addClient(StreamObserver<SubscriptionTargetUpdate> responseObserver) {
        UUID clientId = UUID.randomUUID();
        ServerCallStreamObserver<SubscriptionTargetUpdate> client = (ServerCallStreamObserver<SubscriptionTargetUpdate>) responseObserver;
        synchronized (lock) {
            clients.put(clientId, client);
            client.setOnCancelHandler(() -> removeClient(clientId));
            client.onNext(snapshotUpdate(userConfigurationRepository.findAll()));
        }
    }

    public void publishUpdate(UserConfigurationEntity userConfigurationEntity) {
        SubscriptionTargetChange update = SubscriptionTargetChange.newBuilder()
                .setTarget(configEntityToSubscriptionTarget(userConfigurationEntity))
                .build();
        synchronized (lock) {
            clients.entrySet().removeIf(entry -> {
                try {
                    entry.getValue().onNext(SubscriptionTargetUpdate.newBuilder().setChange(update).build());
                    return false;
                } catch (Exception e) {
                    getLogger().error("Failed to send update to client {}", entry.getKey(), e);
                    return true;
                }
            });
        }
    }

    private void removeClient(UUID clientId) {
        synchronized (lock) {
            clients.remove(clientId);
            getLogger().debug("Removed client {}", clientId);
        }
    }

    private SubscriptionTargetUpdate snapshotUpdate(Iterable<UserConfigurationEntity> targets) {
        SubscriptionTargetSnapshot.Builder snapshotBuilder = SubscriptionTargetSnapshot.newBuilder();
        targets.forEach(target -> snapshotBuilder.addTargets(
                configEntityToSubscriptionTarget(target)
        ));

        return SubscriptionTargetUpdate.newBuilder().setSnapshot(snapshotBuilder).build();
    }

    private SubscriptionTarget configEntityToSubscriptionTarget(UserConfigurationEntity entity) {
        DonationMessageParser.Builder parserBuilder = DonationMessageParser.newBuilder();
        if (entity.getDonationTemplateUser() != null) {
            parserBuilder.setUser(entity.getDonationTemplateUser());
        }
        if (entity.getDonationTemplatePattern() != null) {
            parserBuilder.setPattern(entity.getDonationTemplatePattern());
        }

        return SubscriptionTarget.newBuilder()
                .setBroadcasterId(entity.getChannelId())
                .setDonationMessageParser(parserBuilder)
                .build();
    }
}
