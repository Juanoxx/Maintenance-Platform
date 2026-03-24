package com.juanoxx.maintenance.building.service;

import com.juanoxx.maintenance.building.dto.BuildingRequest;
import com.juanoxx.maintenance.building.dto.BuildingResponse;
import com.juanoxx.maintenance.building.entity.Building;
import com.juanoxx.maintenance.building.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public List<BuildingResponse> listBuildings() {
        return buildingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BuildingResponse createBuilding(BuildingRequest request) {
        Building building = new Building();
        building.setName(request.name().trim());
        building.setAddress(request.address().trim());
        building.setCommune(request.commune().trim());
        building.setActive(true);
        return toResponse(buildingRepository.save(building));
    }

    private BuildingResponse toResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getName(),
                building.getAddress(),
                building.getCommune(),
                building.getAdminUserId(),
                building.isActive()
        );
    }
}
