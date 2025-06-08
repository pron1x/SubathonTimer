package tools.subathon.timer.seimporter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.seimporter.SocketService;
import tools.subathon.timer.util.interfaces.HasLogger;

import static tools.subathon.timer.util.GlobalRabbitMQ.BOT_RPC_QUEUE_NAME;

@Component
@RabbitListener(queues = BOT_RPC_QUEUE_NAME)
public class RpcRequestHandler implements HasLogger {
    private final ObjectMapper mapper;
    private final SocketService socketService;

    public RpcRequestHandler(ObjectMapper mapper, SocketService socketService) {
        this.mapper = mapper;
        this.socketService = socketService;
    }

    @RabbitHandler
    public RpcResponseEntity<Void> handleJwtTokenRequest(RpcRequestEntity<String> request) {
        getLogger().info("Handling jwt token request.");
        if(request == null) {
            return RpcResponseEntity.error("Request is null");
        }
        switch (request.getAction()) {
            case GET, DELETE -> {
                return RpcResponseEntity.of(); // Do nothing for now
            }
            case CREATE_OR_UPDATE -> {
                String jwt = request.getBody();
                if(StringUtils.isBlank(jwt)) {
                    return RpcResponseEntity.error("JWT is empty!");
                }
                socketService.connectWithJwt(jwt);
                return RpcResponseEntity.of();
            }
            default -> {
                return RpcResponseEntity.error("Unknown request action");
            }
        }
    }
}
