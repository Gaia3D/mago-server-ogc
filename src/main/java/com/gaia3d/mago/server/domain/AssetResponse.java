package com.gaia3d.mago.server.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
public class AssetResponse extends Pagination {
    List<Asset> assets;

    public static AssetResponse from(AssetRequest request, Asset asset) {
        return AssetResponse.builder()
                .assets(List.of(asset))
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }

    public static AssetResponse from(AssetRequest request, List<Asset> asset) {
        return AssetResponse.builder()
                .assets(asset)
                .pageNumber(request.getPageNumber())
                .pageSize(request.getPageSize())
                .totalElements(request.getTotalElements())
                .totalPages(request.getTotalPages())
                .build();
    }
}
