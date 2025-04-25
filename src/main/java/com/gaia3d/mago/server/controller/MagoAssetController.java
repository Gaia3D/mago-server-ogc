package com.gaia3d.mago.server.controller;

import com.gaia3d.mago.server.domain.Asset;
import com.gaia3d.mago.server.domain.AssetRequest;
import com.gaia3d.mago.server.domain.AssetResponse;
import com.gaia3d.mago.server.service.MagoAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/api/asset")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MagoAssetController {

    @Autowired
    private MagoAssetService assetService;

    @GetMapping("")
    public ResponseEntity<AssetResponse> getListByPage(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam(required = false) String assetType, @RequestParam(required = false) String assetStatus, @RequestParam(required = false) String assetSort, @RequestParam(required = false) String filterWord) {
        String userId = "admin";
        AssetRequest request = AssetRequest.builder().userId(userId).pageNumber(pageNumber).pageSize(pageSize).assetType(assetType).assetStatus(assetStatus).assetSort(assetSort).filterWord(filterWord).build();
        List<Asset> assets = assetService.getAllList(request, userId);
        AssetResponse response = AssetResponse.from(request, assets);
        return checkResponseEntitiy(response);
    }

    private ResponseEntity<AssetResponse> checkResponseEntitiy(AssetResponse response) {
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
