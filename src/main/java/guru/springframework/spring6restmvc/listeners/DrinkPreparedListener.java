package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.config.KafkaConfig;
import guru.springframework.spring6restmvc.repositories.BeerOrderLineRepository;
import guru.springframework.spring6restmvcapi.events.DrinkPreparedEvent;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrinkPreparedListener {

    private final BeerOrderLineRepository beerOrderLineRepository;

    @KafkaListener(groupId = "DrinkPreparedEvent", topics = KafkaConfig.DRINK_PREPARED_TOPIC)
    public void listen(DrinkPreparedEvent drinkPreparedEvent) {

        beerOrderLineRepository.findById(drinkPreparedEvent.getBeerOrderLine().getId()).ifPresentOrElse(beerOrderLine -> {
            beerOrderLine.setOrderLineStatus(BeerOrderLineStatus.COMPLETE);
            beerOrderLineRepository.save(beerOrderLine);
        }, () -> log.error("Beer order line not found in MVC DrinkPreparedListener"));

    }

}
