package com.epam.esm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SongMetadata {
    private String songName;
    private String artist;
    private String album;
    private String length;
    private long resourceId;
}
