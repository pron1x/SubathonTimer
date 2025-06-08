package tools.subathon.timer.dataservice.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tools.subathon.timer.dataservice.data.entity.UserConfigurationEntity;

@Repository
public interface UserConfigurationRepository extends CrudRepository<UserConfigurationEntity, Long> {

    public UserConfigurationEntity findByChannelId(String channelId);

    public UserConfigurationEntity deleteByChannelId(String channelId);
}
