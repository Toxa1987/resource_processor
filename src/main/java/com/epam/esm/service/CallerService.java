package com.epam.esm.service;

import com.epam.esm.dto.SaveSongDto;
import com.epam.esm.dto.StorageDto;
import com.epam.esm.model.SongMetadata;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CallerService {
    private static final String RESOURCE_ENDPOINT_GET = "resources/%d";
    private static final String RESOURCE_ENDPOINT_POST = "resources";
    private static final String SONG_METADATA_ENDPOINT = "songs";
    private static final String STORAGE_ENDPOINT = "storages";

    @Value("${resource-service.endpointHost}")
    private String resourceServiceHost;
    @Value("${song-service.endpointHost}")
    private String songServiceHost;
    @Value("${storage-service.endpointHost}")
    private String storageServiceHost;
    @Value("${caller.maxRetries}")
    private int maxRetries;
    private final RestTemplate restTemplate;
    private final CircuitBreakerRegistry registry;
    private ResponseEntity<StorageDto[]> savedEntity = new ResponseEntity<StorageDto[]>(new StorageDto[]{}, HttpStatus.NO_CONTENT);

    public CallerService(RestTemplate restTemplate, CircuitBreakerRegistry registry) {
        this.restTemplate = restTemplate;
        this.registry = registry;
    }

    @Retryable(value = {RuntimeException.class}, maxAttemptsExpression = "${caller.maxRetries}", backoff = @Backoff(value = 500L))
    public Optional<ByteArrayResource> callResourceService(long id) {
        log.info(String.format("Call endpoint: %s", String.format(RESOURCE_ENDPOINT_GET, id)));
        return Optional.of(restTemplate.getForObject(String.format(resourceServiceHost + RESOURCE_ENDPOINT_GET, id), ByteArrayResource.class));
    }

    @Recover
    private Optional<ByteArrayResource> recoverResourceServiceCall(RuntimeException exception) {
        log.error(String.format("Can't connect to the resource service, cause: %s, retries: %d", exception.getMessage(), maxRetries));
        return Optional.empty();
    }

    @Retryable(value = {RuntimeException.class}, maxAttemptsExpression = "${caller.maxRetries}", backoff = @Backoff(value = 500L))
    public void callSongService(SongMetadata metadata) {
        log.info(String.format("Call endpoint: %s", SONG_METADATA_ENDPOINT));
        restTemplate.postForLocation(songServiceHost + SONG_METADATA_ENDPOINT, metadata);
    }

    @Recover
    private void recoverSongServiceCall(RuntimeException exception) {
        log.error(String.format("Can't connect to the song service, cause: %s, retries: %d", exception.getMessage(), maxRetries));
    }

    public Optional<List<StorageDto>> callStorageService() {
        log.info(String.format("Call endpoint: %s", STORAGE_ENDPOINT));
        CircuitBreaker circuitBreaker = registry.circuitBreaker("storage");
        Supplier<ResponseEntity<StorageDto[]>> responseEntitySupplier = () -> restTemplate.exchange(storageServiceHost + STORAGE_ENDPOINT, HttpMethod.GET, new HttpEntity<>(null), StorageDto[].class);
        ResponseEntity<StorageDto[]> storageDtos = Try.ofSupplier(circuitBreaker.decorateSupplier(responseEntitySupplier))
                .recover(throwable -> {
                    log.error("Storage service unavailable, returned cashed result");
                    return savedEntity;})
                .get();
        saveResponseEntity(storageDtos);
        return Optional.ofNullable(Arrays.stream(storageDtos.getBody()).collect(Collectors.toList()));
    }

    private void saveResponseEntity(ResponseEntity<StorageDto[]> storageDtos) {
        if (storageDtos.getStatusCodeValue() == 200) {
                savedEntity = new ResponseEntity<StorageDto[]>(storageDtos.getBody(), HttpStatus.NO_CONTENT);
        }
    }


    @Retryable(value = {RuntimeException.class}, maxAttemptsExpression = "${caller.maxRetries}", backoff = @Backoff(value = 500L))
    public Optional<Boolean> postToResourceService(SaveSongDto saveSongDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename("temp.mp3")
                .build();

        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(saveSongDto.getResource(), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);
        body.add("storageId", saveSongDto.getStorageId());
        body.add("resourceId", saveSongDto.getResourceId());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        log.info(String.format("Call endpoint to reupload data: %s",RESOURCE_ENDPOINT_POST));
        restTemplate.postForLocation(resourceServiceHost + RESOURCE_ENDPOINT_POST, requestEntity);

        return Optional.of(Boolean.TRUE);
    }

    @Recover
    private Optional<Boolean> recoverPostToResourceServiceCall(RuntimeException exception) {
        log.error(String.format("Can't connect to the resource service, cause: %s, retries: %d", exception.getMessage(), maxRetries));
        return Optional.of(Boolean.FALSE);
    }
}
