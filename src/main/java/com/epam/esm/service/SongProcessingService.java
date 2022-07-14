package com.epam.esm.service;

import com.epam.esm.model.SongMetadata;
import com.epam.esm.parser.MP3ApplicationParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SongProcessingService {
    private MP3ApplicationParser parser;
    private CallerService callerService;

    public SongProcessingService(MP3ApplicationParser parser, CallerService callerService) {
        this.parser = parser;
        this.callerService = callerService;
    }

    public void process(long id) {
        callerService.callResourceService(id)
                .ifPresent(res -> {
                    SongMetadata metadata = parser.parse(res);
                    metadata.setResourceId(id);
                    callerService.callSongService(metadata);
                });
    }
}
