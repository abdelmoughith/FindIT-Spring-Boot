package pack.smartwaste.controllers.annouce;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.rep.AnnonceRepository;
import pack.smartwaste.services.AnnonceService;

import java.util.List;

@Controller
public class AnnonceGraphQLResolver {
    private final AnnonceService annonceService;
    private static final int PAGE_SIZE = 25;

    public AnnonceGraphQLResolver(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }


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
    public Annonce getAnnonceById(@Argument Long id) {
        return annonceService.getAnnonceById(id)
                .orElseThrow(() -> new RuntimeException("Annonce not found with id " + id));
    }
}

