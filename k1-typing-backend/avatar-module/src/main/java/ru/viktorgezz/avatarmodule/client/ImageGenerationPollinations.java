package ru.viktorgezz.avatarmodule.client;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.viktorgezz.avatarmodule.dto.ImageDto;
import ru.viktorgezz.avatarmodule.exception.BusinessException;
import ru.viktorgezz.avatarmodule.exception.ErrorCode;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class ImageGenerationPollinations implements ImageGeneration {

    @Value("${custom.pollinations-tokens}")
    private List<String> tokens;

    private final RestClient restClient;
    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;

    public ImageGenerationPollinations(
            RestClient restClient,
            ProxyManager<String> proxyManager,
            @Qualifier("imageBucketConfig") BucketConfiguration bucketConfiguration
    ) {
        this.restClient = restClient;
        this.proxyManager = proxyManager;
        this.bucketConfiguration = bucketConfiguration;
    }

    public ImageDto generateImage(Long idUser, String prompt) {
        BucketProxy bucket = proxyManager
                .builder()
                .build("image_limit:" + idUser, () -> bucketConfiguration);

        if (!bucket.tryConsume(1)) {
            throw new BusinessException(ErrorCode.IMAGE_GENERATION_LIMIT_EXCEEDED, idUser.toString());
        }

        final String currentToken = getNextToken(idUser);
        final String promptWithoutSpaces = prompt.strip().toLowerCase(Locale.ROOT).replace(" ", "_");
        final String url = "https://gen.pollinations.ai/image/"
                + promptWithoutSpaces
                + "?model=flux-2-dev";

        ResponseEntity<byte[]> responseEntity = restClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + currentToken)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            log.error(
                                    "code: {}, text: {}, token: {}",
                                    response.getStatusCode(), response.getStatusText(), tokens
                            );
                            throw new BusinessException(
                                    ErrorCode.IMAGE_GENERATION_API_ERROR,
                                    response.getStatusText());
                        })
                .onStatus(HttpStatusCode::is2xxSuccessful,
                        (request, response) ->
                                log.debug("Image generation successful for user: {}", idUser)
                )
                .toEntity(byte[].class);

        String contentType = Optional.ofNullable(responseEntity.getHeaders().getContentType())
                .map(MediaType::toString)
                .orElse(MediaType.IMAGE_PNG_VALUE);

        return new ImageDto(responseEntity.getBody(), contentType);
    }

    /**
     * Выбирает случайно следующий API токен
     */
    private String getNextToken(final Long idUser) {
        if (tokens == null || tokens.isEmpty()) {
            log.error("Cannot get token for user {}: tokens list is empty or null", idUser);
            throw new BusinessException(ErrorCode.IMAGE_GENERATION_API_ERROR);
        }

        int indexRandom = ThreadLocalRandom.current().nextInt(tokens.size());
        String token = tokens.get(indexRandom);
        log.debug("Using token index: {} for user: {}, token: {}", indexRandom, idUser, token);
        return token;
    }
}
