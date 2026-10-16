package tools.subathon.timer.dataservice.subscriptions;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import tools.subathon.timer.proto.subscription.v1.EventSubscriptionServiceGrpc;
import tools.subathon.timer.proto.subscription.v1.SubscriptionTargetUpdate;

@GrpcService
public class SubscriptionRpcService extends EventSubscriptionServiceGrpc.EventSubscriptionServiceImplBase {

    private final SubscriptionTargetRegistry subscriptionTargetRegistry;

    public SubscriptionRpcService(SubscriptionTargetRegistry subscriptionTargetRegistry) {
        this.subscriptionTargetRegistry = subscriptionTargetRegistry;
    }

    @Override
    public void watchTargets(Empty request, StreamObserver<SubscriptionTargetUpdate> responseObserver) {
        subscriptionTargetRegistry.addClient(responseObserver);
    }


}
