package pack.smartwaste.rep;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.smartwaste.models.user.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
