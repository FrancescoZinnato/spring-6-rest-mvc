package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.services.BeerOrderService;
import guru.springframework.spring6restmvcapi.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerOrderController {

    public final static String BEER_ORDER_PATH = "/api/v1/beerorder";
    public final static String BEER_ORDER_PATH_ID = "/{beerOrderId}";

    private final BeerOrderService beerOrderService;

    @GetMapping(BEER_ORDER_PATH)
    public Page<BeerOrderDTO> listBeerOrders(@RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return beerOrderService.listBeerOrders(pageNumber, pageSize);
    }

    @GetMapping(BEER_ORDER_PATH_ID)
    public BeerOrderDTO getBeerOrderById(@PathVariable("beerOrderId") UUID beerOrderId) {
        return beerOrderService.getBeerOrderById(beerOrderId).orElseThrow(NotFoundException::new);
    }

    @PostMapping(BEER_ORDER_PATH)
    public ResponseEntity<?> createBeerOrder(@Validated @RequestBody BeerOrderCreateDTO beerOrderCreateDTO) {
        BeerOrderDTO savedOrder = beerOrderService.createBeerOrder(beerOrderCreateDTO);

        return ResponseEntity.created(URI.create(BEER_ORDER_PATH + "/" + savedOrder.getId().toString())).build();
    }

    /*
    @RequestMapping(BEER_ORDER_PATH_ID)
    public ResponseEntity<?> updateBeerOrder(@PathVariable("beerOrderId") UUID beerOrderId, @Validated @RequestBody BeerOrderCreateDTO beerOrderCreateDTO) {
        if(beerOrderService.updateBeerOrder(beerOrderId, beerOrderCreateDTO).isEmpty()) {
            throw new NotFoundException();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    */

    @PutMapping(BEER_ORDER_PATH_ID)
    public ResponseEntity<BeerOrderDTO> updateOrder(@PathVariable UUID beerOrderId, @RequestBody BeerOrderUpdateDTO beerOrderUpdateDTO) {
        return ResponseEntity.ok(beerOrderService.updateOrder(beerOrderId, beerOrderUpdateDTO));
    }

    @DeleteMapping(BEER_ORDER_PATH_ID)
    public ResponseEntity<?> deleteOrder(@PathVariable UUID beerOrderId) {
        beerOrderService.deleteBeerOrder(beerOrderId);
        return ResponseEntity.noContent().build();
    }

}
