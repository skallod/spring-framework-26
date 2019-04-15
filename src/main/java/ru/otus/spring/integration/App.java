package ru.otus.spring.integration;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
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
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@IntegrationComponentScan
@SuppressWarnings({"resource", "Duplicates", "InfiniteLoopStatement"})
@ComponentScan
@Configuration
@EnableIntegration
public class App {

    private static final Log log = LogFactory.getLog(App.class);

    private static final String[] MENU = {"coffee", "tea", "smoothie", "whiskey", "beer", "cola", "water"};

    @Bean
    public QueueChannel itemsChannel() {
        return MessageChannels.queue(10).get();
    }

    @Bean
    public PublishSubscribeChannel foodChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel icedChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel notIcedChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean (name = PollerMetadata.DEFAULT_POLLER )
    public PollerMetadata poller () {
        return Pollers.fixedRate(100).maxMessagesPerPoll(2).get() ;
    }

    @Bean
    public IntegrationFlow cafeFlow() {
        return IntegrationFlows.from("itemsChannel")
                .split()
                .log("galuzin test")
                .<OrderItem,Boolean>route(item->item.isIced(),mapping
                        ->mapping.subFlowMapping(true,sf->sf.channel("icedChannel"))
                        .subFlowMapping(false,sf->sf.channel("notIcedChannel")))
                .get();
    }
    @Bean
    public IntegrationFlow notIcedFlow() {
        return IntegrationFlows.from("notIcedChannel")
                .split()
                .handle("kitchenService", "cook")
                .log("galuzin2 test")
                .aggregate()
                .channel("foodChannel")
                .get();
    }
    @Bean
    public IntegrationFlow icedFlow() {
        return IntegrationFlows.from("icedChannel")
                .split()
                .handle("kitchenService", "cookIced")
                .log("galuzin3 test")
                .aggregate()
                .channel("foodChannel")
                .get();
    }

    public static void main(String[] args) throws Exception {
        AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(App.class);

        // here we works with cafe using interface
        Cafe cafe = ctx.getBean(Cafe.class);

        while (true) {
            Thread.sleep(1000);

            Collection<OrderItem> items = generateOrderItems();
            log.info("New orderItems: " +
                    items.stream().map(OrderItem::getItemName)
                            .collect(Collectors.joining(",")));
            Collection<Food> food = cafe.process(items);
            log.info("Ready food: " + food.stream()
                    .map(Food::getName)
                    .collect(Collectors.joining(",")));
        }
    }

    private static OrderItem generateOrderItem() {
        int i = RandomUtils.nextInt(0, MENU.length);
        return new OrderItem(MENU[i],i%2==0);
    }

    private static Collection<OrderItem> generateOrderItems() {
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(1, 5); ++i) {
            items.add(generateOrderItem());
        }
        return items;
    }
}
