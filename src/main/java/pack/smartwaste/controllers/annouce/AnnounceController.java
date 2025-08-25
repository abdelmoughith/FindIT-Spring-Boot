package pack.smartwaste.controllers.annouce;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.models.user.User;
import pack.smartwaste.services.AnnonceService;
import pack.smartwaste.services.CustomUserService;

import java.io.File;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/annonces")
public class AnnounceController {
    private final AnnonceService annonceService;
    private final CustomUserService customUserService;
    private final JwtUtils jwtUtils;

    public AnnounceController(AnnonceService annonceService, CustomUserService customUserService, JwtUtils jwtUtils) {
        this.annonceService = annonceService;
        this.customUserService = customUserService;
        this.jwtUtils = jwtUtils;
    }

    // Create
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createAnnonce(
            @RequestParam("annonce") String annonceJson,
            @RequestParam("city") String city,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws IOException {

        // Convert JSON string to Annonce object
        ObjectMapper objectMapper = new ObjectMapper();
        Annonce annonce = objectMapper.readValue(annonceJson, Annonce.class);

        // Extract user from JWT token
        String token = authorizationHeader.substring(7);
        Long userId = jwtUtils.extractUserId(token);
        User user = customUserService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        annonce.setUser(user);

        System.out.println("Received annonce: " + annonce);
        if (images != null) {
            System.out.println("Received " + images.size() + " images.");
        }
        try {
            return ResponseEntity.ok(annonceService.createAnnonce(annonce, images, city));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }




    // Update
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Annonce> updateAnnonce(
            @PathVariable Long id,
            @RequestParam("annonce") String annonceJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Annonce annonceDetails = objectMapper.readValue(annonceJson, Annonce.class);

        return ResponseEntity.ok(annonceService.updateAnnonce(id, annonceDetails, images));
    }


    // Read (Get by ID)
    @GetMapping("/{id}")
    public ResponseEntity<Annonce> getAnnonceById(@PathVariable Long id) {
        return annonceService.getAnnonceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnonce(@PathVariable Long id) {
        try {
            annonceService.deleteAnnonce(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /*
    @Value("${upload.folder}")
    private String uploadFolder;
    // get image cover
    // use this code when you want to download image

    @GetMapping("/image/{imageName}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        File file = new File(uploadFolder+"/" + imageName);
        if (file.exists()) {
            //System.out.println("File exists: " + file.getAbsolutePath());
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
        }
        //System.out.println("File does not exist: " + file.getAbsolutePath());
        return ResponseEntity.notFound().build();
    }

     */
    @GetMapping("/all")
    public List<Annonce> getAllAnnonces() {
        return annonceService.getAll();
    }


}
