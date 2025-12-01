package com.ai_marketing_msg_be.domain.customer.repository;

import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByCustomerId(Long customerId);

    Optional<Customer> findByPhone(String phone);

    List<Customer> findByNameContaining(String name);

    List<Customer> findByPhoneContaining(String phone);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.phone LIKE %:keyword%")
    List<Customer> searchByNameOrPhone(@Param("keyword") String keyword);
}