package varna.mit.kln.unimart.listing.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingResponseDto;
import varna.mit.kln.unimart.listing.service.ListingService;

@RestController
@RequestMapping("/api/v1/public/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<ListingResponseDto> create(@Valid @RequestBody ListingRequestDto requestDto) {
        ListingResponseDto responseDto = listingService.createListing(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ListingResponseDto>> getAll(@RequestParam(required = false) Integer categoryId) {
        List<ListingResponseDto> listings = listingService.getAllListings(categoryId);
        return new ResponseEntity<>(listings, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponseDto> getById(@PathVariable Integer id) {
        ListingResponseDto responseDto = listingService.getListingById(id);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
