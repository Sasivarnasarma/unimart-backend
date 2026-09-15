package varna.mit.kln.unimart.listing.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.listing.dto.ListingImageRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingImageResponseDto;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingResponseDto;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.service.ListingService;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<ListingResponseDto> create(
            @Valid @RequestBody ListingRequestDto requestDto,
            Principal principal) {
        ListingResponseDto responseDto = listingService.createListing(requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ListingResponseDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) ListingStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ListingResponseDto> page = listingService.searchListings(q, categoryId, status, pageable);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponseDto> getById(@PathVariable Integer id) {
        ListingResponseDto responseDto = listingService.getListingById(id);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody ListingRequestDto requestDto,
            Principal principal) {
        ListingResponseDto responseDto = listingService.updateListing(id, requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(
            @PathVariable Integer id,
            Principal principal) {
        listingService.archiveListing(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ListingImageResponseDto> addImage(
            @PathVariable Integer id,
            @Valid @RequestBody ListingImageRequestDto imageDto,
            Principal principal) {
        ListingImageResponseDto responseDto = listingService.addImage(id, imageDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> removeImage(
            @PathVariable Integer id,
            @PathVariable Integer imageId,
            Principal principal) {
        listingService.removeImage(id, imageId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
