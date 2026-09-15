package varna.mit.kln.unimart.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.repository.ListingRepository;
import varna.mit.kln.unimart.order.dto.OrderRequestDto;
import varna.mit.kln.unimart.order.dto.OrderResponseDto;
import varna.mit.kln.unimart.order.dto.OrderStatusUpdateDto;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    private OrderService orderService;

    private User buyer;
    private User seller;
    private Listing listing;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, listingRepository, userRepository);

        buyer = new User();
        buyer.setId(1);
        buyer.setUniversityEmail("order_buyer@kln.ac.lk");
        buyer.setFullName("Order Buyer");

        seller = new User();
        seller.setId(2);
        seller.setUniversityEmail("order_seller@kln.ac.lk");
        seller.setFullName("Order Seller");

        listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setTitle("Laptop Stand");
        listing.setPrice(new BigDecimal("45.00"));
        listing.setStatus(ListingStatus.available);
    }

    @Test
    void createOrder_Success() {
        OrderRequestDto request = new OrderRequestDto(10, "CARD");

        when(userRepository.findByUniversityEmail("order_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(listingRepository.findById(10)).thenReturn(Optional.of(listing));

        Order savedOrder = new Order(listing, buyer, new BigDecimal("45.00"), "CARD");
        savedOrder.setId(100);
        savedOrder.setStatus(OrderStatus.pending);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDto response = orderService.createOrder(request, "order_buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals(10, response.getListingId());
        assertEquals(1, response.getBuyerId());
        assertEquals(2, response.getSellerId());
        assertEquals(OrderStatus.pending, response.getStatus());
    }

    @Test
    void createOrder_ListingNotAvailable_ThrowsIllegalArgumentException() {
        listing.setStatus(ListingStatus.sold);
        OrderRequestDto request = new OrderRequestDto(10, "CARD");

        when(userRepository.findByUniversityEmail("order_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(listingRepository.findById(10)).thenReturn(Optional.of(listing));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request, "order_buyer@kln.ac.lk"));
    }

    @Test
    void createOrder_SelfPurchase_ThrowsIllegalArgumentException() {
        OrderRequestDto request = new OrderRequestDto(10, "CARD");

        when(userRepository.findByUniversityEmail("order_seller@kln.ac.lk")).thenReturn(Optional.of(seller));
        when(listingRepository.findById(10)).thenReturn(Optional.of(listing));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request, "order_seller@kln.ac.lk"));
    }

    @Test
    void getUserOrders_Success() {
        Order order = new Order(listing, buyer, new BigDecimal("45.00"), "CARD");
        order.setId(100);

        when(userRepository.findByUniversityEmail("order_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerIdOrListingSellerIdOrderByCreatedAtDesc(1, 1)).thenReturn(List.of(order));

        List<OrderResponseDto> orders = orderService.getUserOrders("order_buyer@kln.ac.lk");

        assertEquals(1, orders.size());
        assertEquals(100, orders.get(0).getId());
    }

    @Test
    void getOrderById_Success_AsBuyer() {
        Order order = new Order(listing, buyer, new BigDecimal("45.00"), "CARD");
        order.setId(100);

        when(userRepository.findByUniversityEmail("order_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));

        OrderResponseDto response = orderService.getOrderById(100, "order_buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(100, response.getId());
    }

    @Test
    void getOrderById_Unauthorized_ThrowsAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(99);
        otherUser.setUniversityEmail("other@kln.ac.lk");

        Order order = new Order(listing, buyer, new BigDecimal("45.00"), "CARD");
        order.setId(100);

        when(userRepository.findByUniversityEmail("other@kln.ac.lk")).thenReturn(Optional.of(otherUser));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.getOrderById(100, "other@kln.ac.lk"));
    }

    @Test
    void updateOrderStatus_Completed_UpdatesListingToSold() {
        Order order = new Order(listing, buyer, new BigDecimal("45.00"), "CARD");
        order.setId(100);

        OrderStatusUpdateDto updateDto = new OrderStatusUpdateDto(OrderStatus.completed);

        when(userRepository.findByUniversityEmail("order_seller@kln.ac.lk")).thenReturn(Optional.of(seller));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.updateOrderStatus(100, updateDto, "order_seller@kln.ac.lk");

        assertEquals(OrderStatus.completed, response.getStatus());
        assertEquals(ListingStatus.sold, listing.getStatus());
        verify(listingRepository, times(1)).save(listing);
    }

    @Test
    void updateOrderStatus_Cancelled_RestoresListingToAvailable() {
        listing.setStatus(ListingStatus.sold);

        Order order = new Order(listing, buyer, new BigDecimal("45.00"), "CARD");
        order.setId(100);

        OrderStatusUpdateDto updateDto = new OrderStatusUpdateDto(OrderStatus.cancelled);

        when(userRepository.findByUniversityEmail("order_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.updateOrderStatus(100, updateDto, "order_buyer@kln.ac.lk");

        assertEquals(OrderStatus.cancelled, response.getStatus());
        assertEquals(ListingStatus.available, listing.getStatus());
        verify(listingRepository, times(1)).save(listing);
    }
}
