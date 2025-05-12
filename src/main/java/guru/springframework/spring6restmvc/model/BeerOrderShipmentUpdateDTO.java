package guru.springframework.spring6restmvc.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeerOrderShipmentUpdateDTO {

    @NotBlank
    private String trackingNumber;

}
