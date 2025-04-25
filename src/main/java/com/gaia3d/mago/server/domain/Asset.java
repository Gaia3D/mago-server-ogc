package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.*;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Asset {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String size;
    private String date;
    private String type;
    private ProcessType statusCode;

    public static Asset from(AssetRequest request) {
        File output = new File(request.getOutputPath());
        return Asset.builder()
                .name(output.getName())
                .userId(request.getUserId())
                .inputPath(request.getInputPath())
                .outputPath(request.getOutputPath())
                .type(request.getType())
                .statusCode(ProcessType.READY)
                .build();
    }
}
