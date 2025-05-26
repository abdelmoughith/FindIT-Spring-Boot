package pack.smartwaste.models.user;

import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImage;
    private Set<String> roles;
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.profileImage = user.getProfileImage();
        this.roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }


}

