package pack.smartwaste.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pack.smartwaste.services.ImageStorageServiceCloud;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/helper")
public class HelperController {

    private final ImageStorageServiceCloud imageStorageServiceCloud;

    @PostMapping("/post-raw-image")
    public String findAnnoncesByImageUrlsOrderedAfterPosting(
            MultipartFile image
    ) throws IOException {
        return imageStorageServiceCloud.uploadRawImageToTMPfolder(image);
    }
}
