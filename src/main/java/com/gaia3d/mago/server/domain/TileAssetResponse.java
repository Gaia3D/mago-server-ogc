package com.gaia3d.mago.server.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
public class TileAssetResponse extends Pagination {
    List<TileAsset> assets;

    public static TileAssetResponse from(TileAssetRequest request, TileAsset asset) {
        return TileAssetResponse.builder()
                .assets(List.of(asset))
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }

    public static TileAssetResponse from(TileAssetRequest request, List<TileAsset> assets) {
        return TileAssetResponse.builder()
                .assets(assets)
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }
}
