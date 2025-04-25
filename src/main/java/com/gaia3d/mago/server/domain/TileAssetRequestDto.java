package com.gaia3d.mago.server.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TileAssetRequestDto {
    private String name;
    private String outputFormat;
    private String inputFormat;
    private String projection;
    private String crs;
    private String projCode;
    private String debugMode;

    private String rotateX;
    private String heightColumn;
    private String altitudeColumn;

    private String pointRatio;
    private String minLevel;
    private String maxLevel;
    private String maxCount;
    private String minGeometricError;
    private String maxGeometricError;
    private String ignoreTexture;
}
