package com.epam.esm.service;

import com.epam.esm.model.SongMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Slf4j
public class CallerService {
    private static final String RESOURCE_ENDPOINT = "resources/%d";
    private static final String SONG_METADATA_ENDPOINT = "songs";
    @Value("${resource-service.endpointHost}")
    private String resourceServiceHost;
    @Value("${song-service.endpointHost}")
    private String songServiceHost;
    @Value("${caller.maxRetries}")
    private int maxRetries;
    private final RestTemplate restTemplate;

    public CallerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(value = {RuntimeException.class}, maxAttemptsExpression = "${caller.maxRetries}" , backoff = @Backoff(value = 500L))
    public Optional<ByteArrayResource> callResourceService(long id) {
        log.info(String.format("Call endpoint: %s", String.format(RESOURCE_ENDPOINT, id)));
        return Optional.of(restTemplate.getForObject(String.format(resourceServiceHost + RESOURCE_ENDPOINT, id), ByteArrayResource.class));
    }

    @Recover
    private Optional<ByteArrayResource> recoverResourceServiceCall(RuntimeException exception) {
        log.error(String.format("Can't connect to the resource service, cause: %s, retries: %d", exception.getMessage(), maxRetries));
        return Optional.empty();
    }

    @Retryable(value = {RuntimeException.class}, maxAttemptsExpression = "${caller.maxRetries}" , backoff = @Backoff(value = 500L))
    public void callSongService(SongMetadata metadata) {
        log.info(String.format("Call endpoint: %s", SONG_METADATA_ENDPOINT));
        restTemplate.postForLocation(songServiceHost + SONG_METADATA_ENDPOINT, metadata);
    }

    @Recover
    private void recoverSongServiceCall(RuntimeException exception) {
        log.error(String.format("Can't connect to the song service, cause: %s, retries: %d", exception.getMessage(), maxRetries));
    }
}
