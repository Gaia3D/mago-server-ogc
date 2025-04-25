package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class AssetRequest extends Pagination {
    private int id;
    private String userId;
    private String name;
    private String inputPath;
    private String outputPath;
    private String size;
    private String date;
    private String type;
    private ProcessType statusCode;
}
