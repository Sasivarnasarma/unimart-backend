package varna.mit.kln.unimart.listing.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.category.entity.Category;
import varna.mit.kln.unimart.category.repository.CategoryRepository;
import varna.mit.kln.unimart.common.exception.ConflictException;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;
import varna.mit.kln.unimart.listing.dto.ListingImageRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingImageResponseDto;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingResponseDto;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingImage;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.repository.ListingImageRepository;
import varna.mit.kln.unimart.listing.repository.ListingRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingImageRepository listingImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ListingService listingService;

    private User seller;
    private Category category;

    @BeforeEach
    void setUp() {
        listingService = new ListingServiceImpl(listingRepository, listingImageRepository, userRepository, categoryRepository);

        seller = new User();
        seller.setId(1);
        seller.setUniversityEmail("seller@kln.ac.lk");
        seller.setFullName("Seller User");

        category = new Category();
        category.setId(1);
        category.setName("Textbooks");
    }

    @Test
    void createListing_Success() {
        ListingRequestDto request = new ListingRequestDto();
        request.setCategoryId(1);
        request.setTitle("Java Book");
        request.setDescription("Good condition");
        request.setPrice(new BigDecimal("1500.00"));

        when(userRepository.findByUniversityEmail("seller@kln.ac.lk")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        Listing savedListing = new Listing();
        savedListing.setId(10);
        savedListing.setSeller(seller);
        savedListing.setCategory(category);
        savedListing.setTitle("Java Book");
        savedListing.setDescription("Good condition");
        savedListing.setPrice(new BigDecimal("1500.00"));
        savedListing.setStatus(ListingStatus.available);

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);

        ListingResponseDto response = listingService.createListing(request, "seller@kln.ac.lk");

        assertNotNull(response);
        assertEquals(10, response.getId());
        assertEquals("Java Book", response.getTitle());
        assertEquals(new BigDecimal("1500.00"), response.getPrice());
        assertEquals("seller@kln.ac.lk", seller.getUniversityEmail());
    }

    @Test
    void createListing_WithImages_Success() {
        ListingRequestDto request = new ListingRequestDto();
        request.setCategoryId(1);
        request.setTitle("Java Book");
        request.setPrice(new BigDecimal("1500.00"));
        request.setImages(List.of(new ListingImageRequestDto("http://example.com/img1.jpg", 1)));

        when(userRepository.findByUniversityEmail("seller@kln.ac.lk")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        Listing savedListing = new Listing();
        savedListing.setId(10);
        savedListing.setSeller(seller);
        savedListing.setCategory(category);
        savedListing.setTitle("Java Book");
        savedListing.setPrice(new BigDecimal("1500.00"));

        ListingImage savedImage = new ListingImage(savedListing, "http://example.com/img1.jpg", 1);
        savedImage.setId(100);

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);
        when(listingImageRepository.saveAll(anyList())).thenReturn(List.of(savedImage));

        ListingResponseDto response = listingService.createListing(request, "seller@kln.ac.lk");

        assertNotNull(response);
        assertEquals(1, response.getImages().size());
        assertEquals("http://example.com/img1.jpg", response.getImages().get(0).getImageUrl());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchListings_Paginated_Success() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle("Java Book");

        Page<Listing> page = new PageImpl<>(List.of(listing));

        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(listingImageRepository.findByListingIdOrderBySortOrderAsc(10)).thenReturn(List.of());

        Page<ListingResponseDto> result = listingService.searchListings("Java", 1, ListingStatus.available, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Book", result.getContent().get(0).getTitle());
    }

    @Test
    void getListingById_Success() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle("Java Book");

        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));
        when(listingImageRepository.findByListingIdOrderBySortOrderAsc(10)).thenReturn(List.of());

        ListingResponseDto response = listingService.getListingById(10);

        assertNotNull(response);
        assertEquals(10, response.getId());
    }

    @Test
    void getListingById_NotFound_ThrowsResourceNotFoundException() {
        when(listingRepository.findByIdAndStatusNot(99, ListingStatus.inactive)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listingService.getListingById(99));
    }

    @Test
    void updateListing_Success_AsSeller() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle("Old Title");
        listing.setPrice(new BigDecimal("1000.00"));

        ListingRequestDto updateRequest = new ListingRequestDto();
        updateRequest.setTitle("New Title");
        updateRequest.setPrice(new BigDecimal("1200.00"));

        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(listingImageRepository.findByListingIdOrderBySortOrderAsc(10)).thenReturn(List.of());

        ListingResponseDto response = listingService.updateListing(10, updateRequest, "seller@kln.ac.lk");

        assertNotNull(response);
        assertEquals("New Title", response.getTitle());
    }

    @Test
    void updateListing_Unauthorized_ThrowsAccessDeniedException() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);

        ListingRequestDto updateRequest = new ListingRequestDto();
        updateRequest.setTitle("New Title");

        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));

        assertThrows(AccessDeniedException.class, () -> listingService.updateListing(10, updateRequest, "other@kln.ac.lk"));
    }

    @Test
    void archiveListing_Success() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setStatus(ListingStatus.available);

        when(listingRepository.findById(10)).thenReturn(Optional.of(listing));

        listingService.archiveListing(10, "seller@kln.ac.lk");

        assertEquals(ListingStatus.inactive, listing.getStatus());
        verify(listingRepository, times(1)).save(listing);
    }

    @Test
    void archiveListing_Sold_ThrowsConflictException() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setStatus(ListingStatus.sold);

        when(listingRepository.findById(10)).thenReturn(Optional.of(listing));

        assertThrows(ConflictException.class, () -> listingService.archiveListing(10, "seller@kln.ac.lk"));
    }

    @Test
    void addImage_Success() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);

        ListingImageRequestDto imageDto = new ListingImageRequestDto("http://example.com/img2.jpg", 2);

        ListingImage savedImage = new ListingImage(listing, "http://example.com/img2.jpg", 2);
        savedImage.setId(101);

        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImageResponseDto response = listingService.addImage(10, imageDto, "seller@kln.ac.lk");

        assertNotNull(response);
        assertEquals(101, response.getId());
        assertEquals("http://example.com/img2.jpg", response.getImageUrl());
    }

    @Test
    void removeImage_Success() {
        Listing listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);

        ListingImage image = new ListingImage(listing, "http://example.com/img1.jpg", 1);
        image.setId(100);

        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));
        when(listingImageRepository.findById(100)).thenReturn(Optional.of(image));

        listingService.removeImage(10, 100, "seller@kln.ac.lk");

        verify(listingImageRepository, times(1)).delete(image);
    }
}
