package pack.smartwaste.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.user.Role;
import pack.smartwaste.models.user.User;
import pack.smartwaste.rep.RoleRepository;
import pack.smartwaste.rep.UserRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Transactional
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmailCustom(username).orElse(null);
    }


    @Transactional
    public User registerUser(User user) throws Exception {

        if (loadUserByUsername(user.getUsername()) != null) {
            throw new Exception("Username already taken");
        }
        if (loadUserByUsername(user.getEmail()) != null) {
            throw new Exception("Email already taken");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now().minusYears(18))) {
            throw new Exception("User must be at least 18 years old");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("No role found")
        ));
        user.setRoles(roles);
        return userRepository.save(user);
    }

    /*
    // TODO TO UPDATE USER NEXT TIME
    public User updateUser(Long id, User user) {
        if (loadUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("This username is already taken");
        }
        if (loadUserByUsername(user.getEmail()) != null) {
            throw new RuntimeException("Email already taken");
        }
        User updatedUser = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("No such user found (invalid id or token)")
        );
        updatedUser.setEmail(user.getEmail());
        updatedUser.setUsername(user.getUsername());

    }

     */

    public Optional<User> findUserById(Long id) throws UsernameNotFoundException {
        return userRepository.findById(id);
    }

}
