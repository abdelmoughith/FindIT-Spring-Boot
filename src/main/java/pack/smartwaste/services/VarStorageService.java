package pack.smartwaste.services;


import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class VarStorageService {

    // 🔴 CHANGE ONLY THIS IF NEEDED
    private static final String BASE_DIR = "/var/www/findit";

    /* ================= SAVE IMAGE ================= */

    public String saveImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String baseFileName = UUID.randomUUID().toString();
        String originalFileName = baseFileName + "." + fileExtension;
        String resizedFileName = baseFileName + "_resized." + fileExtension;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // main image
        ByteArrayOutputStream mainBaos = new ByteArrayOutputStream();
        if (originalImage.getWidth() > 1080 || originalImage.getHeight() > 1080) {
            Thumbnails.of(originalImage)
                    .size(1080, 1080)
                    .outputFormat(fileExtension)
                    .toOutputStream(mainBaos);
        } else {
            ImageIO.write(originalImage, fileExtension, mainBaos);
        }
        byte[] mainImageData = mainBaos.toByteArray();

        // preview
        ByteArrayOutputStream previewBaos = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(150, 150)
                .outputFormat(fileExtension)
                .toOutputStream(previewBaos);
        byte[] previewImageData = previewBaos.toByteArray();

        saveToLocal(mainImageData, "uploads/" + originalFileName);
        saveToLocal(previewImageData, "uploads/" + resizedFileName);

        return "/uploads/" + originalFileName;
    }

    /* ================= TMP IMAGE ================= */

    public String uploadRawImageToTMPfolder(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String rawFileName = UUID.randomUUID() + "." + fileExtension;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        if (originalImage.getWidth() > 5000 || originalImage.getHeight() > 5000) {
            throw new IllegalArgumentException("Image exceeds 5000px limit.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, fileExtension, baos);

        saveToLocal(baos.toByteArray(), "tmp/" + rawFileName);

        return "/tmp/" + rawFileName;
    }

    /* ================= PROFILE IMAGE ================= */

    public String saveProfileImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String originalFileName = UUID.randomUUID() + "." + fileExtension;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        ByteArrayOutputStream mainBaos = new ByteArrayOutputStream();
        if (originalImage.getWidth() > 1080 || originalImage.getHeight() > 1080) {
            Thumbnails.of(originalImage)
                    .size(1080, 1080)
                    .outputFormat(fileExtension)
                    .toOutputStream(mainBaos);
        } else {
            ImageIO.write(originalImage, fileExtension, mainBaos);
        }

        saveToLocal(mainBaos.toByteArray(), "profile_pictures/" + originalFileName);

        return "/profile_pictures/" + originalFileName;
    }

    /* ================= DELETE ================= */

    public void deleteImageFromFirebase(String url) throws IOException {
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        File file = new File(BASE_DIR + decoded);
        if (file.exists()) {
            file.delete();
        }
    }

    /* ================= HELPERS ================= */

    private void saveToLocal(byte[] data, String relativePath) throws IOException {
        File file = new File(BASE_DIR + "/" + relativePath);
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), data);
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }
}
