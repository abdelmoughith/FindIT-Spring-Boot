package pack.smartwaste.models.post;

import lombok.Data;
import pack.smartwaste.models.user.City;
import pack.smartwaste.models.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnnonceResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime datePublished;
    private String status;
    private List<String> imageUrls;
    private String location;
    private City city;
    private User user;
    private boolean isSaved;
}

