package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TerrainAssetRequestDto {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String projection;
    private String crs;
    private String interpolationType;
    private String refineIntensity;
    private String debugMode;
    private String minLevel;
    private String maxLevel;

    private String size;
    private String date;
    private ProcessType statusCode;
}
