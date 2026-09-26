package tools.subathon.timer.ui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;
import tools.subathon.timer.proto.configuration.v1.ConfigurationServiceGrpc;
import tools.subathon.timer.proto.timer.v1.TimerServiceGrpc;

@Configuration
@ImportGrpcClients(target = "dataservice", types = {
        ConfigurationServiceGrpc.ConfigurationServiceBlockingStub.class,
        TimerServiceGrpc.TimerServiceBlockingStub.class
})
public class GrpcConfig {
}
