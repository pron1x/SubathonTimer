package tools.subathon.timer.bot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;
import tools.subathon.timer.proto.timer.v1.TimerServiceGrpc;

@Configuration
@ImportGrpcClients(target = "dataservice", types = {
        TimerServiceGrpc.TimerServiceBlockingStub.class
})
public class GrpcConfig {
}
