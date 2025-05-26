package pack.smartwaste.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class ImageStorageServiceCloud {


    @Value("${firebase.token}")
    private String firebaseTokenPath;

    // Initialize Firebase once at startup
    @PostConstruct
    public void initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream(firebaseTokenPath);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("indus-532b7.appspot.com")  // Your Firebase Storage bucket name
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            System.err.println("Firebase initialization error: " + e.getMessage());
        }
    }

    /**
     * Uploads an image to Firebase Storage.
     * Creates two versions:
     *  - The main image (resized to a maximum of 1080×1080 if necessary)
     *  - A preview image (resized to 150×150)
     *
     * @param file the uploaded file from the client
     * @return an array of two URLs: [0]=main image URL, [1]=preview image URL; returns null for a version if upload fails.
     * @throws IOException if reading the file fails
     */
    public String saveImage(MultipartFile file) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        // Determine file extension
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String baseFileName = UUID.randomUUID().toString();
        String originalFileName = baseFileName + "." + fileExtension;
        String resizedFileName = baseFileName + "_resized." + fileExtension;

        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Process main image: resize to a maximum of 1080x1080 if necessary
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

        // Process preview image: resize to 150x150
        ByteArrayOutputStream previewBaos = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(150, 150)
                .outputFormat(fileExtension)
                .toOutputStream(previewBaos);
        byte[] previewImageData = previewBaos.toByteArray();

        // Upload images to Firebase
        String mainImageUrl = uploadToFirebase(mainImageData, "uploads/" + originalFileName, fileExtension);
        String previewImageUrl = uploadToFirebase(previewImageData, "uploads/" + resizedFileName, fileExtension);

        return mainImageUrl;
    }

    /**
     * Uploads image data to Firebase Storage.
     *
     * @param imageData   the image data as a byte array
     * @param path        the path in the Firebase bucket
     * @param fileExtension the file extension to set the MIME type (e.g., "jpg", "png")
     * @return the public URL of the uploaded image or null if upload fails
     */
    private String uploadToFirebase(byte[] imageData, String path, String fileExtension) {
        try {
            // Generate a unique download token
            String downloadToken = UUID.randomUUID().toString();

            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of("indus-532b7.appspot.com", path);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setMetadata(java.util.Map.of("firebaseStorageDownloadTokens", downloadToken))
                    .setContentType("image/" + fileExtension)
                    .build();

            Blob blob = storage.create(blobInfo, imageData);
            if (blob == null) {
                return null;
            }

            // Construct the public URL
            return "https://firebasestorage.googleapis.com/v0/b/indus-532b7.appspot.com/o/"
                    + path.replace("/", "%2F")
                    + "?alt=media&token=" + downloadToken;
        } catch (Exception e) {
            System.err.println("Error uploading to Firebase: " + e.getMessage());
            return null;
        }
    }

    public String uploadRawImageToTMPfolder(MultipartFile file) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        // Determine file extension
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String baseFileName = UUID.randomUUID().toString();
        String rawFileName = baseFileName + "." + fileExtension;

        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Check if image exceeds size limits (height or width greater than 1080px)
        if (originalImage.getWidth() > 5000 || originalImage.getHeight() > 5000) {
            throw new IllegalArgumentException("Image height or width exceeds the 5000px limit.");
        }

        // Convert image to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, fileExtension, baos);
        byte[] imageData = baos.toByteArray();

        // Upload image to Firebase
        return uploadToFirebase(imageData, "tmp/" + rawFileName, fileExtension);
    }
    public void deleteImageFromFirebase(String firebaseUrl) throws IOException {
        // Extract the path from the Firebase URL
        String decodedUrl = URLDecoder.decode(firebaseUrl, StandardCharsets.UTF_8);

        // Example Firebase URL format:
        // https://firebasestorage.googleapis.com/v0/b/YOUR_PROJECT.appspot.com/o/tmp%2Ffilename.png?alt=media
        // You need to extract "tmp/filename.png"
        String prefix = "/o/";
        String suffix = "?alt=media";
        int start = decodedUrl.indexOf(prefix) + prefix.length();
        int end = decodedUrl.indexOf(suffix);

        if (start == -1 || end == -1) {
            throw new IllegalArgumentException("Invalid Firebase image URL.");
        }

        String filePath = decodedUrl.substring(start, end).replace("%2F", "/");

        // Now delete the file using Firebase Storage
        StorageClient.getInstance().bucket().get(filePath).delete();
    }


    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }

    public String saveProfileImage(MultipartFile file) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed.");
        }

        // Determine file extension
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String baseFileName = UUID.randomUUID().toString();
        String originalFileName = baseFileName + "." + fileExtension;

        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Process main image: resize to a maximum of 1080x1080 if necessary
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

        // Process preview image:
        // to 150x150
        ByteArrayOutputStream previewBaos = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(150, 150)
                .outputFormat(fileExtension)
                .toOutputStream(previewBaos);
        byte[] previewImageData = previewBaos.toByteArray();

        // Upload images to Firebase
        String mainImageUrl = uploadToFirebase(mainImageData, "profile_pictures/" + originalFileName, fileExtension);

        return mainImageUrl;
    }
}
