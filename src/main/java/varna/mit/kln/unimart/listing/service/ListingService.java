package varna.mit.kln.unimart.listing.service;

import java.util.List;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingResponseDto;

public interface ListingService {
    ListingResponseDto createListing(ListingRequestDto requestDto);
    List<ListingResponseDto> getAllListings(Integer categoryId);
    ListingResponseDto getListingById(Integer id);
}
