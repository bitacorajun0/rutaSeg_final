package com.jumani.rutaseg.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Entity
@Table(name = "customs_datas")
@Slf4j
public class CustomsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "customsData", fetch = FetchType.LAZY)
    private Order order;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    public CustomsData() {
    }

    public CustomsData(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
