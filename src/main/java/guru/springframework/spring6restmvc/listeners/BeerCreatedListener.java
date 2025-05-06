package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.events.BeerCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BeerCreatedListener {

    @EventListener
    public void listen(BeerCreatedEvent beerCreatedEvent) {
        log.info("A new beer has been created with id: {}", beerCreatedEvent.getBeer().getId());

        //todo - adding a real implementation to persist audit record
    }

}
