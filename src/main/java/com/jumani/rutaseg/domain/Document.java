package com.jumani.rutaseg.domain;

import com.jumani.rutaseg.util.DateGen;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
@Getter
@Entity
@Table(name = "documents")
@Slf4j
@EqualsAndHashCode(exclude = "id")
public class Document implements DateGen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "name")
    private String name;

    @Column(name = "resource")
    private String resource;

    private Document() {
    }

    public Document(String name, String resource) {
        this.name = name;
        this.resource = resource;
        this.createdAt = this.currentDateUTC();
    }

}
