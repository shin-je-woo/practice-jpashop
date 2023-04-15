package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;


    public List<OrderQueryDTO> findOrderQueryDtos() {
        List<OrderQueryDTO> result = findOrders(); //query 1번 -> N개
        result.forEach(o -> {
            List<OrderItemQueryDTO> orderItems = findOrderItems(o.getOrderId()); //query N번
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDTO> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDTO(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDTO.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDTO> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderQueryDTO(o.id, m.name, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDTO.class)
                .getResultList();
    }

    public List<OrderQueryDTO> findAllByDto_optimization() {
        List<OrderQueryDTO> result = findOrders();

        Map<Long, List<OrderItemQueryDTO>> orderItemMap = findOrderItemMap(toOrderIds(result));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDTO>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDTO> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDTO(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDTO.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDTO>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDTO::getOrderId));

        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDTO> result) {
        return result.stream()
                .map(OrderQueryDTO::getOrderId)
                .collect(Collectors.toList());
    }
}
