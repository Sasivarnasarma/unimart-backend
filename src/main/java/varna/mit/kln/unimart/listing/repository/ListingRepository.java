package varna.mit.kln.unimart.listing.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.listing.entity.Listing;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Integer> {
    List<Listing> findByCategoryId(Integer categoryId);
}
