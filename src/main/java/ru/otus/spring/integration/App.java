package ru.otus.spring.integration;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.DirectChannelSpec;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.scheduling.PollerMetadata;
import ru.otus.spring.integration.domain.Food;
import ru.otus.spring.integration.domain.OrderItem;


@IntegrationComponentScan
@SuppressWarnings({"resource", "Duplicates", "InfiniteLoopStatement"})
@ComponentScan
@Configuration
@EnableIntegration
public class App {
    private static final String[] MENU = {"coffee", "tea", "smoothie", "whiskey", "beer", "cola", "water"};

    @Bean
    public QueueChannel itemsChannel() {
        return MessageChannels.queue(10).datatype(OrderItem.class).get();
    }

    @Bean
    public PublishSubscribeChannel foodChannel() {
        return MessageChannels.publishSubscribe().datatype(Food.class).get();
    }

    @Bean (name = PollerMetadata.DEFAULT_POLLER )
    public PollerMetadata poller () {
        return Pollers.fixedRate(1000).get() ;
    }

    @Bean
    public IntegrationFlow cafeFlow() {
        return IntegrationFlows.from("itemsChannel")
                .handle("kitchenService", "cook")
                // TODO*: add router and subflows to process iced and usual items
                // TODO*: add splitter and aggregator
                .channel("foodChannel")
                .get();
    }

    public static void main(String[] args) throws Exception {
        AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

        // here we works with cafe using interface
        Cafe cafe = ctx.getBean(Cafe.class);

        while (true) {
            Thread.sleep(1000);

            OrderItem orderItem = generateOrderItem();
            System.out.println("New orderItem: " + orderItem.getItemName());
            Food food = cafe.process(orderItem);
            System.out.println("Ready food: " + food.getName());
        }
    }

    private static OrderItem generateOrderItem() {
        return new OrderItem(MENU[RandomUtils.nextInt(0, MENU.length)]);
    }
}
