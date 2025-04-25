package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.*;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SimulationAsset {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String outputFormat;
    private String inputFormat;
    private String projection;

    private String size;
    private String date;
    private ProcessType statusCode;

    public static SimulationAsset from(SimulationAssetRequest request) {
        File output = new File(request.getOutputPath());
        return SimulationAsset.builder()
                .name(output.getName())
                .userId(request.getUserId())
                .inputPath(request.getInputPath())
                .outputPath(request.getOutputPath())
                .projection(request.getProjection())
                .statusCode(ProcessType.READY)
                .build();
    }
}
