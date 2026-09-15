package varna.mit.kln.unimart.listing.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;

import java.util.ArrayList;
import java.util.List;

public class ListingSpecification {

    public static Specification<Listing> filterListings(String query, Integer categoryId, ListingStatus status) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleLike, descLike));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                predicates.add(cb.notEqual(root.get("status"), ListingStatus.inactive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
