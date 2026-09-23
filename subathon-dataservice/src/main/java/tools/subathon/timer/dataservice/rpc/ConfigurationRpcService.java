package tools.subathon.timer.dataservice.rpc;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.grpc.server.service.GrpcService;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.mapper.UserConfigMapper;
import tools.subathon.timer.dataservice.service.UserConfigurationService;
import tools.subathon.timer.proto.configuration.v1.ConfigurationServiceGrpc;
import tools.subathon.timer.proto.configuration.v1.GetConfigurationRequest;
import tools.subathon.timer.proto.configuration.v1.UserConfiguration;

import java.util.Optional;

@GrpcService
public class ConfigurationRpcService extends ConfigurationServiceGrpc.ConfigurationServiceImplBase {

    private final UserConfigurationService configurationService;
    private final UserConfigMapper configMapper;

    public ConfigurationRpcService(UserConfigurationService configurationService, UserConfigMapper configMapper) {
        this.configurationService = configurationService;
        this.configMapper = configMapper;
    }

    @Override
    public void getConfiguration(GetConfigurationRequest request, StreamObserver<UserConfiguration> responseObserver) {
        Optional<UserConfiguration> config = configurationService.getForChannel(request.getChannelId()).map(configMapper::dtoToProto);
        config.ifPresentOrElse(responseObserver::onNext,
                () -> { throw new EntityNotFoundException(); });
        responseObserver.onCompleted();
    }

    @Override
    public void putConfiguration(UserConfiguration request, StreamObserver<UserConfiguration> responseObserver) {
        UserConfigurationDto dto = configMapper.protoToDto(request);
        if (dto != null) {
            dto = configurationService.save(dto);
            responseObserver.onNext(configMapper.dtoToProto(dto));
        } else {
            responseObserver.onError(new IllegalArgumentException("UserConfiguration is required"));
        }
        responseObserver.onCompleted();
    }
}
