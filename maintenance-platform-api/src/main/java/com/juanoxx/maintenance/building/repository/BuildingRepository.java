package com.juanoxx.maintenance.building.repository;

import com.juanoxx.maintenance.building.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
}
