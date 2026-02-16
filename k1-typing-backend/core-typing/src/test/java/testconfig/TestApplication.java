package testconfig;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "ru.viktorgezz.coretyping")
@EnableJpaRepositories(basePackages = "ru.viktorgezz.coretyping")
@EntityScan(basePackages = "ru.viktorgezz.coretyping")
@EnableScheduling
@EnableAsync
public class TestApplication {
}
