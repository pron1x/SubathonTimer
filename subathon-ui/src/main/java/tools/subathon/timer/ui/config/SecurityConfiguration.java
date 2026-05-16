package tools.subathon.timer.ui.config;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    protected SecurityFilterChain configureFilterChain(HttpSecurity http) {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.oauth2LoginPage("/oauth2/authorization/twitch");
        });

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            auth.requestMatchers(PathPatternRequestMatcher.pathPattern("/api/**")).permitAll();
        });

        return http.build();
    }
}
