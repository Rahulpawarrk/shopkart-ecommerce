package com.example.ecommerce.order.task;

import com.example.ecommerce.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled background task to automatically cancel abandoned PENDING orders (older than 15 minutes),
 * releasing locked inventory back into the store stock and preventing denial-of-inventory attacks.
 */
@Component
public class AbandonedOrderCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(AbandonedOrderCleanupTask.class);

    private final OrderService orderService;

    public AbandonedOrderCleanupTask(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Runs every 60 seconds to inspect and cancel stale PENDING orders.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    public void cleanupAbandonedOrders() {
        try {
            int cancelledCount = orderService.cancelExpiredPendingOrders(15);
            if (cancelledCount > 0) {
                logger.info("AbandonedOrderCleanupTask: Cancelled {} expired pending orders and restored inventory.", cancelledCount);
            }
        } catch (Exception e) {
            logger.error("Error running AbandonedOrderCleanupTask", e);
        }
    }
}
