package com.gaia3d.mago.server.service;

import com.gaia3d.mago.server.domain.Asset;
import com.gaia3d.mago.server.domain.AssetRequest;
import com.gaia3d.mago.server.domain.Pagination;
import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagoAssetService implements DefaultConverterService {
    private final ExecutionStatusService executionStatusService;

    @Value("${data.path}")
    private String DATA_PATH;

    @Value("${asset.path}")
    private String ASSET_PATH;

    private File getOutputPath() {
        File outputDir = new File(ASSET_PATH);
        if (!outputDir.exists() && outputDir.mkdirs()) {
            log.debug("Output directory is created");
        }
        return outputDir;
    }

    private File getOutputPath(String subDir) {
        File outputDir = getOutputPath();
        File subTempDir = new File(outputDir, subDir);
        if (!subTempDir.exists() && subTempDir.mkdirs()) {
            log.debug("Sub directory {} is created.", subTempDir.getName());
        }
        return subTempDir;
    }


    public String getAssetType(File assetFolder) {
        File tilesetJson = new File(assetFolder, "tileset.json");
        if (tilesetJson.exists()) {
            return "tileset";
        }
        File layerJson = new File(assetFolder, "layer.json");
        if (layerJson.exists()) {
            return "terrain";
        }
        /*File jsonIndex = new File(assetFolder, "JsonIndex.json");
        if (jsonIndex.exists()) {
            return "simulation";
        }*/
        return "unknown";
    }

    public List<Asset> getAllList(Pagination request, String userId) {
        String filterAssetType = request.getAssetType() == null ? "" : request.getAssetType();
        String filterAssetStatus = request.getAssetStatus() == null ? "" : request.getAssetStatus();
        String filterAssetSort = request.getAssetSort() == null ? "" : request.getAssetSort();

        List<Asset> assets = new ArrayList<>();
        File output = getOutputPath();

        // recursive
        //List<File> files = (List<File>) FileUtils.listFiles(output, null, true);
        File[] files = output.listFiles();
        if (output.isDirectory() && files != null) {
            for (File child : files) {
                if (child.isDirectory()) {
                    if (request.getFilterWord() != null && !StringUtils.containsIgnoreCase(child.getName(), request.getFilterWord(), Locale.KOREAN)) {
                        continue;
                    }

                    String assetType = getAssetType(child);
                    if (!filterAssetType.isBlank() && !filterAssetType.equalsIgnoreCase(assetType)) {
                        continue;
                    }

                    try {
                        FileTime creationTime = (FileTime) Files.getAttribute(child.toPath(), "creationTime");
                        Date date = new Date(creationTime.toMillis());
                        LocalDateTime newDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = newDateTime.format(formatter);

                        ProcessType status = executionStatusService.getStatus(child);
                        if (filterAssetStatus.equalsIgnoreCase("running") && status != ProcessType.RUNNING) {
                            continue;
                        } else if (filterAssetStatus.equalsIgnoreCase("success") && status != ProcessType.SUCCESS) {
                            continue;
                        } else if (filterAssetStatus.equalsIgnoreCase("fail") && status != ProcessType.FAIL) {
                            continue;
                        } else if (filterAssetStatus.equalsIgnoreCase("notfound") && status != ProcessType.NOT_FOUND) {
                            continue;
                        }

                        assets.add(Asset.builder()
                                .name(child.getName())
                                .outputPath("./" + output.getName() + "/" + child.getName())
                                .type(assetType)
                                .statusCode(status).date(formattedDateTime).build());
                    } catch (IOException e) {
                        log.error("error", e);
                    }
                }
            }
        }

        if (filterAssetSort.equalsIgnoreCase("latest")) {
            assets.sort(Comparator.comparing(Asset::getDate));
            Collections.reverse(assets);
        } else if (filterAssetSort.equalsIgnoreCase("oldest")) {
            assets.sort(Comparator.comparing(Asset::getDate));
        } else if (filterAssetSort.equalsIgnoreCase("name")) {
            assets.sort(Comparator.comparing(Asset::getName));
        } else if (filterAssetSort.equalsIgnoreCase("status")) {
            assets.sort(Comparator.comparing(Asset::getStatusCode));
            Collections.reverse(assets);
        } else {
            assets.sort(Comparator.comparing(Asset::getDate));
            Collections.reverse(assets);
        }
        AtomicInteger id = new AtomicInteger(1);
        assets.forEach(asset -> asset.setId(id.getAndIncrement()));
        return assets;
    }

    public List<Asset> getListWithPagination(Pagination request, String userId) {
        String assetType = request.getAssetType() == null ? "" : request.getAssetType();
        if (!(assetType.isBlank() || assetType.equals("tileset"))) {
            return new ArrayList<>();
        }

        List<Asset> datas = getAllList(request, userId);
        if (datas.isEmpty()) {
            request.setPageNumber(0);
            request.setPageSize(0);
            request.setTotalPages(0);
            request.setTotalElements(0);
            return datas;
        }

        int pageNumber = request.getPageNumber();
        int pageSize = request.getPageSize();
        int totalElements = datas.size();
        int totalPages = (totalElements / pageSize);
        int remainder = totalElements % pageSize;
        if (remainder > 0) {
            totalPages++;
        }

        if (pageNumber > totalPages) {
            pageNumber = totalPages;
        } else if (pageNumber < 1) {
            pageNumber = 1;
        }

        int start = (pageNumber - 1) * pageSize;
        int end = pageNumber * pageSize;
        if (end > totalElements) {
            end = totalElements;
        }

        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setTotalPages(totalPages);
        request.setTotalElements(totalElements);
        return datas.subList(start, end);
    }

    public Asset delete(AssetRequest request) {
        request.setOutputPath(DATA_PATH);
        Asset asset = Asset.from(request);
        File outputDir = getOutputPath(asset.getName());
        if (outputDir.exists() && outputDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return asset;
    }
}
