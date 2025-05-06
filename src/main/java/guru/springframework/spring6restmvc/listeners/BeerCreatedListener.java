package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.events.BeerCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BeerCreatedListener {

    @EventListener
    @Async
    public void listen(BeerCreatedEvent beerCreatedEvent) {
        log.info("A new beer has been created with id: {}", beerCreatedEvent.getBeer().getId());
        log.info("Current thread name: {} \n Current thread id: {}", Thread.currentThread().getName(), Thread.currentThread().threadId());

        //todo - adding a real implementation to persist audit record
    }

}
