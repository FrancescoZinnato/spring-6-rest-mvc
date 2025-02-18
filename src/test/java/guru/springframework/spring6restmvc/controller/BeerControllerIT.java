package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerMapper beerMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testListBeerByStyleAndNamePaging() throws Exception {
        mockMvc.perform(get("/api/v1/beer")
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                    .queryParam("beerName", "IPA")
                    .queryParam("beerStyle", BeerStyle.IPA.name())
                    .queryParam("pageNumber", "2")
                    .queryParam("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50))); //11
    }

    @Test
    void testListBeersByStyle() throws Exception {
        mockMvc.perform(get("/api/v1/beer")
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("pageSize", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(548)));
    }

    @Test
    void testListBeersByName() throws Exception {
        mockMvc.perform(get("/api/v1/beer")
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .queryParam("beerName", "IPA")
                .queryParam("pageSize", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(336)));
    }

    @Test
    void testPatchBeerBadName() throws Exception {
        Beer beer = beerRepository.findAll().getFirst();

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "Out of bound name too longgggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");

        MvcResult result = mockMvc.perform(patch("/api/v1/beer/" + beer.getId())
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());

    }

    @Test
    void testDeleteNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteBeer() {
        Beer beer = beerRepository.findAll().getFirst();

        ResponseEntity<?> responseEntity = beerController.deleteById(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(beerRepository.findById(beer.getId())).isEmpty();
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build()));
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateBeer() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "Updated Beer";
        beerDTO.setBeerName(beerName);

        ResponseEntity<?> responseEntity = beerController.updateById(beer.getId(), beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = beerRepository.findById(beer.getId()).get();

        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    void testListBeers() {
        Page<BeerDTO> list = beerController.listBeers(null, null, 1, 2413);

        assertThat(list.getContent().size()).isEqualTo(1000); //2413 se carichi il csv delle birre //1000 ritorno massimo impostato della page
    }

    @Rollback // We want to tell Spring to roll back the persistence operations done in this test to avoid errors on others
    @Transactional
    @Test
    void testEmptyListBeers() {
        beerRepository.deleteAll();
        Page<BeerDTO> list = beerController.listBeers(null, null, 1, 25);

        assertThat(list.getContent().size()).isEqualTo(0);
    }

    @Test
    void testGetBeerById() {
        Beer beer = beerRepository.findAll().getFirst();

        BeerDTO dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(beer.getId());
    }

    @Test
    void testBeerIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.getBeerById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testSaveBeer() {
        BeerDTO beerDTO = BeerDTO.builder().beerName("Test Save Beer").build();

        ResponseEntity<?> responseEntity = beerController.handlePost(beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[locationUUID.length - 1]); // Dall'ultimo slash

        Beer beer = beerRepository.findById(savedUUID).get();
        assertThat(beer).isNotNull();

    }

}