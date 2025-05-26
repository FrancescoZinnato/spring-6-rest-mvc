package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class DrinkListenerKafkaConsumer {

    AtomicInteger iceColdMessageCounter = new AtomicInteger(0);
    AtomicInteger coldMessageCounter = new AtomicInteger(0);
    AtomicInteger coolMessageCounter = new AtomicInteger(0);

    @KafkaListener(groupId = "KafkaIntegrationTest", topics = KafkaConfig.DRINK_REQUEST_ICE_COLD_TOPIC)
    public void listenIceCold() {
        log.debug("Listening Kafka Consumer ICE_COLD");
        iceColdMessageCounter.incrementAndGet();
    }

    @KafkaListener(groupId = "KafkaIntegrationTest", topics = KafkaConfig.DRINK_REQUEST_COLD_TOPIC)
    public void listenCold() {
        log.debug("Listening Kafka Consumer COLD");
        coldMessageCounter.incrementAndGet();
    }

    @KafkaListener(groupId = "KafkaIntegrationTest", topics = KafkaConfig.DRINK_REQUEST_COOL_TOPIC)
    public void listenCool() {
        log.debug("Listening Kafka Consumer COOL");
        coolMessageCounter.incrementAndGet();
    }

}
