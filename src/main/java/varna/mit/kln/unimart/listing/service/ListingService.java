package varna.mit.kln.unimart.listing.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import varna.mit.kln.unimart.listing.dto.ListingImageRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingImageResponseDto;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingResponseDto;
import varna.mit.kln.unimart.listing.entity.ListingStatus;

public interface ListingService {
    ListingResponseDto createListing(ListingRequestDto requestDto, String userEmail);
    Page<ListingResponseDto> searchListings(String query, Integer categoryId, ListingStatus status, Pageable pageable);
    ListingResponseDto getListingById(Integer id);
    ListingResponseDto updateListing(Integer id, ListingRequestDto requestDto, String userEmail);
    void archiveListing(Integer id, String userEmail);
    ListingImageResponseDto addImage(Integer listingId, ListingImageRequestDto imageDto, String userEmail);
    void removeImage(Integer listingId, Integer imageId, String userEmail);
}
