package com.gaia3d.mago.server.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
public class SimulationAssetResponse extends Pagination {
    List<SimulationAsset> assets;

    public static SimulationAssetResponse from(SimulationAssetRequest request, SimulationAsset data) {
        return SimulationAssetResponse.builder()
                .assets(List.of(data))
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }

    public static SimulationAssetResponse from(SimulationAssetRequest request, List<SimulationAsset> data) {
        return SimulationAssetResponse.builder()
                .assets(data)
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }
}
