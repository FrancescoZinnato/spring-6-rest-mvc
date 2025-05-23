package guru.springframework.spring6restmvc.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Data
public class BeerOrderDTO {

    private UUID id;

    private Long version;

    private Timestamp createdDate;

    private Timestamp lastModifiedDate;

    private String customerRef;

    private BigDecimal paymentAmount;

    private CustomerDTO customer;

    private Set<BeerOrderLineDTO> beerOrderLines;

    private BeerOrderShipmentDTO beerOrderShipment;

}
