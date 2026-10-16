package tools.subathon.timer.bot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;
import tools.subathon.timer.proto.subscription.v1.EventSubscriptionServiceGrpc;
import tools.subathon.timer.proto.timer.v1.TimerServiceGrpc;

@Configuration
@ImportGrpcClients(target = "dataservice", types = {
        TimerServiceGrpc.TimerServiceBlockingStub.class
})
@ImportGrpcClients(target = "dataservice-stream", types = {
        EventSubscriptionServiceGrpc.EventSubscriptionServiceStub.class
})
public class GrpcConfig {
}
