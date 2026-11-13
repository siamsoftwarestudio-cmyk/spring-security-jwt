package com.example.security.jwt.repository;

import com.example.security.jwt.entity.Order;
import com.example.security.jwt.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOwnerOrderByCreatedAtDesc(User owner);
}
