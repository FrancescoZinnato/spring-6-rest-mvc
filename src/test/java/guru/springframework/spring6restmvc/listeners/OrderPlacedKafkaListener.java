package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.config.KafkaConfig;
import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class OrderPlacedKafkaListener {

    AtomicInteger messageCounter = new AtomicInteger(0);

    @KafkaListener(groupId = "KafkaIntegrationTest", topics = KafkaConfig.ORDER_PLACED_TOPIC)
    public void receive(OrderPlacedEvent orderPlacedEvent) {
        log.debug("Kafka Listener Received OrderPlacedEvent: {}", orderPlacedEvent);
        messageCounter.incrementAndGet();
    }

}
