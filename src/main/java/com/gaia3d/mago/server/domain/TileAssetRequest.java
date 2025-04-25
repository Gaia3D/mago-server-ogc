package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class TileAssetRequest extends Pagination {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String outputFormat;
    private String inputFormat;
    private String projection;
    private String crs;
    private String projCode;
    private Boolean debugMode;

    private String rotateX;
    private String heightColumn;
    private String altitudeColumn;

    private String pointRatio;
    private String minLevel;
    private String maxLevel;
    private String minGeometricError;
    private String maxGeometricError;

    private Boolean ignoreTexture;

    private String size;
    private String date;
    private ProcessType statusCode;
}
