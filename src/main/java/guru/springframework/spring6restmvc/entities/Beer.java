package guru.springframework.spring6restmvc.entities;

import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Beer {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(length = 36, columnDefinition = "varchar", unique = true, nullable = false, updatable = false)
    private UUID id;
    @Version
    private Integer version;
    @NotBlank
    @NotNull
    @Size(max = 50) // Bean validation constraint per evitare che arrivino dati invalidi al DB, quindi previene l'invio di un'eccezione dal database
    @Column(length = 50)
    private String beerName;
    @NotNull
    private BeerStyle beerStyle;
    @NotBlank
    @NotNull
    @Size(max = 255)
    private String upc;
    private Integer quantityOnHand;
    @NotNull
    private BigDecimal price;
    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
}
