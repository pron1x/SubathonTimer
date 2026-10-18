package tools.subathon.timer.dataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tools.subathon.timer.dataservice.properties.TimerSecondsProperties;

@EnableConfigurationProperties(TimerSecondsProperties.class)
@SpringBootApplication
public class SubathonDataserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubathonDataserviceApplication.class, args);
    }
}
