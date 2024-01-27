package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.stereotype.Repository;




@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryExtended {

}






