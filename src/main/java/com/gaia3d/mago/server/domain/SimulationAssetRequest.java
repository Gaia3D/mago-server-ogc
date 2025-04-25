package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class SimulationAssetRequest extends Pagination {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String projection;

    private String size;
    private String date;
    private ProcessType statusCode;
}
