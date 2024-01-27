package com.jumani.rutaseg.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Table(name = "driver_datas")
@Slf4j
public class DriverData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "driverData", fetch = FetchType.LAZY)
    private Order order;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "chasis")
    private String chasis;

    @Column(name = "semi")
    private String semi;

    @Column(name = "company")
    private String company;

    public DriverData() {
    }

    public DriverData(String name, String phone,
                      String chasis, String semi,
                      String company) {
        this.name = name;
        this.phone = phone;
        this.chasis = chasis;
        this.semi = semi;
        this.company = company;
    }
}
