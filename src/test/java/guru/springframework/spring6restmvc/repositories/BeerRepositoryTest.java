package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.bootstrap.BootstrapData;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerCsvServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({BootstrapData.class, BeerCsvServiceImpl.class})
@DataJpaTest // Controllers and components will not be imported, simply a JPA test env
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testGetBeerListByName() {
        Page<Beer> list = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%", null); // Ogni nome che contiene IPA nel nome

        assertThat(list.getContent().size()).isEqualTo(336);
    }

    @Test
    void testGetBeerListByStyle() {
        Page<Beer> list = beerRepository.findAllByBeerStyle(BeerStyle.IPA, null);

        assertThat(list.getContent().size()).isEqualTo(548);
    }

    @Test
    void testGetBeerListByStyleAndName() {
        Page<Beer> list = beerRepository.findAllByBeerStyleAndBeerNameIsLikeIgnoreCase(BeerStyle.IPA, "%IPA%", null);

        assertThat(list.getContent().size()).isEqualTo(310);
    }

    @Test
    void testSaveBeerNameTooLong() {

        assertThrows(ConstraintViolationException.class, () -> { // Avendo aggiunto l'annotazione @Size(max=50) ora ottengo questa eccezione, solo con @Column(length=50) invece otterrei una DataIntegrityViolationException
            beerRepository.save(Beer.builder()
                    .beerName("Saved Beer too longgggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg")
                    .beerStyle(BeerStyle.IPA)
                    .upc("2314123")
                    .price(BigDecimal.valueOf(4.99))
                    .build());

            beerRepository.flush();
        });

    }

    @Test
    void testSaveBeer() {
        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("Saved Beer")
                .beerStyle(BeerStyle.IPA)
                .upc("2314123")
                .price(BigDecimal.valueOf(4.99))
                .build());

        beerRepository.flush(); // Esegue l'operazione dopo aver creato correttamente l'oggetto, sennò fa troppo veloce e il test passa anche con dati errati

        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }

}