package pack.smartwaste.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pack.smartwaste.RequestsEntities.ImageRequest;
import pack.smartwaste.RequestsEntities.ImageResponse;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.services.AnnonceService;
import pack.smartwaste.services.FastApiService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Validated
public class FastAPIController {

    private static final Logger logger = LoggerFactory.getLogger(FastAPIController.class);


    @Autowired
    private FastApiService fastApiService;

    @Autowired
    private AnnonceService annonceService;

    @PostMapping("/search")
    public ImageResponse findSimilar(@Valid @RequestBody ImageRequest request) {
        return fastApiService.getSimilarImages(request.getImage());
    }

    @PostMapping("/search-by-images")
    public ResponseEntity<?> searchAnnoncesByImageUrls(@Valid @RequestBody ImageRequest request) {
        try {
            logger.warn("Fetching similar images for URL: {}", request.getImage());

            // Fetch similar images from FastAPI
            ImageResponse imageResponse = fastApiService.getSimilarImages(request.getImage());
            if (imageResponse == null || imageResponse.getUrls() == null) {
                logger.warn("No valid image URLs received from FastAPI");
                return ResponseEntity.badRequest().body("Failed to fetch similar images");
            }

            logger.warn("Received image URLs: {}", imageResponse.getUrls());

            // Find annonces by image URLs
            List<Annonce> annonces = annonceService.findAnnoncesByImageUrlsOrdered(imageResponse.getUrls());
            logger.warn("Found {} annonces matching the image URLs", annonces.size());

            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request");
        }
    }
}

