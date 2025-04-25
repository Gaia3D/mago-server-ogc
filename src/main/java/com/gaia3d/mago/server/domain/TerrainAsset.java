package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.*;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TerrainAsset {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String projection;
    private String crs;
    private String interpolationType;
    private String refineIntensity;
    private Boolean debugMode;
    private String minLevel;
    private String maxLevel;

    private String size;
    private String date;
    private ProcessType statusCode;

    public static TerrainAsset from(TerrainAssetRequest request) {
        File output = new File(request.getOutputPath());
        return TerrainAsset.builder()
                .name(output.getName())
                .userId(request.getUserId())
                .inputPath(request.getInputPath())
                .outputPath(request.getOutputPath())
                .projection(request.getProjection())
                .crs(request.getCrs())
                .debugMode(request.getDebugMode())
                .minLevel(request.getMinLevel())
                .maxLevel(request.getMaxLevel())
                .interpolationType(request.getInterpolationType())
                .refineIntensity(request.getRefineIntensity())
                .statusCode(ProcessType.READY)
                .build();
    }
}
