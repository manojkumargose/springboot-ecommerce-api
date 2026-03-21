package scheduler;

import com.example.ecommerce.service.DemandTrackingService;
import com.example.ecommerce.service.DynamicPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PricingScheduler {

    private static final Logger log = LoggerFactory.getLogger(PricingScheduler.class);

    private final DynamicPricingService dynamicPricingService;
    private final DemandTrackingService demandTrackingService;

    public PricingScheduler(DynamicPricingService dynamicPricingService,
                            DemandTrackingService demandTrackingService) {
        this.dynamicPricingService = dynamicPricingService;
        this.demandTrackingService = demandTrackingService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void recalculatePrices() {
        log.info("Scheduled price recalculation started...");
        try {
            int adjusted = dynamicPricingService.recalculateAllPrices();
            log.info("Scheduled price recalculation complete. {} products adjusted.", adjusted);
        } catch (Exception e) {
            log.error("Price recalculation FAILED: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldEvents() {
        log.info("Cleaning up old demand events...");
        try {
            demandTrackingService.cleanupOldEvents(7);
            log.info("Cleanup complete.");
        } catch (Exception e) {
            log.error("Cleanup FAILED: {}", e.getMessage(), e);
        }
    }
}