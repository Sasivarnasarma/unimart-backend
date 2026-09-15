package varna.mit.kln.unimart.listing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Integer>, JpaSpecificationExecutor<Listing> {
    List<Listing> findByCategoryId(Integer categoryId);
    Optional<Listing> findByIdAndStatusNot(Integer id, ListingStatus status);
}

