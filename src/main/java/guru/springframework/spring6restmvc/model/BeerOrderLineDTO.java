package guru.springframework.spring6restmvc.model;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class BeerOrderLineDTO {

    private UUID id;

    private Long version;

    private Timestamp createdDate;

    private Timestamp lastModifiedDate;

    @Min(value = 1, message = "orderQuantity must be greater than 0")
    private Integer orderQuantity;

    private Integer quantityAllocated;

    //private BeerOrderDTO beerOrder; Non serve?

    private BeerDTO beer;

}
