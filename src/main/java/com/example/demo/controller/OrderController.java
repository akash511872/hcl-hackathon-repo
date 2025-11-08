package com.example.demo.controller;

import com.example.demo.dto.OrderPlacementRequest;
import com.example.demo.dto.OrderPlacementResponse;
import com.example.demo.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Place order endpoint
     * 
     * @param request - Contains customer ID, product ID, quantity, cost, and currency
     * @return OrderPlacementResponse with success status and order details
     */
    @PostMapping("/placeOrder")
    public ResponseEntity<OrderPlacementResponse> placeOrder(@RequestBody OrderPlacementRequest request) {
        logger.info("Received order placement request: customer={}, product={}, quantity={}", 
            request.getCustomerId(), request.getProductId(), request.getQuantity());
        
        try {
            OrderPlacementResponse response = orderService.placeOrder(request);
            
            if (response.isSuccess()) {
                logger.info("Order placed successfully: {}", 
                    response.getOrderDetails() != null ? response.getOrderDetails().getOrderNumber() : "N/A");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Order placement failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Unexpected error during order placement: {}", e.getMessage(), e);
            OrderPlacementResponse errorResponse = new OrderPlacementResponse(false, 
                "An unexpected error occurred. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
