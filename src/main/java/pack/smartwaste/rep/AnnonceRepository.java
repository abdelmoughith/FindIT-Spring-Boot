package pack.smartwaste.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.models.user.City;

import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    Page<Annonce> findAllByOrderByDatePublishedDesc(Pageable pageable);

    Page<Annonce> findAllByCityOrderByDatePublishedDesc(City city, Pageable pageable);

    Page<Annonce> findByTitleContainingOrDescriptionContainingOrLocationContaining(String title, String description, String location, Pageable pageable);

    Page<Annonce> findByCityAndTitleContainingIgnoreCaseOrCityAndDescriptionContainingIgnoreCaseOrCityAndLocationContainingIgnoreCaseOrderByDatePublishedDesc(
            City city1, String title, City city2, String description, City city3, String location, Pageable pageable
    );
    // find by images url for search by image
    @Query("SELECT DISTINCT a FROM Annonce a JOIN a.imageUrls i WHERE i IN :imageUrls")
    List<Annonce> findByImageUrls(@Param("imageUrls") List<String> imageUrls);

}