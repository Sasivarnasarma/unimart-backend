package varna.mit.kln.unimart.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUniversityEmail(String universityEmail);
}
