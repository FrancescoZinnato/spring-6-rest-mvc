package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.config.SpringSecConfig;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
//@Import(SpringSecConfig.class) // Se importo questa classe di securityConfig per far passare anche le richieste non GET, si rompe tutto
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
    ArgumentCaptor<BeerDTO> beerCaptor;

    @MockitoBean //Tells Mockito to provide a "mock" of this in the Spring Context, return null by default
    BeerService beerService;

    BeerServiceImpl beerServiceImpl;

    public static final String USERNAME = "user1";
    public static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/beer/" + UUID.randomUUID())
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        //Beer testBeer = beerServiceImpl.listBeers().get(0);
        UUID beerId = UUID.randomUUID();
        BeerDTO testBeer = BeerDTO.builder()
                .id(beerId)
                .beerName("Test Beer")
                .beerStyle(BeerStyle.IPA)
                .price(BigDecimal.valueOf(5.99))
                .build();

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(get("/api/v1/beer/" + beerId)
                        .with(httpBasic(USERNAME, PASSWORD))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(beerId.toString()))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
        //System.out.println(beerController.getBeerById(UUID.randomUUID()));
    }

    @Test
    void testListBeers() throws Exception {
        Page<BeerDTO> testBeers = beerServiceImpl.listBeers(null, null, null, null);

        given(beerService.listBeers(any(), any(), any(), any())).willReturn(testBeers);

        mockMvc.perform(get("/api/v1/beer")
                        .with(httpBasic(USERNAME, PASSWORD))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(3)));
    }

    @Test
    void testCreateBeerNullName() throws Exception {
        BeerDTO beerDTO = BeerDTO.builder().build();

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, 1, 25).getContent().getFirst());

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/beer")
                        .with(httpBasic(USERNAME, PASSWORD))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6))) // Mi aspetto 6 errori di validazione
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testCreateBeer() throws Exception {
        UUID beerId = UUID.randomUUID();
        BeerDTO testBeer = BeerDTO.builder().id(beerId).beerName("TestPost")
                .price(BigDecimal.valueOf(4.99)).upc("upc").beerStyle(BeerStyle.IPA).build();

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(testBeer);

        mockMvc.perform(post("/api/v1/beer")
                        .with(httpBasic(USERNAME, PASSWORD))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testUpdateBeer() throws Exception {
        UUID beerId = UUID.randomUUID();
        BeerDTO testBeer = beerServiceImpl.listBeers(null, null, 1, 25).getContent().getFirst();

        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(testBeer));
        //doNothing().when(beerService).updateBeerById(eq(beerId), any(Beer.class));

        mockMvc.perform(put("/api/v1/beer/" + beerId)
                        .with(httpBasic(USERNAME, PASSWORD))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testBeer)))
        .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(uuidCaptor.capture(), beerCaptor.capture()); // Verifica che sia stato chiamato il metodo

        assertThat(uuidCaptor.getValue()).isEqualTo(beerId);
        assertThat(beerCaptor.getValue()).isEqualTo(testBeer);
    }

    @Test
    void testUpdateBeerNullName() throws Exception {
        UUID beerId = UUID.randomUUID();
        BeerDTO testBeer = beerServiceImpl.listBeers(null, null, 1, 25).getContent().getFirst();
        testBeer.setBeerName("");

        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(testBeer));
        //doNothing().when(beerService).updateBeerById(eq(beerId), any(Beer.class));

        mockMvc.perform(put("/api/v1/beer/" + beerId)
                                        .with(httpBasic(USERNAME, PASSWORD))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void testDeleteBeer() throws Exception {
        UUID beerId = UUID.randomUUID();
        //BeerDTO testBeer = BeerDTO.builder().id(beerId).build();

        given(beerService.deleteById(any(UUID.class))).willReturn(true);
        //doNothing().when(beerService).deleteById(eq(beerId));

        mockMvc.perform(delete("/api/v1/beer/" + beerId)
                        .with(httpBasic(USERNAME, PASSWORD))
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
                        .with(httpBasic(USERNAME, PASSWORD))
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