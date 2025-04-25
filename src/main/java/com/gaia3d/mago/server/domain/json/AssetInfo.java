package com.gaia3d.mago.server.domain.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssetInfo {
    @JsonProperty("author")
    private String author;
    @JsonProperty("assetName")
    private String assetName;
    @JsonProperty("originalPath")
    private String originalPath;
    @JsonProperty("assetPath")
    private String assetPath;
    @JsonProperty("assetUrl")
    private String assetUrl;
    @JsonProperty("size")
    private String size;

    private String type;
    private ProcessType statusCode;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
