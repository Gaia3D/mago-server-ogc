package com.gaia3d.mago.server.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Pagination {
    private int pageNumber;
    private int pageSize;
    private int totalElements;
    private int totalPages;

    private String filterWord;
    private String assetType;
    private String assetStatus;
    private String assetSort;
}
