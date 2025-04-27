package org.example.repository;

import org.example.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {
    // Детали заказа по id и userId
    @Query("SELECT * FROM orders WHERE id = :id AND user_id = :userId")
    Mono<Order> findByIdAndUserId(Integer id, UUID userId);

    // Список заказов с фильтрами
    @Query("SELECT * FROM orders WHERE user_id = :userId " +
            "AND (:status IS NULL OR status = :status) " +
            "AND (:startDate IS NULL OR order_date >= :startDate) " +
            "AND (:endDate IS NULL OR order_date <= :endDate) " +
            "ORDER BY :orderBy " +
            "OFFSET :offset LIMIT :limit")
    Flux<Order> findOrders(UUID userId, String status, LocalDateTime startDate, LocalDateTime endDate, String orderBy, long offset, int limit);

    // Подсчёт заказов с фильтрами
    @Query("SELECT COUNT(*) FROM orders WHERE user_id = :userId " +
            "AND (:status IS NULL OR status = :status) " +
            "AND (:startDate IS NULL OR order_date >= :startDate) " +
            "AND (:endDate IS NULL OR order_date <= :endDate)")
    Mono<Long> countOrders(UUID userId, String status, LocalDateTime startDate, LocalDateTime endDate);

    // Удаление заказа по id и userId
    @Query("DELETE FROM orders WHERE id = :id AND user_id = :userId")
    Mono<Void> deleteByIdAndUserId(Integer id, UUID userId);
}