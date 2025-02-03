package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers() {
        //return beerRepository.findAll().stream().map(beerMapper::beerToBeerDTO).collect(Collectors.toList());
        return beerMapper.beersToBeerDTOs(beerRepository.findAll()); // Utilizza la capacità di MapStruct di mappare le liste (più veloce di .stream)
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        //return Optional.ofNullable(beerMapper.beerToBeerDTO(beerRepository.findById(id).orElse(null)));
        return beerRepository.findById(id).map(beerMapper::beerToBeerDTO); // Evita null, l'optional sarà già vuoto - Segue il paradigma di Optional
        //return beerMapper.beerToBeerDTO(beerRepository.findById(id)); // Soluzione se implementi il ritorno di Optional direttamente nel Mapper
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        return null;
    }

    @Override
    public void updateBeerById(UUID beerId, BeerDTO beer) {

    }

    @Override
    public void deleteById(UUID beerId) {

    }

    @Override
    public void patchBeerById(UUID beerId, BeerDTO beer) {

    }
}
