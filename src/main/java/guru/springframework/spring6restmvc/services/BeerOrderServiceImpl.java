package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.controller.NotFoundException;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderLine;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring6restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderUpdateDTO;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderServiceImpl implements BeerOrderService {

    private final BeerOrderMapper beerOrderMapper;
    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerRepository beerRepository;

    private final static int DEFAULT_PAGE = 0;
    private final static int DEFAULT_PAGE_SIZE = 25;

    @Override //Non utilizzato
    public Optional<BeerOrderDTO> updateBeerOrder(UUID beerOrderId, BeerOrderCreateDTO beerOrderCreateDTO) {
        Customer customer = customerRepository.findById(beerOrderCreateDTO.getCustomerId()).orElseThrow(NotFoundException::new);
        Set<BeerOrderLine> beerOrderLines = new HashSet<>();

        beerOrderCreateDTO.getBeerOrderLines().forEach(beerOrderLine -> beerOrderLines.add(BeerOrderLine.builder()
                .beer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new))
                .orderQuantity(beerOrderLine.getOrderQuantity())
                .build()));

        AtomicReference<Optional<BeerOrderDTO>> atomicReference = new AtomicReference<>();

        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(foundOrder -> {
            foundOrder.setCustomer(customer);
            foundOrder.setBeerOrderLines(beerOrderLines);
            foundOrder.setCustomerRef(beerOrderCreateDTO.getCustomerRef());
            atomicReference.set(Optional.of(beerOrderMapper.beerOrderToBeerOrderDTO(beerOrderRepository.save(foundOrder))));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }

    @Override
    public BeerOrderDTO updateOrder(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO) {
        BeerOrder order = beerOrderRepository.findById(beerOrderId).orElseThrow(NotFoundException::new);

        order.setCustomer(customerRepository.findById(beerOrderUpdateDTO.getCustomerId()).orElseThrow(NotFoundException::new));
        order.setCustomerRef(beerOrderUpdateDTO.getCustomerRef());

        beerOrderUpdateDTO.getBeerOrderLines().forEach(beerOrderLine -> {

            if (beerOrderLine.getBeerId() != null) {
                BeerOrderLine foundLine = order.getBeerOrderLines().stream()
                        .filter(beerOrderLine1 -> beerOrderLine1.getId().equals(beerOrderLine.getId()))
                        .findFirst().orElseThrow(NotFoundException::new);
                foundLine.setBeer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new));
                foundLine.setOrderQuantity(beerOrderLine.getOrderQuantity());
                foundLine.setQuantityAllocated(beerOrderLine.getQuantityAllocated());
            } else {
                order.getBeerOrderLines().add(BeerOrderLine.builder()
                        .beer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new))
                        .orderQuantity(beerOrderLine.getOrderQuantity())
                        .quantityAllocated(beerOrderLine.getQuantityAllocated())
                        .build());
            }
        });

        if (beerOrderUpdateDTO.getBeerOrderShipment() != null && beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber() != null) {
            if (order.getBeerOrderShipment() == null) {
                order.setBeerOrderShipment(BeerOrderShipment.builder().trackingNumber(beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber()).build());
            } else {
                order.getBeerOrderShipment().setTrackingNumber(beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber());
            }
        }

        return beerOrderMapper.beerOrderToBeerOrderDTO(beerOrderRepository.save(order));
    }

    @Cacheable(cacheNames = "beerOrderListCache")
    @Override
    public Page<BeerOrderDTO> listBeerOrders(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        return beerOrderRepository.findAll(pageRequest).map(beerOrderMapper::beerOrderToBeerOrderDTO);
    }

    @Cacheable(cacheNames = "beerOrderCache", key = "#id")
    @Override
    public Optional<BeerOrderDTO> getBeerOrderById(UUID id) {
        return Optional.of(beerOrderRepository.findById(id).map(beerOrderMapper::beerOrderToBeerOrderDTO)).orElse(null);
    }

    @Override
    public BeerOrderDTO createBeerOrder(BeerOrderCreateDTO beerOrderCreateDTO) {
        /*
        * Nel beerOrderCreateDTO ho solo l'id di riferimento del Customer, ed i suoi orderLines sono fatti da BeerOrderLineCreateDTO che hanno solo l'id di riferimento alla birra
        * Quindi devo trovare il Customer utilizzando il suo id nel beerOrderCreateDTO, e devo rifare il Set di BeerOrderLine
        * iterando sul set in beerOrderCreateDTO, e per ogni elemento, utilizzare il beerId salvato per recuperare la Beer
        * in modo da poter creare il BeerOrderLine con l'intero oggetto Beer
        */
        Customer customer = customerRepository.findById(beerOrderCreateDTO.getCustomerId()).orElseThrow(NotFoundException::new);
        Set<BeerOrderLine> beerOrderLines = new HashSet<>();

        beerOrderCreateDTO.getBeerOrderLines().forEach(beerOrderLine -> beerOrderLines.add(BeerOrderLine.builder()
                        .beer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new))
                        .orderQuantity(beerOrderLine.getOrderQuantity())
                .build()));

        return beerOrderMapper.beerOrderToBeerOrderDTO(
                beerOrderRepository.save(BeerOrder.builder()
                        .customer(customer)
                        .beerOrderLines(beerOrderLines)
                        .customerRef(beerOrderCreateDTO.getCustomerRef())
                        .build()
                )
        );
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if(pageNumber != null && pageNumber > 0) {
            queryPageNumber = pageNumber;
        } else {
            queryPageNumber = DEFAULT_PAGE;
        }

        if(pageSize != null && pageSize > 0) {
            queryPageSize = pageSize;
        } else {
            queryPageSize = DEFAULT_PAGE_SIZE;
        }

        if(queryPageSize > 1000) {
            queryPageSize = 1000;
        }

        Sort sort = Sort.by(Sort.Order.asc("id"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }
}
