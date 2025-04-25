package com.gaia3d.mago.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class NotificationResponse {
    private String status;
    private String type;
    private String name;
    private String detail;
    private List<ConversionProcess> conversionProcesses;
}
