package varna.mit.kln.unimart.listing.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.category.entity.Category;
import varna.mit.kln.unimart.category.repository.CategoryRepository;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingResponseDto;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.repository.ListingRepository;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ListingServiceImpl(
            ListingRepository listingRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public ListingResponseDto createListing(ListingRequestDto requestDto) {
        User seller = userRepository.findById(requestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with ID: " + requestDto.getSellerId()));

        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + requestDto.getCategoryId()));

        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle(requestDto.getTitle());
        listing.setDescription(requestDto.getDescription());
        listing.setPrice(requestDto.getPrice());
        listing.setStatus(requestDto.getStatus());

        Listing savedListing = listingRepository.save(listing);
        return new ListingResponseDto(savedListing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingResponseDto> getAllListings(Integer categoryId) {
        List<Listing> listings;
        if (categoryId != null) {
            listings = listingRepository.findByCategoryId(categoryId);
        } else {
            listings = listingRepository.findAll();
        }
        return listings.stream()
                .map(ListingResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponseDto getListingById(Integer id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));
        return new ListingResponseDto(listing);
    }
}
