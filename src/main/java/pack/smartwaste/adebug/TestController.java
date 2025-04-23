package pack.smartwaste.adebug;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pack.smartwaste.services.ImageStorageService;
import pack.smartwaste.services.ImageStorageServiceCloud;

import java.io.IOException;

@RestController
@RequestMapping("/test")
public class TestController {
    private final ImageStorageServiceCloud imageStorageService;

    public TestController(ImageStorageServiceCloud imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Save the image and get the URL path
            String imageUrl = imageStorageService.saveImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully: " + imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload the file: " + e.getMessage());
        }
    }
}
