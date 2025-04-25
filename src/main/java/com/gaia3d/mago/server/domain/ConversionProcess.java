package com.gaia3d.mago.server.domain;

import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ConversionProcess {
    private int index;
    private String name;
    private ProcessType status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String inputPath;
    private String outputPath;
}
