package com.gaia3d.mago.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaia3d.mago.server.domain.SimulationAsset;
import com.gaia3d.mago.server.domain.SimulationAssetRequest;
import com.gaia3d.mago.server.domain.SimulationAssetRequestDto;
import com.gaia3d.mago.server.domain.SimulationAssetResponse;
import com.gaia3d.mago.server.service.MagoSimulationService;
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
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MagoSimulationController {

    private final MagoSimulationService simulationService;

    @GetMapping("")
    public ResponseEntity<SimulationAssetResponse> getListByPage(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam(required = false) String assetType, @RequestParam(required = false) String assetStatus, @RequestParam(required = false) String filterWord) {
        String userId = "admin";

        SimulationAssetRequest request = SimulationAssetRequest.builder().userId(userId).pageNumber(pageNumber).pageSize(pageSize).assetType(assetType).assetStatus(assetStatus).filterWord(filterWord).build();
        List<SimulationAsset> simulationAssets = simulationService.getListWithPagination(request, userId);
        SimulationAssetResponse response = SimulationAssetResponse.from(request, simulationAssets);
        return checkResponseEntity(response);
    }

    @PostMapping("")
    public ResponseEntity<SimulationAssetResponse> uploadByMultipartFile(@RequestParam(value = "body") String requestDtoString, @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        ObjectMapper mapper = new ObjectMapper();
        SimulationAssetResponse response = null;
        try {
            SimulationAssetRequestDto requestDto = mapper.readValue(requestDtoString, SimulationAssetRequestDto.class);
            SimulationAssetRequest request = SimulationAssetRequest.builder()
                    .userId("admin")
                    .name(requestDto.getName())
                    .projection(requestDto.getProjection())
                    .build();
            simulationService.saveTempFiles(files, request);
            SimulationAsset simulationAsset = simulationService.create(request);
            response = SimulationAssetResponse.from(request, simulationAsset);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return checkResponseEntity(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<SimulationAssetResponse> deleteSample(@PathVariable("name") String number) {
        SimulationAssetRequest request = SimulationAssetRequest.builder().userId("admin").name(number).build();
        SimulationAsset simulationAsset = simulationService.delete(request);
        SimulationAssetResponse response = SimulationAssetResponse.from(request, simulationAsset);
        return checkResponseEntity(response);
    }

    @GetMapping("/{name}/download")
    public ResponseEntity<byte[]> downloadSimulationset(@PathVariable("name") String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", String.format("%s.zip", name));
        SimulationAssetRequest request = SimulationAssetRequest.builder().userId("admin").name(name).build();
        byte[] zipFile = simulationService.download(request);
        return ResponseEntity.ok().headers(headers).body(zipFile);
    }

    private ResponseEntity<SimulationAssetResponse> checkResponseEntity(SimulationAssetResponse response) {
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
