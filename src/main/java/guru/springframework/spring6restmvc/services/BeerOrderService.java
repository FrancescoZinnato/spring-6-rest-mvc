package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface BeerOrderService {

    Page<BeerOrderDTO> listBeerOrders(Integer pageNumber, Integer pageSize);

    Optional<BeerOrderDTO> getBeerOrderById(UUID id);

    BeerOrderDTO createBeerOrder(BeerOrderCreateDTO beerOrderCreateDTO);

    Optional<BeerOrderDTO> updateBeerOrder(UUID beerOrderId, BeerOrderCreateDTO beerOrderCreateDTO); //Non utilizzato

    BeerOrderDTO updateOrder(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO);

}
