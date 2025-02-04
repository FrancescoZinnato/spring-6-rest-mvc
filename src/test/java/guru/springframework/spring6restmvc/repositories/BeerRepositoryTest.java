package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest // Controllers and components will not be imported, simply a JPA test env
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

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