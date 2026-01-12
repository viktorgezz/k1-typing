package testconfig;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

import ru.viktorgezz.k1_typing_backend.K1TypingBackendApplication;

@Testcontainers
@SpringBootTest(
        classes = K1TypingBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class AbstractIntegrationRedisTest extends AbstractIntegrationPostgresTest{

    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    static {
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registryProperty) {
        registryProperty.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registryProperty.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registryProperty.add("spring.data.redis.password", () -> "");
    }
}
