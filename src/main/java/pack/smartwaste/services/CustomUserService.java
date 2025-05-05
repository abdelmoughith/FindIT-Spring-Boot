package pack.smartwaste.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.models.user.City;
import pack.smartwaste.models.user.Role;
import pack.smartwaste.models.user.User;
import pack.smartwaste.models.user.UserResponseDTO;
import pack.smartwaste.rep.RoleRepository;
import pack.smartwaste.rep.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;


@Service
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ImageStorageServiceCloud imageStorageServiceCloud;


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

    @Transactional
    // update image pic
    public String updateProfilePicture(
            User user,
            MultipartFile image
    ) throws IOException {
        if (image != null && !image.isEmpty()) {
            String response = imageStorageServiceCloud.saveProfileImage(image);
            user.setProfileImage(response);
            userRepository.save(user);
            return response;
        }
        throw new IOException("Image is null or empty");
    }

}
