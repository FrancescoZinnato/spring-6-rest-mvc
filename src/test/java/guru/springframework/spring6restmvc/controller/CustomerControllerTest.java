package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.config.SpringSecConfig;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.services.CustomerService;
import guru.springframework.spring6restmvc.services.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SpringSecConfig.class)
public class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CustomerService customerService;
    @Captor
    ArgumentCaptor<CustomerDTO> customerCaptor;
    @Captor
    ArgumentCaptor<UUID> uuidCaptor;

    @Autowired
    ObjectMapper objectMapper;

    CustomerServiceImpl customerServiceImpl = new CustomerServiceImpl();

    @Test
    void getCustomerByIdNotFound() throws Exception {

        given(customerService.getCustomerById(any(UUID.class))).willThrow(NotFoundException.class);

        mockMvc.perform(get(CustomerController.CUSTOMER_URI_ID, UUID.randomUUID())
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCustomers() throws Exception {
        List<CustomerDTO> customers = customerServiceImpl.getAllCustomers();

        given(customerService.getAllCustomers()).willReturn(customers);

        mockMvc.perform(get(CustomerController.CUSTOMERS_URI)
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(customers.size())));
    }

    @Test
    void testGetCustomerById() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerDTO customer = CustomerDTO.builder().id(customerId).name("TestGetId").build();

        given(customerService.getCustomerById(customerId)).willReturn(Optional.of(customer));

        mockMvc.perform(get(CustomerController.CUSTOMER_URI_ID, customerId)
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(customerId.toString())));
    }

    @Test
    void testPostCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerDTO customer = CustomerDTO.builder().id(customerId).name("TestPost").build();

        given(customerService.saveNewCustomer(any())).willReturn(customer);

        mockMvc.perform(post(CustomerController.CUSTOMERS_URI)
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON) // Specifica il content-type corretto
                .accept(MediaType.APPLICATION_JSON) // Mantiene JSON come formato accettato
                .content(objectMapper.writeValueAsString(customer))) // Passa il JSON come @RequestBody richiesto
                .andExpect(status().isCreated())
                //.andExpect(content().contentType(MediaType.APPLICATION_JSON)) Se la risposta non ha corpo questa riga va rimossa perchè non ha senso testare il content-type
                .andExpect(header().exists("Location"));

    }

    @Test
    void testPutCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerDTO customer = CustomerDTO.builder().id(customerId).name("TestPut").build();

        //doNothing().when(customerService).updateCustomerById(eq(customerId), any());
        // eq viene usato per controllare che il parametro sia esattemente quello passato
        // Inoltre mockito utilizza un sistema di matchers per intercettare le chiamate, quindi se usi almeno 1 matcher (any()), TUTTI gli altri parametri DEVONO essere matchers
        // eq oltre a controllare l'esattezza del parametro lo rende un matcher, quindi va utilizzato per forza se si passa un parametro specifico
        // o utilizzi tutti valori reali, o tutti matchers

        given(customerService.updateCustomerById(any(UUID.class), any(CustomerDTO.class))).willReturn(Optional.of(customer));

        mockMvc.perform(put(CustomerController.CUSTOMER_URI_ID, customerId)
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNoContent());

        verify(customerService).updateCustomerById(uuidCaptor.capture(), customerCaptor.capture());

        assertThat(uuidCaptor.getValue()).isEqualTo(customerId);
        assertThat(customerCaptor.getValue()).isEqualTo(customer);
    }

    @Test
    void testDeleteCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        //Customer customer = Customer.builder().id(customerId).name("TestDelete").build();

        //doNothing().when(customerService).deleteCustomerById(eq(customerId));
        given(customerService.deleteCustomerById(any(UUID.class))).willReturn(true);

        mockMvc.perform(delete(CustomerController.CUSTOMER_URI_ID, customerId)
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomerById(uuidCaptor.capture());

        assertThat(customerId).isEqualTo(uuidCaptor.getValue());
    }

    @Test
    void testPatchCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("name", "Patched Name");

        //doNothing().when(customerService).patchCustomerById(eq(customerId), any());

        mockMvc.perform(patch(CustomerController.CUSTOMER_URI_ID, customerId)
                        .with(httpBasic(BeerControllerTest.USERNAME, BeerControllerTest.PASSWORD))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerMap)))
                .andExpect(status().isNoContent());

        verify(customerService).patchCustomerById(uuidCaptor.capture(), customerCaptor.capture());

        assertThat(customerId).isEqualTo(uuidCaptor.getValue());
        assertThat(customerMap.get("name")).isEqualTo(customerCaptor.getValue().getName());
    }

}
