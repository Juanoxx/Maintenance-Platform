package com.juanoxx.maintenance.building.entity;

import com.juanoxx.maintenance.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "buildings")
public class Building extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 80)
    private String commune;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(nullable = false)
    private boolean active = true;
}
