package com.jumani.rutaseg.domain;

import com.jumani.rutaseg.dto.result.Error;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Entity
@FieldNameConstants
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "cuit")
    private Long cuit;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "consignees", foreignKey = @ForeignKey(name = "`fk_consignees-clients`"),
            joinColumns = @JoinColumn(name = "client_id"))
    private List<Consignee> consignees;

    public Client(User user, String name, String phone, Long cuit) {
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.cuit = cuit;
        this.consignees = new ArrayList<>();

        if (Objects.nonNull(this.cuit)) {
            consignees.add(new Consignee(this.name, this.cuit));
        }
    }

    private Client() {
    }

    public Optional<Error> addConsignee(Consignee consignee) {
        final String consigneeName = consignee.getName();
        final long consigneeCuit = consignee.getCuit();

        for (Consignee existingConsignee : consignees) {
            if (existingConsignee.getName().equals(consigneeName) || existingConsignee.getCuit() == consigneeCuit) {
                return Optional.of(new Error("duplicated_consignee", "consignee with the same name or cuit already exists"));
            }
        }

        consignees.add(consignee);
        return Optional.empty();
    }

    public void update(String name, String phone, Long cuit, User user) {
        this.name = name;
        this.phone = phone;
        this.cuit = cuit;
        this.user = user;
    }

    public Long getUserId() {
        return Optional.ofNullable(this.user).map(User::getId).orElse(null);
    }


}
