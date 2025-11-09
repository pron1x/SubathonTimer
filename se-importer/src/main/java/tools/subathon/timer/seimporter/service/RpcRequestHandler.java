package tools.subathon.timer.seimporter.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.streamelements.AuthenticateStreamelementsPayload;
import tools.subathon.rpc.payload.streamelements.StreamelementsPayload;
import tools.subathon.timer.seimporter.SocketService;
import tools.subathon.timer.util.interfaces.HasLogger;

import static tools.subathon.timer.util.GlobalRabbitMQ.IMPORTER_MANAGEMENT_QUEUE;

@Component
public class RpcRequestHandler implements HasLogger {
    private final SocketService socketService;

    public RpcRequestHandler(SocketService socketService) {
        this.socketService = socketService;
    }

    @RabbitListener(queues = IMPORTER_MANAGEMENT_QUEUE)
    public RpcResponse<Boolean> handleAuthRequest(RpcRequest<StreamelementsPayload> request) {
        getLogger().info("Handling auth request.");
        return switch (request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case AUTHENTICATE_SE -> {
                AuthenticateStreamelementsPayload payload = (AuthenticateStreamelementsPayload) request.getPayload();
                yield RpcResponse.ok(socketService.connectWithJwt(payload.jwt()));
            }
            default -> RpcResponse.error("Request command is not available for this queue.");
        };
    }
}
