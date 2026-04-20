package ru.viktorgezz.security.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "custom")
@Getter
@Setter
public class CustomProperties {

    private String hostFrontend;

    // custom.port-frontend -> portFrontend
    private Integer portFrontend;

    @PostConstruct
    public void init() {
        log.debug("url frontend: {}, port frontend {}", hostFrontend, portFrontend);
    }
}
