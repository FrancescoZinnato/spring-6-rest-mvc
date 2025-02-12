package guru.springframework.spring6restmvc.entities;

import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
    @JdbcTypeCode(SqlTypes.CHAR) // Sennò MySQL prova ad inserirlo come binary
    @Column(length = 36, columnDefinition = "varchar(36)", unique = true, nullable = false, updatable = false)
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
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updateDate;

    @Builder.Default
    @OneToMany(mappedBy = "beer")
    private Set<BeerOrderLine> beerOrderLines = new HashSet<>();
}
