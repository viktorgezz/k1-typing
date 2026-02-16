package ru.viktorgezz.avatarmodule.client;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.viktorgezz.avatarmodule.dto.GeneratedImageDto;
import ru.viktorgezz.avatarmodule.exception.BusinessException;
import ru.viktorgezz.avatarmodule.exception.ErrorCode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ImageGeneration {

    private final RestClient restClient;
    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;

    @Value("${custom.pollinations-tokens}")
    private List<String> tokens;
    private final AtomicInteger indexToken = new AtomicInteger(0);

    public ImageGeneration(RedissonClient redissonClient) {
        this.restClient = RestClient.builder()
                .build();

        Redisson redisson = (Redisson) redissonClient;
        CommandAsyncExecutor commandExecutor = redisson.getCommandExecutor();
        this.proxyManager = RedissonBasedProxyManager.builderFor(commandExecutor)
                .build();

        this.bucketConfiguration = BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(1)
                        .refillIntervally(1, Duration.ofMinutes(1)))
                .build();
    }

    public GeneratedImageDto generateImage(Long idUser, String prompt) {
        BucketProxy bucket = proxyManager
                .builder()
                .build("image_limit:" + idUser, () -> bucketConfiguration);

        if (!bucket.tryConsume(1)) {
            throw new BusinessException(ErrorCode.IMAGE_GENERATION_LIMIT_EXCEEDED, idUser.toString());
        }

        String currentToken = getNextToken(idUser);
        String promptEncoded = encodeForPath(prompt);
        String url = "https://gen.pollinations.ai/image/"
                + promptEncoded
                + "?model=flux-2-dev";

        ResponseEntity<byte[]> responseEntity = restClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + currentToken)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            log.debug("tokens: {}", tokens);
                            log.error("code: {}. text: {}", response.getStatusCode(), response.getStatusText());
                            throw new BusinessException(
                                    ErrorCode.IMAGE_GENERATION_API_ERROR,
                                    response.getStatusText());
                        })
                .onStatus(HttpStatusCode::is2xxSuccessful,
                        (requst, response) ->
                                log.debug("requset succesfull")
                )
                .toEntity(byte[].class);

        String contentType = Optional.ofNullable(responseEntity.getHeaders().getContentType())
                .map(MediaType::toString)
                .orElse(MediaType.IMAGE_PNG_VALUE);

        return new GeneratedImageDto(responseEntity.getBody(), contentType);
    }

    /**
     * Выбирает следующий API токен по кругу
     */
    private String getNextToken(final Long idUser) {
        int index = Math.abs(indexToken.getAndIncrement() % tokens.size());
        String token = tokens.get(index);
        log.debug("Using token index: {} for user: {}, token: {}", index, idUser, token);
        return tokens.get(index);
    }

    private static String encodeForPath(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        value = value.trim().toLowerCase().replace(" ", "_");
        return URLEncoder
                .encode(value, StandardCharsets.UTF_8);
    }
}
