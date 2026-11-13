package com.example.security.jwt.service;

import com.example.security.jwt.dto.OrderRequest;
import com.example.security.jwt.dto.OrderResponse;
import com.example.security.jwt.dto.OrderStatusRequest;
import com.example.security.jwt.entity.Order;
import com.example.security.jwt.entity.Role;
import com.example.security.jwt.entity.User;
import com.example.security.jwt.exception.ApiException;
import com.example.security.jwt.mapper.OrderMapper;
import com.example.security.jwt.repository.OrderRepository;
import com.example.security.jwt.repository.UserRepository;
import com.example.security.jwt.security.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        User current = currentUser();

        List<Order> orders = current.getRole() == Role.ADMIN
                ? orderRepository.findAll()
                : orderRepository.findAllByOwnerOrderByCreatedAtDesc(current);

        return orderMapper.toResponseList(orders);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = getOrderOrThrow(id);
        ensureCanRead(order);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = orderMapper.toEntity(request, currentUser());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = getOrderOrThrow(id);
        ensureCanModify(order);
        orderMapper.update(order, request);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusRequest request) {
        Order order = getOrderOrThrow(id);
        order.setStatus(request.status());
        return orderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        Order order = getOrderOrThrow(id);
        ensureCanModify(order);
        orderRepository.delete(order);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found: " + id, HttpStatus.NOT_FOUND));
    }

    private void ensureCanRead(Order order) {
        User current = currentUser();
        if (current.getRole() != Role.ADMIN && !order.getOwner().getId().equals(current.getId())) {
            throw new ApiException("You can only view your own orders", HttpStatus.FORBIDDEN);
        }
    }

    private void ensureCanModify(Order order) {
        User current = currentUser();
        if (current.getRole() != Role.ADMIN && !order.getOwner().getId().equals(current.getId())) {
            throw new ApiException("You can only modify your own orders", HttpStatus.FORBIDDEN);
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }
}
