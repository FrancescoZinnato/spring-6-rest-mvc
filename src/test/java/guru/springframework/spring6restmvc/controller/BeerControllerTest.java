package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@SpringBootTest
@WebMvcTest(BeerController.class)
class BeerControllerTest {

    //@Autowired
    //BeerController beerController;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Captor
    ArgumentCaptor<UUID> uuidCaptor;
    @Captor
    ArgumentCaptor<Beer> beerCaptor;

    @MockitoBean //Tells Mockito to provide a "mock" of this in the Spring Context, return null by default
    BeerService beerService;

    BeerServiceImpl beerServiceImpl = new BeerServiceImpl();


    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/beer/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        //Beer testBeer = beerServiceImpl.listBeers().get(0);
        UUID beerId = UUID.randomUUID();
        Beer testBeer = Beer.builder()
                .id(beerId)
                .beerName("Test Beer")
                .beerStyle(BeerStyle.IPA)
                .price(BigDecimal.valueOf(5.99))
                .build();

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(get("/api/v1/beer/" + beerId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(beerId.toString()))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
        //System.out.println(beerController.getBeerById(UUID.randomUUID()));
    }

    @Test
    void testListBeers() throws Exception {
        List<Beer> testBeers = beerServiceImpl.listBeers();

        given(beerService.listBeers()).willReturn(testBeers);

        mockMvc.perform(get("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(testBeers.size())));
    }

    @Test
    void testCreateBeer() throws Exception {
        UUID beerId = UUID.randomUUID();
        Beer testBeer = Beer.builder().id(beerId).beerName("TestPost").beerStyle(BeerStyle.IPA).build();

        given(beerService.saveNewBeer(any(Beer.class))).willReturn(testBeer);

        mockMvc.perform(post("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testUpdateBeer() throws Exception {
        UUID beerId = UUID.randomUUID();
        Beer testBeer = Beer.builder().id(beerId).build();

        //doNothing().when(beerService).updateBeerById(eq(beerId), any(Beer.class));

        mockMvc.perform(put("/api/v1/beer/" + beerId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testBeer)))
        .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(uuidCaptor.capture(), beerCaptor.capture()); // Verifica che sia stato chiamato il metodo

        assertThat(uuidCaptor.getValue()).isEqualTo(beerId);
        assertThat(beerCaptor.getValue()).isEqualTo(testBeer);
    }

    @Test
    void testDeleteBeer() throws Exception {
        UUID beerId = UUID.randomUUID();
        Beer testBeer = Beer.builder().id(beerId).build();

        doNothing().when(beerService).deleteById(eq(beerId));

        mockMvc.perform(delete("/api/v1/beer/" + beerId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

        verify(beerService).deleteById(uuidCaptor.capture());

        assertThat(beerId).isEqualTo(uuidCaptor.getValue()); // L'argumentcaptor prende l'id passato come pathvariable controlla che sia stato parsato correttamente e poi controllo se gli id corrispondono
    }

    @Test
    void testPatchBeer() throws Exception {
        UUID beerId = UUID.randomUUID();

        // Utilizzare una map simula il funzionamento del Patch, che invia solo i campi da modificare, quindi la map rappresenta un payload parziale, perfetto per il patching
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "Patched Name");

        mockMvc.perform(patch("/api/v1/beer/" + beerId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        // Anche se sto passando una map come corpo della risposta, dato che nel metodo patch accetto @RequestBody Beer, il sistema di deserializzazione di Spring trasformerà
        // automaticamente il JSON (la map) in un oggetto Beer, quindi anche passando una map, il beerCaptor catturerà un oggetto Beer creato dalla deserializzazione
        verify(beerService).patchBeerById(uuidCaptor.capture(), beerCaptor.capture());

        assertThat(beerId).isEqualTo(uuidCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerCaptor.getValue().getBeerName());
    }

}