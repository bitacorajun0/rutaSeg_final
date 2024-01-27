package com.jumani.rutaseg.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "unique_email", columnNames = "email"))
@FieldNameConstants
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "admin")
    private boolean admin;

    public User(String nickname, String password, String email, boolean admin) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.admin = admin;
    }

    private User() {
    }

    public void update(String nickname, String password, String email, boolean admin) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.admin = admin;
    }
}
