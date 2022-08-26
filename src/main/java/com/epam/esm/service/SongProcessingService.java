package com.epam.esm.service;

import com.epam.esm.dto.SaveSongDto;
import com.epam.esm.dto.StorageDto;
import com.epam.esm.exception.StorageNotExistException;
import com.epam.esm.model.SongMetadata;
import com.epam.esm.model.StorageType;
import com.epam.esm.parser.MP3ApplicationParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SongProcessingService {
    private MP3ApplicationParser parser;
    private CallerService callerService;

    public SongProcessingService(MP3ApplicationParser parser, CallerService callerService) {
        this.parser = parser;
        this.callerService = callerService;
    }

    public void process(long id, String traceId) {
        callerService.callResourceService(id,traceId)
                .ifPresent(res -> {
                    SongMetadata metadata = parser.parse(res);
                    metadata.setResourceId(id);
                    callerService.callSongService(metadata,traceId);
                    SaveSongDto saveSongDto = SaveSongDto.builder()
                            .resource(res.getByteArray())
                            .resourceId(id)
                            .storageId(getStorage().getId())
                            .build();
                    callerService.postToResourceService(saveSongDto,traceId);
                });
    }

    private StorageDto getStorage() {
        Optional<List<StorageDto>> storageDtos = callerService.callStorageService();
        List<StorageDto> permanentStorages = storageDtos.orElseThrow(() -> {
                    log.error("List of storages in storage service  is empty");
                    return new StorageNotExistException("List of storages in storage service  is empty");
                })
                .stream()
                .filter(storageDto -> storageDto.getStorageType().equals(StorageType.PERMANENT))
                .collect(Collectors.toList());
        Random random = new Random();
        return permanentStorages.get(random.nextInt(permanentStorages.size()));
    }
}
