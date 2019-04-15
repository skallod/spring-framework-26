package ru.otus.spring.integration.kitchen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import ru.otus.spring.integration.App;
import ru.otus.spring.integration.domain.Food;
import ru.otus.spring.integration.domain.OrderItem;

@Service
public class KitchenService {

    private static final Log log = LogFactory.getLog(App.class);

    public Food cook(OrderItem orderItem) throws Exception {
        log.info("Cooking " + orderItem.getItemName());
        Thread.sleep(3000);
        log.info("Cooking " + orderItem.getItemName() + " done");
        return new Food(orderItem.getItemName());
    }
    public Food cookIced(OrderItem orderItem) throws Exception {
        log.info("Cooking iced" + orderItem.getItemName());
        Thread.sleep(4000);
        log.info("Cooking iced" + orderItem.getItemName() + " done");
        return new Food(orderItem.getItemName());
    }
}
