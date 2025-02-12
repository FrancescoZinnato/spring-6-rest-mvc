package guru.springframework.spring6restmvc.entities;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@Entity
@Builder
public class BeerOrderShipment {

    public BeerOrderShipment(UUID id, Long version, String trackingNumber, Timestamp createdDate, Timestamp lastModifiedDate, BeerOrder beerOrder) {
        this.id = id;
        this.version = version;
        this.trackingNumber = trackingNumber;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.setBeerOrder(beerOrder);
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false )
    private UUID id;

    @Version
    private Long version;

    private String trackingNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp lastModifiedDate;

    @OneToOne
    private BeerOrder beerOrder;


}
