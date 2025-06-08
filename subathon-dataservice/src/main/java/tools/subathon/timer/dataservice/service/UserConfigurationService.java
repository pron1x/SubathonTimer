package tools.subathon.timer.dataservice.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;
import tools.subathon.timer.dataservice.data.repository.UserConfigurationRepository;

@Service
public class UserConfigurationService {

    private final ModelMapper modelMapper;

    private final UserConfigurationRepository userConfigurationRepository;

    public UserConfigurationService(ModelMapper modelMapper, UserConfigurationRepository userConfigurationRepository) {
        this.modelMapper = modelMapper;
        this.userConfigurationRepository = userConfigurationRepository;
    }

    public UserConfigurationModel save(UserConfigurationModel userConfigurationModel) {
        return modelMapper.map(userConfigurationRepository.save(modelMapper.map(userConfigurationModel, UserConfigurationEntity.class)), UserConfigurationModel.class);
    }

    public UserConfigurationModel getForChannel(String channelId) {
        UserConfigurationEntity config = userConfigurationRepository.findByChannelId(channelId);
        return config != null ? modelMapper.map(config, UserConfigurationModel.class) : null;
    }

    public UserConfigurationModel deleteForChannel(String channelId) {
        return modelMapper.map(userConfigurationRepository.deleteByChannelId(channelId), UserConfigurationModel.class);
    }

}
