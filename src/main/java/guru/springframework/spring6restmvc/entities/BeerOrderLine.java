package guru.springframework.spring6restmvc.entities;

import guru.springframework.spring6restmvcapi.model.BeerOrderLineStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@Entity
@Builder
public class BeerOrderLine {

    public BeerOrderLine(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, Integer orderQuantity, Integer quantityAllocated, BeerOrder beerOrder, Beer beer, BeerOrderLineStatus beerOrderLineStatus) {
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.orderQuantity = orderQuantity;
        this.quantityAllocated = quantityAllocated;
        this.setBeerOrder(beerOrder);
        this.setBeer(beer);
        this.setOrderLineStatus(beerOrderLineStatus);
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false )
    private UUID id;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp lastModifiedDate;

    public boolean isNew() {
        return this.id == null;
    }

    @Min(value = 1, message = "orderQuantity must be greater than 0")
    private Integer orderQuantity = 1;

    private Integer quantityAllocated = 0;

    @ManyToOne
    private BeerOrder beerOrder;

    public void setBeerOrder(BeerOrder beerOrder) {
        if (beerOrder != null) {
            this.beerOrder = beerOrder;
            beerOrder.getBeerOrderLines().add(this);
        }
    }

    @ManyToOne
    private Beer beer;

    // Lo mettiamo di Default su NEW e lo salviamo come String, anche se, su grandi applicazioni, si usano i numeri per salvare spazio negli enum
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private BeerOrderLineStatus orderLineStatus = BeerOrderLineStatus.NEW;

    public void setBeer(Beer beer) {
        if (beer != null) {
            this.beer = beer;
            beer.getBeerOrderLines().add(this);
        }
    }

}