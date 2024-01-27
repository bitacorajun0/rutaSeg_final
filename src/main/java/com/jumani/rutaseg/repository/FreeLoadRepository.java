package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.FreeLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeLoadRepository extends JpaRepository<FreeLoad, Long> {
    List<FreeLoad> findByPatent(String patent);

    @Query("select fl.orderId from FreeLoad fl join fl.destinations fld where fld.code = :destinationCode")
    List<Long> findOrderIdByDestinationCode(String destinationCode);
}
