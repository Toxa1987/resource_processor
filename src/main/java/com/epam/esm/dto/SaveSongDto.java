package com.epam.esm.dto;

import lombok.*;
import org.springframework.core.io.ByteArrayResource;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SaveSongDto {
    private byte[] resource;
    private long resourceId;
    private long storageId;
}
