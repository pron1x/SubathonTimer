package tools.subathon.timer.dataservice.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;
import tools.subathon.timer.dataservice.data.repository.UserConfigurationRepository;

import java.util.Optional;

@Service
public class UserConfigurationService {

    private final ModelMapper modelMapper;

    private final UserConfigurationRepository userConfigurationRepository;

    public UserConfigurationService(ModelMapper modelMapper, UserConfigurationRepository userConfigurationRepository) {
        this.modelMapper = modelMapper;
        this.userConfigurationRepository = userConfigurationRepository;
    }

    public UserConfigurationDto save(UserConfigurationDto userConfigurationModel) {
        return modelMapper.map(userConfigurationRepository.save(modelMapper.map(userConfigurationModel, UserConfigurationEntity.class)), UserConfigurationDto.class);
    }

    public Optional<UserConfigurationDto> getForChannel(String channelId) {
        UserConfigurationEntity config = userConfigurationRepository.findByChannelId(channelId);
        return config != null ? Optional.of(modelMapper.map(config, UserConfigurationDto.class)) : Optional.empty();
    }

    public UserConfigurationDto deleteForChannel(String channelId) {
        return modelMapper.map(userConfigurationRepository.deleteByChannelId(channelId), UserConfigurationDto.class);
    }

}
