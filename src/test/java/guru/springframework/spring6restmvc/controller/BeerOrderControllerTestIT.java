package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.model.*;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static guru.springframework.spring6restmvc.controller.BeerControllerTest.jwtRequestPostProcessor;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RecordApplicationEvents
@SpringBootTest
class BeerOrderControllerTestIT {

    MockMvc mockMvc;
    
    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    BeerOrderRepository beerOrderRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    BeerRepository beerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testDeleteOrder() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().getFirst();

        mockMvc.perform(delete(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                .with(jwtRequestPostProcessor))
                .andExpect(status().isNoContent());

        assertTrue(beerOrderRepository.findById(beerOrder.getId()).isEmpty());

        mockMvc.perform(delete(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateBeerOrder() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().getFirst();
        Beer beer = beerRepository.findAll().getFirst();
        Customer customer = customerRepository.findAll().getFirst();

        BeerOrderUpdateDTO beerOrderUpdateDTO = BeerOrderUpdateDTO.builder()
                .orderId(beerOrder.getId())
                .customerRef(beerOrder.getCustomerRef())
                .customerId(customer.getId())
                .beerOrderLines(Set.of(BeerOrderLineUpdateDTO.builder()
                        .beerId(beer.getId())
                        .orderQuantity(1)
                        .build()))
                .build();

        mockMvc.perform(put(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(beerOrderUpdateDTO))
                .with(jwtRequestPostProcessor))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    @Rollback
    // Remember to always annotate with Transactional all methods that execute operations on the database to avoid LazyInitializationException by having the same Hibernate session during the Transaction
    void testUpdateOrder() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().getFirst();

        Set<BeerOrderLineUpdateDTO> lines = new HashSet<>();

        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> lines.add(BeerOrderLineUpdateDTO.builder()
                .id(beerOrderLine.getId())
                .beerId(beerOrderLine.getBeer().getId())
                .orderQuantity(beerOrderLine.getOrderQuantity())
                .quantityAllocated(beerOrderLine.getQuantityAllocated())
                .build()));

        BeerOrderUpdateDTO beerOrderUpdateDTO = BeerOrderUpdateDTO.builder()
                .customerId(beerOrder.getCustomer().getId())
                .customerRef("TestRef")
                .beerOrderLines(lines)
                .beerOrderShipment(BeerOrderShipmentUpdateDTO.builder()
                        .trackingNumber("123456")
                        .build())
                .build();

        mockMvc.perform(put(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(beerOrderUpdateDTO))
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRef", is("TestRef")));
    }

    @Test
    @Transactional
    @Rollback
    void testCreateBeerOrder() throws Exception {
        Beer beer = beerRepository.findAll().getFirst();
        Customer customer = customerRepository.findAll().getFirst();

        BeerOrderCreateDTO beerOrderCreateDTO = BeerOrderCreateDTO.builder()
                .customerId(customer.getId())
                .beerOrderLines(Set.of(BeerOrderLineCreateDTO.builder()
                                .beerId(beer.getId())
                                .orderQuantity(1)
                                .build()))
                .build();

        mockMvc.perform(post(BeerOrderController.BEER_ORDER_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(beerOrderCreateDTO))
                .with(jwtRequestPostProcessor))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }

    @Test
    void testListBeerOrders() throws Exception {
        mockMvc.perform(get(BeerOrderController.BEER_ORDER_PATH)
                .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", greaterThan(0)));
    }

    @Test
    void testGetBeerOrderById() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().getFirst();

        mockMvc.perform(get(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(beerOrder.getId().toString())));
    }

}