package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {
    List<Container> findByCode(String code);

    @Query("select c.orderId from Container c join c.destinations cd where cd.code = :destinationCode")
    List<Long> findOrderIdByDestinationCode(String destinationCode);
}
