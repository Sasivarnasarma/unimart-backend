package varna.mit.kln.unimart.listing.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import varna.mit.kln.unimart.listing.repository.ListingSpecification;

@Service
public class ListingServiceImpl implements ListingService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ListingServiceImpl(
            ListingRepository listingRepository,
            ListingImageRepository listingImageRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public ListingResponseDto createListing(ListingRequestDto requestDto, String userEmail) {
        User seller = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + requestDto.getCategoryId()));

        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle(requestDto.getTitle());
        listing.setDescription(requestDto.getDescription());
        listing.setPrice(requestDto.getPrice());
        listing.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : ListingStatus.available);

        Listing savedListing = listingRepository.save(listing);

        List<ListingImage> savedImages = null;
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<ListingImage> imagesToSave = requestDto.getImages().stream()
                    .map(imgDto -> new ListingImage(savedListing, imgDto.getImageUrl(), imgDto.getSortOrder()))
                    .collect(Collectors.toList());
            savedImages = listingImageRepository.saveAll(imagesToSave);
        }

        return new ListingResponseDto(savedListing, savedImages);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingResponseDto> searchListings(String query, Integer categoryId, ListingStatus status, Pageable pageable) {
        int effectivePageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), effectivePageSize, pageable.getSort());

        Page<Listing> listingsPage = listingRepository.findAll(
                ListingSpecification.filterListings(query, categoryId, status),
                cappedPageable
        );

        return listingsPage.map(listing -> {
            List<ListingImage> images = listingImageRepository.findByListingIdOrderBySortOrderAsc(listing.getId());
            return new ListingResponseDto(listing, images);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponseDto getListingById(Integer id) {
        Listing listing = listingRepository.findByIdAndStatusNot(id, ListingStatus.inactive)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));

        List<ListingImage> images = listingImageRepository.findByListingIdOrderBySortOrderAsc(id);
        return new ListingResponseDto(listing, images);
    }

    @Override
    @Transactional
    public ListingResponseDto updateListing(Integer id, ListingRequestDto requestDto, String userEmail) {
        Listing listing = listingRepository.findByIdAndStatusNot(id, ListingStatus.inactive)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));

        verifySellerOwnership(listing, userEmail);

        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + requestDto.getCategoryId()));
            listing.setCategory(category);
        }

        if (requestDto.getTitle() != null) {
            listing.setTitle(requestDto.getTitle());
        }
        if (requestDto.getDescription() != null) {
            listing.setDescription(requestDto.getDescription());
        }
        if (requestDto.getPrice() != null) {
            listing.setPrice(requestDto.getPrice());
        }
        if (requestDto.getStatus() != null) {
            listing.setStatus(requestDto.getStatus());
        }

        Listing updatedListing = listingRepository.save(listing);
        List<ListingImage> images = listingImageRepository.findByListingIdOrderBySortOrderAsc(id);
        return new ListingResponseDto(updatedListing, images);
    }

    @Override
    @Transactional
    public void archiveListing(Integer id, String userEmail) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));

        verifySellerOwnership(listing, userEmail);

        if (listing.getStatus() == ListingStatus.sold) {
            throw new ConflictException("Cannot archive a sold listing");
        }

        listing.setStatus(ListingStatus.inactive);
        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public ListingImageResponseDto addImage(Integer listingId, ListingImageRequestDto imageDto, String userEmail) {
        Listing listing = listingRepository.findByIdAndStatusNot(listingId, ListingStatus.inactive)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + listingId));

        verifySellerOwnership(listing, userEmail);

        ListingImage listingImage = new ListingImage(listing, imageDto.getImageUrl(), imageDto.getSortOrder());
        ListingImage savedImage = listingImageRepository.save(listingImage);

        return new ListingImageResponseDto(savedImage);
    }

    @Override
    @Transactional
    public void removeImage(Integer listingId, Integer imageId, String userEmail) {
        Listing listing = listingRepository.findByIdAndStatusNot(listingId, ListingStatus.inactive)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + listingId));

        verifySellerOwnership(listing, userEmail);

        ListingImage image = listingImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));

        if (!image.getListing().getId().equals(listingId)) {
            throw new IllegalArgumentException("Image does not belong to listing with ID: " + listingId);
        }

        listingImageRepository.delete(image);
    }

    private void verifySellerOwnership(Listing listing, String userEmail) {
        if (listing.getSeller() == null || !listing.getSeller().getUniversityEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You are not authorized to modify this listing");
        }
    }
}
