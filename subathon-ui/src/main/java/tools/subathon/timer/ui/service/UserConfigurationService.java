package tools.subathon.timer.ui.service;

import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.proto.configuration.v1.ConfigurationServiceGrpc;
import tools.subathon.timer.proto.configuration.v1.GetConfigurationRequest;
import tools.subathon.timer.proto.configuration.v1.UserConfiguration;
import tools.subathon.timer.ui.mapper.UserConfigMapper;

@Service
public class UserConfigurationService {

    private final UserConfigMapper mapper;
    private final ConfigurationServiceGrpc.ConfigurationServiceBlockingStub configRpcService;

    public UserConfigurationService(UserConfigMapper mapper, ConfigurationServiceGrpc.ConfigurationServiceBlockingStub configRpcService) {
        this.mapper = mapper;
        this.configRpcService = configRpcService;
    }

    public UserConfigurationDto getUserConfiguration(String channelId) {
        UserConfiguration configProto = configRpcService.getConfiguration(GetConfigurationRequest.newBuilder().setChannelId(channelId).build());
        return mapper.protoToDto(configProto);
    }

    public UserConfigurationDto saveUserConfiguration(UserConfigurationDto configDto) {
        UserConfiguration configProto = mapper.dtoToProto(configDto);
        return mapper.protoToDto(configRpcService.putConfiguration(configProto));
    }
}
