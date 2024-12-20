package com.ecommerce.app.service;

import com.ecommerce.app.dto.OrderRequestDto;
import com.ecommerce.app.entity.Order;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public Order placeOrder(OrderRequestDto orderRequest) {
        Order order = new Order();
        List<Product> products = productRepository.findAllById(orderRequest.getProductIds());
        double totalPrice = products.stream().mapToDouble(Product::getPrice).sum();

        order.setProducts(products);
        order.setTotalPrice(totalPrice);
        order.setIsInvoiceGenerated(false);
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
