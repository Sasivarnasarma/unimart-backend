package varna.mit.kln.unimart.listing.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.listing.entity.ListingImage;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Integer> {
    List<ListingImage> findByListingIdOrderBySortOrderAsc(Integer listingId);
}
