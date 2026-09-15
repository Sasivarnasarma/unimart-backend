package varna.mit.kln.unimart.order.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.repository.ListingRepository;
import varna.mit.kln.unimart.order.dto.OrderRequestDto;
import varna.mit.kln.unimart.order.dto.OrderResponseDto;
import varna.mit.kln.unimart.order.dto.OrderStatusUpdateDto;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ListingRepository listingRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userEmail) {
        User buyer = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Listing listing = listingRepository.findById(requestDto.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + requestDto.getListingId()));

        if (listing.getStatus() != ListingStatus.available) {
            throw new IllegalArgumentException("Listing is not available for purchase");
        }

        if (listing.getSeller() != null && listing.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("You cannot purchase your own listing");
        }

        Order order = new Order(
                listing,
                buyer,
                listing.getPrice(),
                requestDto.getPaymentMethod() != null ? requestDto.getPaymentMethod() : "CARD"
        );
        order.setStatus(OrderStatus.pending);

        Order savedOrder = orderRepository.save(order);
        return new OrderResponseDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrders(String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Order> orders = orderRepository.findByBuyerIdOrListingSellerIdOrderByCreatedAtDesc(user.getId(), user.getId());
        return orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Integer id, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = order.getListing() != null && order.getListing().getSeller() != null
                && order.getListing().getSeller().getId().equals(user.getId());

        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("You are not authorized to view this order");
        }

        return new OrderResponseDto(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Integer id, OrderStatusUpdateDto statusUpdateDto, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = order.getListing() != null && order.getListing().getSeller() != null
                && order.getListing().getSeller().getId().equals(user.getId());

        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("You are not authorized to update this order");
        }

        OrderStatus newStatus = statusUpdateDto.getStatus();
        order.setStatus(newStatus);

        Listing listing = order.getListing();
        if (listing != null) {
            if (newStatus == OrderStatus.completed) {
                listing.setStatus(ListingStatus.sold);
                listingRepository.save(listing);
            } else if (newStatus == OrderStatus.cancelled) {
                listing.setStatus(ListingStatus.available);
                listingRepository.save(listing);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return new OrderResponseDto(updatedOrder);
    }
}
