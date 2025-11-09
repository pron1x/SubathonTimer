package tools.subathon.timer.ui.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;

@Service
public class UserConfigurationService {

    private final DataserviceRpcService dataserviceRpcService;

    @Autowired
    public UserConfigurationService(DataserviceRpcService dataserviceRpcService) {
        this.dataserviceRpcService = dataserviceRpcService;
    }

    public RpcResponse<UserConfigurationModel> getUserConfiguration(String channelId) {
        return dataserviceRpcService.getUserConfiguration(channelId);
    }

    public RpcResponse<UserConfigurationModel> saveUserConfiguration(String channelId, UserConfigurationModel config) {
        return dataserviceRpcService.saveUserConfiguration(channelId, config);
    }
}
