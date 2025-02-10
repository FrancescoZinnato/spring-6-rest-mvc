package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers(String beerName, BeerStyle beerStyle) {
        if(StringUtils.hasText(beerName) && beerStyle == null) {
            return beerMapper.beersToBeerDTOs(listBeersByName(beerName));
        }

        if(!StringUtils.hasText(beerName) && beerStyle != null) {
            return beerMapper.beersToBeerDTOs(listBeersByStyle(beerStyle));
        }

        //return beerRepository.findAll().stream().map(beerMapper::beerToBeerDTO).collect(Collectors.toList());
        return beerMapper.beersToBeerDTOs(beerRepository.findAll()); // Utilizza la capacità di MapStruct di mappare le liste (più veloce di .stream)
    }

    private List<Beer> listBeersByName(String beerName) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%"); // Per usare SQL Wildcards
    }

    private List<Beer> listBeersByStyle(BeerStyle beerStyle) {
        return beerRepository.findAllByBeerStyle(beerStyle);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        //return Optional.ofNullable(beerMapper.beerToBeerDTO(beerRepository.findById(id).orElse(null)));
        return beerRepository.findById(id).map(beerMapper::beerToBeerDTO); // Evita null, l'optional sarà già vuoto - Segue il paradigma di Optional
        //return beerMapper.beerToBeerDTO(beerRepository.findById(id)); // Soluzione se implementi il ritorno di Optional direttamente nel Mapper
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        //return beerMapper.beerToBeerDTO(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
        Beer beerToSave = beerMapper.beerDtoToBeer(beer); // Stessa cosa ma ogni step separato per rendere più leggibile e manutenibile
        Beer savedBeer = beerRepository.save(beerToSave);
        return beerMapper.beerToBeerDTO(savedBeer);
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        /*
        Un'AtomicReference<T> è una classe di Java Concurrency che fornisce un wrapper thread-safe attorno a un oggetto di tipo T.
        Il suo scopo principale è consentire operazioni atomiche su un riferimento, senza bisogno di sincronizzazione esplicita.
         */
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        /*
        Le variabili locali in Java devono essere effettivamente finali nelle lambda.
        Poiché dentro la lambda vogliamo aggiornare un valore fuori dal suo scope, l'uso di AtomicReference permette di aggirare questo limite.
         */
        beerRepository.findById(beerId).ifPresentOrElse( foundBeer -> {
            foundBeer.setBeerName(beer.getBeerName());
            foundBeer.setBeerStyle(beer.getBeerStyle());
            foundBeer.setUpc(beer.getUpc());
            foundBeer.setPrice(beer.getPrice());
            atomicReference.set(Optional.of(beerMapper.beerToBeerDTO(beerRepository.save(foundBeer))));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }

    @Override
    public Boolean deleteById(UUID beerId) {
        if (beerRepository.existsById(beerId)) {
            beerRepository.deleteById(beerId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if(StringUtils.hasText(beer.getBeerName())) {
                foundBeer.setBeerName(beer.getBeerName());
            }
            if(StringUtils.hasText(beer.getUpc())) {
                foundBeer.setUpc(beer.getUpc());
            }
            if(beer.getPrice() != null) {
                foundBeer.setPrice(beer.getPrice());
            }
            if(beer.getBeerStyle() != null) {
                foundBeer.setBeerStyle(beer.getBeerStyle());
            }
            if(beer.getQuantityOnHand() != null) {
                foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            }
            atomicReference.set(Optional.of(beerMapper.beerToBeerDTO(beerRepository.save(foundBeer))));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }
}
