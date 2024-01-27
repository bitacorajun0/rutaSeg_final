package com.jumani.rutaseg.domain;

import com.jumani.rutaseg.util.DateGen;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Table(name = "costs")
@EqualsAndHashCode(exclude = "id")
public class Cost implements DateGen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private double amount;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CostType type;

    @Column(name = "created_by_user_id")
    private long createdByUserId;

    public Cost(double amount, String description, CostType type, long createdByUserId) {
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.createdAt = this.currentDateUTC();
        this.updatedAt = null;
        this.createdByUserId = createdByUserId;
    }

    public Cost() {
    }

    public void update(double amount, String description, CostType type) {
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.updatedAt = this.currentDateUTC();
    }
}