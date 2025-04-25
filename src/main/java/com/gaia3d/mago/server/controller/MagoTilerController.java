package com.gaia3d.mago.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaia3d.mago.server.domain.TileAsset;
import com.gaia3d.mago.server.domain.TileAssetRequest;
import com.gaia3d.mago.server.domain.TileAssetRequestDto;
import com.gaia3d.mago.server.domain.TileAssetResponse;
import com.gaia3d.mago.server.service.MagoTilerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/api/tiler")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MagoTilerController {

    private final MagoTilerService tilerService;

    @GetMapping("")
    public ResponseEntity<TileAssetResponse> getListByPage(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam(required = false) String assetType, @RequestParam(required = false) String assetStatus, @RequestParam(required = false) String filterWord) {
        String userId = "admin";

        TileAssetRequest request = TileAssetRequest.builder().userId(userId).pageNumber(pageNumber).pageSize(pageSize).assetType(assetType).assetStatus(assetStatus).filterWord(filterWord).build();
        List<TileAsset> tileAssets = tilerService.getListWithPagination(request, userId);
        TileAssetResponse response = TileAssetResponse.from(request, tileAssets);
        return checkResponseEntitiy(response);
    }

    @PostMapping("")
    public ResponseEntity<TileAssetResponse> uploadByMultipartFile(@RequestParam(value = "body") String requestDtoString, @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        ObjectMapper mapper = new ObjectMapper();
        TileAssetResponse response = null;
        try {
            TileAssetRequestDto requestDto = mapper.readValue(requestDtoString, TileAssetRequestDto.class);
            TileAssetRequest request = TileAssetRequest.builder().userId("admin").name(requestDto.getName()).inputFormat(requestDto.getInputFormat()).outputFormat(requestDto.getOutputFormat()).projection(requestDto.getProjection()).crs(requestDto.getCrs()).debugMode(Boolean.parseBoolean(requestDto.getDebugMode())).rotateX(requestDto.getRotateX()).pointRatio(requestDto.getPointRatio()).heightColumn(requestDto.getHeightColumn()).altitudeColumn(requestDto.getAltitudeColumn()).minLevel(requestDto.getMinLevel()).maxLevel(requestDto.getMaxLevel()).minGeometricError(requestDto.getMinGeometricError()).maxGeometricError(requestDto.getMaxGeometricError()).ignoreTexture(Boolean.parseBoolean(requestDto.getIgnoreTexture())).build();
            tilerService.saveTempFiles(files, request);
            TileAsset tileAsset = tilerService.create(request);
            response = TileAssetResponse.from(request, tileAsset);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return checkResponseEntitiy(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<TileAssetResponse> deleteSample(@PathVariable("name") String number) {
        TileAssetRequest request = TileAssetRequest.builder().userId("admin").name(number).build();
        TileAsset tileAsset = tilerService.delete(request);
        TileAssetResponse response = TileAssetResponse.from(request, tileAsset);
        return checkResponseEntitiy(response);
    }

    @GetMapping("/{name}/download")
    public ResponseEntity<byte[]> downloadTileset(@PathVariable("name") String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", String.format("%s.zip", name));
        TileAssetRequest request = TileAssetRequest.builder().userId("admin").name(name).build();
        byte[] zipFile = tilerService.download(request);
        return ResponseEntity.ok().headers(headers).body(zipFile);
    }

    private ResponseEntity<TileAssetResponse> checkResponseEntitiy(TileAssetResponse response) {
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
