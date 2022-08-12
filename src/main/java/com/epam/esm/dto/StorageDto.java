package com.epam.esm.dto;

import com.epam.esm.model.StorageType;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class StorageDto {
    private long id;
    private StorageType storageType;
    private String bucket;
}
