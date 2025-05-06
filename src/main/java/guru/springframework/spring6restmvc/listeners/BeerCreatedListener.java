package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.entities.BeerAudit;
import guru.springframework.spring6restmvc.events.BeerCreatedEvent;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.repositories.BeerAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerCreatedListener {

    private final BeerMapper beerMapper;
    private final BeerAuditRepository beerAuditRepository;

    @EventListener
    @Async
    public void listen(BeerCreatedEvent beerCreatedEvent) {
        log.info("A new beer has been created with id: {}", beerCreatedEvent.getBeer().getId());
        log.info("Current thread name: {} \n Current thread id: {}", Thread.currentThread().getName(), Thread.currentThread().threadId());

        // Adding a real implementation to persist audit record
        BeerAudit beerAudit = beerMapper.beerToBeerAudit(beerCreatedEvent.getBeer());
        beerAudit.setAuditEventType("BEER_CREATED");

        if(beerCreatedEvent.getAuthentication() != null && beerCreatedEvent.getAuthentication().getName() != null) {
            beerAudit.setPrincipalName(beerCreatedEvent.getAuthentication().getName());
        }

        BeerAudit savedBeerAudit = beerAuditRepository.save(beerAudit);
        log.debug("Saved beer audit with beer id: {} \n With audit id: {}", savedBeerAudit.getId(), savedBeerAudit.getAuditId());
    }

}
