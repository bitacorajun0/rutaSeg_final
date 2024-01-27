package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryExtended {
    boolean existsByEmail(String email);
    Optional<User> findOneByEmail(String email);
}