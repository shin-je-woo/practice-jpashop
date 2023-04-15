package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy때문에 프록시객체 강제 초기화
            order.getDelivery().getStatus(); //Lazy때문에 프록시객체 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy때문에 프록시객체 강제 초기화(OrderItem, Item 둘 동시에)
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    List<OrderDTO> ordersV2() {
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(OrderDTO::new)
                .collect(toList());
    }

    @GetMapping("/api/v3/orders")
    List<OrderDTO> ordersV3() {
        return orderRepository.findAllWithItem().stream()
                .map(OrderDTO::new)
                .collect(toList());
    }

    @GetMapping("/api/v3.1/orders")
    List<OrderDTO> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                 @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderRepository.findAllWithMemberDelivery(offset, limit).stream()
                .map(OrderDTO::new)
                .collect(toList());
    }

    @GetMapping("/api/v4/orders")
    List<OrderQueryDTO> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    List<OrderQueryDTO> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @Getter
    static class OrderDTO {

        private Long orderId;
        private String username;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDTO> orderItems;

        public OrderDTO(Order order) {
            orderId = order.getId();
            username = order.getMember().getName(); // Lazy 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // Lazy 초기화
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDTO::new)
                    .collect(toList());
        }

    }

    @Getter
    static class OrderItemDTO {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDTO(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }


}
