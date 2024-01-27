package com.jumani.rutaseg.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "free_loads", indexes = {
        @Index(name = "IDX_FREE_LOADS_PATENT", columnList = "patent")})
public class FreeLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "patent")
    private String patent;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private FreeLoadType type;

    @Column(name = "weight")
    private String weight;

    @Column(name = "guide")
    private String guide;

    @Column(name = "pema")
    private String pema;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "free_load_destinations", foreignKey = @ForeignKey(name = "`fk_destinations-free_loads`"),
            joinColumns = @JoinColumn(name = "id"), indexes = {
            @Index(name = "IDX_FREE_LOAD_DESTINATIONS_CODE", columnList = "code")})
    private List<Destination> destinations;

    private FreeLoad() {

    }

    public FreeLoad(String patent, FreeLoadType type, String weight, String guide, String pema, List<Destination> destinations) {
        this.patent = patent;
        this.type = type;
        this.weight = weight;
        this.guide = guide;
        this.pema = pema;
        this.destinations = destinations;
    }
}
