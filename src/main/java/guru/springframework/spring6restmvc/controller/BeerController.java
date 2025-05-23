package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvcapi.model.BeerDTO;
import guru.springframework.spring6restmvcapi.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/beer")
public class BeerController {
    private final BeerService beerService;

    /**
     * PATCH /api/v1/beer/{beerId}: patch element by ID
     * @param beerId element that will be patched ID's
     * @param beer element's dto with patched data
     * @return status code 204 (NO CONTENT)
     */
    @PatchMapping("/{beerId}")
    public ResponseEntity<?> updateBeerPatchById(@PathVariable("beerId")UUID beerId, @RequestBody BeerDTO beer){

        beerService.patchBeerById(beerId, beer);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * DELETE /api/v1/beer/{beerId}: delete element by ID
     * @param beerId element that will be deleted ID's
     * @return status code 204 (NO CONTENT)
     */
    @DeleteMapping("/{beerId}")
    public ResponseEntity<?> deleteById(@PathVariable("beerId") UUID beerId){

        if(!beerService.deleteById(beerId)) {
            throw new NotFoundException();
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * PUT /api/v1/beer/{beerId}: put element by ID
     * @param beerId element that will be replaced ID's
     * @param beer (validated) element's dto with updated data
     * @return status code 204 (NO CONTENT)
     */
    @PutMapping("/{beerId}")
    public ResponseEntity<?> updateById(@PathVariable("beerId")UUID beerId, @Validated @RequestBody BeerDTO beer){

        if(beerService.updateBeerById(beerId, beer).isEmpty()) {
            throw new NotFoundException();
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * POST /api/v1/beer: put element by ID
     * @param beer (validated) element's dto to upload
     * @return status code 201 (CREATED)
     */
    @PostMapping()
    public ResponseEntity<?> handlePost(@Validated @RequestBody BeerDTO beer){

        BeerDTO savedBeer = beerService.saveNewBeer(beer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/beer/" + savedBeer.getId().toString());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/beer: get all elements
     * @param beerName (optional) filter research by names that include {@code beerName}
     * @param beerStyle (optional) filter research by {@link guru.springframework.spring6restmvcapi.model.BeerStyle}
     * @param pageNumber (optional) max number of pages returned
     * @param pageSize (optional) max number of elements displayed by each page
     * @return {@link Page}<{@link BeerDTO}>
     */
    @GetMapping()
    public Page<BeerDTO> listBeers(@RequestParam(required = false) String beerName, @RequestParam(required = false) BeerStyle beerStyle,
                                   @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return beerService.listBeers(beerName, beerStyle, pageNumber, pageSize);
    }

    /**
     * GET /api/v1/beer/{beerId}: get the element by ID
     * @param beerId element to get ID's
     * @return {@link BeerDTO}
     * @throws NotFoundException
     *         if the {@code beerId} doesn't exist in the datasource
     */
    @GetMapping("/{beerId}")
    public BeerDTO getBeerById(@PathVariable("beerId") UUID beerId){

        log.debug("Get Beer by Id - in controller asdasdsadasda");

        return beerService.getBeerById(beerId).orElseThrow(NotFoundException::new);
        // Possibile perchè ora restituisco un Optional<Beer> dal service, quindi se Beer esiste bene, sennò lancia una NotFoundException
    }

}
