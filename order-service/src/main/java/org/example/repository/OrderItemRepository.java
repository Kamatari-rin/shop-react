package org.example.repository;

import org.example.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Integer> {
    // Элементы заказа по orderId с пагинацией
    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY id ASC OFFSET :offset LIMIT :limit")
    Flux<OrderItem> findByOrderId(Integer orderId, long offset, int limit);

    // Элементы заказа без пагинации (для getOrderDetail)
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    Flux<OrderItem> findByOrderId(Integer orderId);

    // Подсчёт элементов заказа
    @Query("SELECT COUNT(*) FROM order_items WHERE order_id = :orderId")
    Mono<Long> countByOrderId(Integer orderId);
}