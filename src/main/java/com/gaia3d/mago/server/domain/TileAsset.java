package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.*;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TileAsset {
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

    public static TileAsset from(TileAssetRequest request) {
        File output = new File(request.getOutputPath());
        return TileAsset.builder()
                .name(output.getName())
                .userId(request.getUserId())
                .inputPath(request.getInputPath())
                .outputPath(request.getOutputPath())
                .inputFormat(request.getInputFormat())
                .outputFormat(request.getOutputFormat())
                .projection(request.getProjection())
                .crs(request.getCrs())
                .projCode(request.getProjCode())

                .debugMode(request.getDebugMode())

                .rotateX(request.getRotateX())
                //.swapUpAxis(request.getSwapUpAxis())
                //.flipUpAxis(request.getFlipUpAxis())

                //.nameColumn(request.getNameColumn())
                .heightColumn(request.getHeightColumn())
                .altitudeColumn(request.getAltitudeColumn())

                .pointRatio(request.getPointRatio())
                //.pointSkip(request.getPointSkip())
                .minLevel(request.getMinLevel())
                .maxLevel(request.getMaxLevel())
                .minGeometricError(request.getMinGeometricError())
                .maxGeometricError(request.getMaxGeometricError())
                .ignoreTexture(request.getIgnoreTexture())

                .statusCode(ProcessType.READY)
                .build();
    }
}
