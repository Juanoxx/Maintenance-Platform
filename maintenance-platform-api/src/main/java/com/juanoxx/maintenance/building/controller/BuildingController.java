package com.juanoxx.maintenance.building.controller;

import com.juanoxx.maintenance.building.dto.BuildingRequest;
import com.juanoxx.maintenance.building.dto.BuildingResponse;
import com.juanoxx.maintenance.building.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BuildingResponse> listBuildings() {
        return buildingService.listBuildings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BuildingResponse createBuilding(@Valid @RequestBody BuildingRequest request) {
        return buildingService.createBuilding(request);
    }
}
