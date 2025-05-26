package pack.smartwaste.models.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class City {
    @Id
    private Long id;
    @Column(nullable = false)
    private String ville;
    @Column(nullable = false)
    private Long region;
}
