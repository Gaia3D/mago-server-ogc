package com.gaia3d.mago.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaia3d.mago.server.domain.TerrainAsset;
import com.gaia3d.mago.server.domain.TerrainAssetRequest;
import com.gaia3d.mago.server.domain.TerrainAssetRequestDto;
import com.gaia3d.mago.server.domain.TerrainAssetResponse;
import com.gaia3d.mago.server.service.MagoTerrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/api/terrainer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MagoTerrainerController {

    @Autowired
    private MagoTerrainerService terrainerService;

    @GetMapping("")
    public ResponseEntity<TerrainAssetResponse> getListByPage(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam(required = false) String assetType, @RequestParam(required = false) String assetStatus, @RequestParam(required = false) String filterWord) {
        String userId = "admin";

        TerrainAssetRequest request = TerrainAssetRequest.builder()
                .userId(userId)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .assetType(assetType)
                .assetStatus(assetStatus)
                .filterWord(filterWord)
                .build();
        List<TerrainAsset> terrainMeshList = terrainerService.getListWithPagination(request, userId);
        TerrainAssetResponse response = TerrainAssetResponse.from(request, terrainMeshList);
        return checkResponseEntity(response);
    }

    @PostMapping("")
    public ResponseEntity<TerrainAssetResponse> uploadByMultipartFile(@RequestParam(value = "body") String requestDtoString, @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        ObjectMapper mapper = new ObjectMapper();
        TerrainAssetResponse response = null;
        try {
            TerrainAssetRequestDto requestDto = mapper.readValue(requestDtoString, TerrainAssetRequestDto.class);
            TerrainAssetRequest request = TerrainAssetRequest.builder()
                    .userId("admin")
                    .name(requestDto.getName())
                    .debugMode(Boolean.parseBoolean(requestDto.getDebugMode()))
                    .minLevel(requestDto.getMinLevel())
                    .maxLevel(requestDto.getMaxLevel())
                    .refineIntensity(requestDto.getRefineIntensity())
                    .interpolationType(requestDto.getInterpolationType())
                    .build();
            terrainerService.saveTempFiles(files, request);
            TerrainAsset terrainMesh = terrainerService.create(request);
            response = TerrainAssetResponse.from(request, terrainMesh);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return checkResponseEntity(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<TerrainAssetResponse> deleteSample(@PathVariable("name") String number) {
        TerrainAssetRequest request = TerrainAssetRequest.builder()
                .userId("admin")
                .name(number)
                .build();
        TerrainAsset terrainMesh = terrainerService.delete(request);
        TerrainAssetResponse response = TerrainAssetResponse.from(request, terrainMesh);
        return checkResponseEntity(response);
    }

    @GetMapping("/{name}/download")
    public ResponseEntity<byte[]> downloadTileset(@PathVariable("name") String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", String.format("%s.zip", name));
        TerrainAssetRequest request = TerrainAssetRequest.builder()
                .userId("admin")
                .name(name)
                .build();
        byte[] zipFile = terrainerService.download(request);
        return ResponseEntity.ok()
                .headers(headers)
                .body(zipFile);
    }

    private ResponseEntity<TerrainAssetResponse> checkResponseEntity(TerrainAssetResponse response) {
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
