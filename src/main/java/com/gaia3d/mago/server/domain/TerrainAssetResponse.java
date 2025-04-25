package com.gaia3d.mago.server.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
public class TerrainAssetResponse extends Pagination {
    List<TerrainAsset> assets;

    public static TerrainAssetResponse from(TerrainAssetRequest request, TerrainAsset asset) {
        return TerrainAssetResponse.builder()
                .assets(List.of(asset))
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }

    public static TerrainAssetResponse from(TerrainAssetRequest request, List<TerrainAsset> assets) {
        return TerrainAssetResponse.builder()
                .assets(assets)
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }
}
