package pack.smartwaste.rep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pack.smartwaste.models.user.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Custom JPQL Query (Optional alternative)
    @Query("SELECT u FROM User u WHERE u.username = :value OR u.email = :value")
    Optional<User> findByUsernameOrEmailCustom(@Param("value") String value);
}
