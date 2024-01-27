package com.jumani.rutaseg.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;


@Getter
@Entity
@Table(name = "containers", indexes = {
        @Index(name = "IDX_CONTAINERS_CODE", columnList = "code")})
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "code")
    private String code;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ContainerType type;

    @Column(name = "repackage")
    private boolean repackage;

    @Column(name = "bl")
    private String bl;

    @Column(name = "pema")
    private String pema;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "container_destinations", foreignKey = @ForeignKey(name = "`fk_destinations-containers`"),
            joinColumns = @JoinColumn(name = "id"), indexes = {
            @Index(name = "IDX_CONTAINER_DESTINATIONS_CODE", columnList = "code")})
    private List<Destination> destinations;

    public Container() {
    }

    public Container(String code,
                     ContainerType type,
                     boolean repackage,
                     String bl,
                     String pema,
                     List<Destination> destinations) {
        this.code = code;
        this.type = type;
        this.repackage = repackage;
        this.bl = bl;
        this.pema = pema;
        this.destinations = destinations;
    }
}

