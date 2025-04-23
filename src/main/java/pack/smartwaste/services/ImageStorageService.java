package pack.smartwaste.services;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${upload.folder}")
    private String uploadFolder;

    public String saveImage(MultipartFile file) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        // Generate unique file name
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String baseFileName = UUID.randomUUID().toString();
        String originalFileName = baseFileName + "." + fileExtension;
        String resizedFileName = baseFileName + "_resized." + fileExtension;

        // Ensure upload directory exists
        File directory = new File(uploadFolder);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Read image to check dimensions
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // Save original image (Resize only if larger than 1080x1080)
        Path originalFilePath = Path.of(uploadFolder, originalFileName);
        if (originalImage.getWidth() > 1080 || originalImage.getHeight() > 1080) {
            Thumbnails.of(originalImage)
                    .size(1080, 1080)
                    .outputFormat(fileExtension)
                    .toFile(originalFilePath.toFile());
        } else {
            Files.copy(file.getInputStream(), originalFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Save resized image (max 150px)
        Path resizedFilePath = Path.of(uploadFolder, resizedFileName);
        Thumbnails.of(originalImage)
                .size(150, 150)
                .outputFormat(fileExtension)
                .toFile(resizedFilePath.toFile());

        /*
        return new String[]{
                "/uploads/" + originalFileName, // URL for the original image
                "/uploads/" + resizedFileName   // URL for the resized image
        };

         */

        return "/uploads/" + originalFileName;
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }
}
