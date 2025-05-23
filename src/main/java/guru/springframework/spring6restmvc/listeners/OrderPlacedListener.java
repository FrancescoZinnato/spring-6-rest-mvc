package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedListener {

    @Async
    @EventListener
    public void listen(OrderPlacedEvent event) {

        //todo Need to send the event to Kafka
        System.out.println("Order Placed Event Received");
    }

}
