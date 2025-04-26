package pack.smartwaste.controllers.annouce;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pack.smartwaste.RequestsEntities.ImageRequest;
import pack.smartwaste.RequestsEntities.ImageResponse;
import pack.smartwaste.controllers.FastAPIController;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.rep.AnnonceRepository;
import pack.smartwaste.services.AnnonceService;
import pack.smartwaste.services.FastApiService;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class AnnonceGraphQLResolver {
    private final AnnonceService annonceService;
    private final FastApiService fastApiService;
    private static final int PAGE_SIZE = 25;
    private static final Logger logger = LoggerFactory.getLogger(AnnonceGraphQLResolver.class);




    /*
    @QueryMapping
    public List<Annonce> getAllAnnoncesByCity(
            @Argument String city,
            @Argument(name = "page") Integer page
            ) {
        int pageNumber = (page != null) ? page : 0;
        if (city == null) System.out.println("the city is null");
        try {
            return annonceService.getAllAnnoncesDescOrByCity(city, pageNumber, PAGE_SIZE);
        } catch (Exception e) {
            return null;
        }

    }

    @QueryMapping
    public List<Annonce> searchAnnonces(@Argument String pattern,@Argument Integer page) {
        int pageNumber = (page != null) ? page : 0;
        try {
            return annonceService.searchAnnoncesByPattern(pattern, pageNumber, PAGE_SIZE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }


     */

    @QueryMapping
    public Annonce getAnnonceById(@Argument Long id) {
        return annonceService.getAnnonceById(id)
                .orElseThrow(() -> new RuntimeException("Annonce not found with id " + id));
    }

    @QueryMapping
    public List<Annonce> getAllAnnonces(
            @Argument String city,
            @Argument String pattern,
            @Argument Integer page
    ) {
        int pageNumber = (page != null) ? page : 0;
        try {
            return annonceService.getAllAnnoncesByCityAndPattern(city, pattern, pageNumber, PAGE_SIZE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @QueryMapping
    public List<Annonce> searchAnnoncesByImage(@Argument String image) {
        try {
            logger.warn("Fetching similar images for URL: {}", image);

            // Send image to FastAPI to get similar image URLs
            ImageResponse imageResponse = fastApiService.getSimilarImages(image);
            if (imageResponse == null || imageResponse.getUrls() == null) {
                logger.warn("No valid image URLs received from FastAPI");
                return Collections.emptyList();
            }

            logger.warn("Received image URLs: {}", imageResponse.getUrls());

            // Find annonces based on those image URLs in order
            return annonceService.findAnnoncesByImageUrlsOrdered(imageResponse.getUrls());

        } catch (Exception e) {
            logger.error("Error in searchAnnoncesByImage: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


}

