package pack.smartwaste.controllers.annouce;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;
import pack.smartwaste.RequestsEntities.FastApiResponse;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.models.post.AnnonceResponse;
import pack.smartwaste.models.user.User;
import pack.smartwaste.services.AnnonceService;
import pack.smartwaste.services.CustomUserService;
import pack.smartwaste.services.FastApiService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Controller
public class AnnonceGraphQLResolver {
    private final AnnonceService annonceService;
    private final FastApiService fastApiService;
    private final CustomUserService userService;
    private static final int PAGE_SIZE = 25;
    private static final Logger logger = LoggerFactory.getLogger(AnnonceGraphQLResolver.class);
    private final JwtUtils jwtUtil;




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
    public AnnonceResponse getAnnonceById(@Argument Long id) {
        Annonce annonce = annonceService.getAnnonceById(id)
                .orElseThrow(() -> new RuntimeException("Annonce not found with id " + id));
        AnnonceResponse response = new AnnonceResponse();
        response.setId(annonce.getId());
        response.setTitle(annonce.getTitle());
        response.setDescription(annonce.getDescription());
        response.setDatePublished(annonce.getDatePublished());
        response.setStatus(annonce.getStatus().name());
        response.setImageUrls(annonce.getImageUrls());
        response.setLocation(annonce.getLocation());
        response.setCity(annonce.getCity());
        response.setUser(annonce.getUser());
        response.setSaved(true);
        return response;
    }

    @QueryMapping
    public List<AnnonceResponse> getAllAnnonces(
            @Argument String token,
            @Argument String city,
            @Argument String pattern,
            @Argument Integer page,
            @Argument Boolean onlySaved
    ) {
        int pageNumber = (page != null) ? page : 0;
        try {
            List<Annonce> annonces = annonceService.getAllAnnoncesByCityAndPattern(city, pattern, pageNumber, PAGE_SIZE);
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = userService.loadUserByUsername(username).getId();
            Set<Long> savedAnnonceIds = annonceService.getSavedAnnonces(userId).stream()
                    .map(Annonce::getId)
                    .collect(Collectors.toSet());
            List<AnnonceResponse> responseList;
            if (onlySaved){
                responseList = annonces.stream()
                        .filter(annonce -> savedAnnonceIds.contains(annonce.getId()))
                        .map(annonce -> {
                            AnnonceResponse response = new AnnonceResponse();
                            response.setId(annonce.getId());
                            response.setTitle(annonce.getTitle());
                            response.setDescription(annonce.getDescription());
                            response.setDatePublished(annonce.getDatePublished());
                            response.setStatus(annonce.getStatus().name());
                            response.setImageUrls(annonce.getImageUrls());
                            response.setLocation(annonce.getLocation());
                            response.setCity(annonce.getCity());
                            response.setUser(annonce.getUser());
                            response.setSaved(true);
                            return response;
                        })
                        .toList();
            } else {
                responseList = annonces.stream().map(annonce -> {
                    AnnonceResponse response = new AnnonceResponse();
                    response.setId(annonce.getId());
                    response.setTitle(annonce.getTitle());
                    response.setDescription(annonce.getDescription());
                    response.setDatePublished(annonce.getDatePublished());
                    response.setStatus(annonce.getStatus().name());
                    response.setImageUrls(annonce.getImageUrls());
                    response.setLocation(annonce.getLocation());
                    response.setCity(annonce.getCity());
                    response.setUser(annonce.getUser());
                    response.setSaved(savedAnnonceIds.contains(annonce.getId()));
                    return response;
                }).toList();
            }

            return responseList;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    @QueryMapping
    public List<AnnonceResponse> getAllAnnoncesMine(
            @Argument String token
    ) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.loadUserByUsername(username);
            List<Annonce> annonces = annonceService.getAnnoncesByUser(user);

            Long userId = user.getId();
            Set<Long> savedAnnonceIds = annonceService.getSavedAnnonces(userId).stream()
                    .map(Annonce::getId)
                    .collect(Collectors.toSet());
            List<AnnonceResponse> responseList;

            responseList = annonces.stream().map(annonce -> {
                AnnonceResponse response = new AnnonceResponse();
                response.setId(annonce.getId());
                response.setTitle(annonce.getTitle());
                response.setDescription(annonce.getDescription());
                response.setDatePublished(annonce.getDatePublished());
                response.setStatus(annonce.getStatus().name());
                response.setImageUrls(annonce.getImageUrls());
                response.setLocation(annonce.getLocation());
                response.setCity(annonce.getCity());
                response.setUser(annonce.getUser());
                response.setSaved(savedAnnonceIds.contains(annonce.getId()));
                return response;
            }).toList();




            return responseList;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }



    @QueryMapping
    public List<AnnonceResponse> searchAnnoncesByImage(@Argument String image,
                                                       @Argument String token) {
        try {
            logger.warn("Fetching similar images for URL: {}", image);

            // Send image to FastAPI to get similar image URLs
            FastApiResponse fastApiResponse = fastApiService.getSimilarImages(image);
            if (fastApiResponse == null || fastApiResponse.getUrls() == null) {
                logger.warn("No valid image URLs received from FastAPI");
                return Collections.emptyList();
            }

            logger.warn("Received image URLs: {}", fastApiResponse.getUrls());

            // Find annonces based on those image URLs in order
            List<Annonce> annonceList = annonceService.findAnnoncesByImageUrlsOrdered(fastApiResponse.getUrls());
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = userService.loadUserByUsername(username).getId();
            Set<Long> savedAnnonceIds = annonceService.getSavedAnnonces(userId).stream()
                    .map(Annonce::getId)
                    .collect(Collectors.toSet());
            List<AnnonceResponse> responseList = annonceList.stream().map(annonce -> {
                        AnnonceResponse response = new AnnonceResponse();
                        response.setId(annonce.getId());
                        response.setTitle(annonce.getTitle());
                        response.setDescription(annonce.getDescription());
                        response.setDatePublished(annonce.getDatePublished());
                        response.setStatus(annonce.getStatus().name());
                        response.setImageUrls(annonce.getImageUrls());
                        response.setLocation(annonce.getLocation());
                        response.setCity(annonce.getCity());
                        response.setUser(annonce.getUser());
                        response.setSaved(savedAnnonceIds.contains(annonce.getId()));
                        return response;
                    })
                    .toList();
            return responseList;

        } catch (Exception e) {
            logger.error("Error in searchAnnoncesByImage: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    @QueryMapping
    public List<AnnonceResponse> searchAnnoncesByText(@Argument String text,
                                                       @Argument String token) {
        try {
            logger.warn("Fetching similar annonce for URL: {}", text);

            // Send image to FastAPI to get similar image URLs
            FastApiResponse fastApiResponse = fastApiService.getSimilarImagesByText(text);
            if (fastApiResponse == null || fastApiResponse.getUrls() == null) {
                logger.warn("No valid image URLs received from FastAPI");
                return Collections.emptyList();
            }

            logger.warn("Received image URLs: {}", fastApiResponse.getUrls());

            // Find annonces based on those image URLs in order
            List<Annonce> annonceList = annonceService.findAnnoncesByImageUrlsOrdered(fastApiResponse.getUrls());
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Long userId = userService.loadUserByUsername(username).getId();
            Set<Long> savedAnnonceIds = annonceService.getSavedAnnonces(userId).stream()
                    .map(Annonce::getId)
                    .collect(Collectors.toSet());
            List<AnnonceResponse> responseList = annonceList.stream().map(annonce -> {
                        AnnonceResponse response = new AnnonceResponse();
                        response.setId(annonce.getId());
                        response.setTitle(annonce.getTitle());
                        response.setDescription(annonce.getDescription());
                        response.setDatePublished(annonce.getDatePublished());
                        response.setStatus(annonce.getStatus().name());
                        response.setImageUrls(annonce.getImageUrls());
                        response.setLocation(annonce.getLocation());
                        response.setCity(annonce.getCity());
                        response.setUser(annonce.getUser());
                        response.setSaved(savedAnnonceIds.contains(annonce.getId()));
                        return response;
                    })
                    .toList();
            return responseList;

        } catch (Exception e) {
            logger.error("Error in searchAnnoncesByImage: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }




}

