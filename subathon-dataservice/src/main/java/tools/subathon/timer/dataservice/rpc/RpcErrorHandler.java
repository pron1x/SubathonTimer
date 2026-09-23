package tools.subathon.timer.dataservice.rpc;

import io.grpc.Status;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;

@Configuration
public class RpcErrorHandler {

    @Bean
    GrpcExceptionHandler grpcExceptionHandler() {
        return ex -> {
            if (ex instanceof EntityNotFoundException) {
                return Status.NOT_FOUND.asException();
            }
            return Status.INTERNAL.withDescription("Internal error.").asException();
        };
    }
}
