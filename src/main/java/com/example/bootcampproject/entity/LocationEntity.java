package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_idx")
    private Long idx;

    @Column(name = "location_name", nullable = false, length = 50)
    private String name;

    @Column(name = "location_category", nullable = false, length = 255)
    private String category;

    @Column(name = "location_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location_is_available")
    private Boolean available;

    // 카카오맵에서 검색/선택해 등록한 장소의 주소 (같은 장소 중복 등록 방지용 키로도 사용)
    @Column(name = "location_address", length = 255)
    private String address;

    @Column(name = "location_lat")
    private Double lat;

    @Column(name = "location_lng")
    private Double lng;

    @Builder
    public LocationEntity(String name, String category,
                          String description, Boolean available,
                          String address, Double lat, Double lng) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.available = available;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }
}
