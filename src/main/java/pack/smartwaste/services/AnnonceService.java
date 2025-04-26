package pack.smartwaste.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pack.smartwaste.RequestsEntities.ImageRequest;
import pack.smartwaste.RequestsEntities.ImageResponse;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.models.user.City;
import pack.smartwaste.rep.AnnonceRepository;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class AnnonceService {
    private final AnnonceRepository annonceRepository;
    private final ImageStorageServiceCloud imageStorageServiceCloud;
    private final CityService cityService;
    private final FastApiService fastApiService;

    // Create
    @Transactional
    public Annonce createAnnonce(
            Annonce annonce,
            List<MultipartFile> images,
            String city
    ) throws IOException {
        if (images != null && !images.isEmpty()) {
            if (images.size() > 4) {
                throw new IllegalArgumentException("You can upload a maximum of 4 images.");
            }

            City cityObject = cityService.getCityByName(city)
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
            System.out.println(cityObject.getId() + " the name is " + cityObject.getVille());
            annonce.setCity(cityObject);

            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                String imageUrl = imageStorageServiceCloud.saveImage(image);
                imageUrls.add(imageUrl);
            }
            annonce.setImageUrls(imageUrls);

        }
        return annonceRepository.save(annonce);
    }
    // Read (Get by ID)
    public Optional<Annonce> getAnnonceById(Long id) {
        return annonceRepository.findById(id);
    }

    public List<Annonce> searchAnnoncesByPattern(String pattern, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (pattern == null || pattern.isEmpty()) {
            throw new EntityNotFoundException("No pattern entered");
        }
        return annonceRepository.findByTitleContainingOrDescriptionContainingOrLocationContaining(pattern, pattern, pattern, pageable).getContent();

    }
    // Read (Get all)
    public List<Annonce> getAllAnnoncesDescOrByCity(String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (city == null) {
            return annonceRepository.findAllByOrderByDatePublishedDesc(pageable).getContent();
        }
        City relatedCity = cityService.getCityByName(city).orElseThrow(
                () -> new EntityNotFoundException("City not found")
        );
        return annonceRepository.findAllByCityOrderByDatePublishedDesc(relatedCity, pageable).getContent();
    }

    public List<Annonce> getAllAnnoncesByCityAndPattern(String city, String pattern, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (city == null && pattern == null){
            return annonceRepository.findAllByOrderByDatePublishedDesc(pageable).getContent();
        }
        if (city == null) { // look for pattern only
            return searchAnnoncesByPattern(pattern, page, size);
        }
        if (pattern == null) { // look for pattern only
            return getAllAnnoncesDescOrByCity(city, page, size);
        }
        // city != null && pattern != null

        City relatedCity = cityService.getCityByName(city).orElseThrow(
                () -> new EntityNotFoundException("City not found")
        );
        if (pattern == null || pattern.isEmpty()) {
            throw new EntityNotFoundException("No pattern entered");
        }
        return annonceRepository.findByCityAndTitleContainingIgnoreCaseOrCityAndDescriptionContainingIgnoreCaseOrCityAndLocationContainingIgnoreCaseOrderByDatePublishedDesc(
                relatedCity,
                pattern,
                relatedCity,
                pattern,
                relatedCity,
                pattern,
                pageable
        ).getContent();
    }


    // Update
    public Annonce updateAnnonce(Long id, Annonce annonceDetails, List<MultipartFile> images) throws IOException {
        return annonceRepository.findById(id).map(existingAnnonce -> {
            existingAnnonce.setTitle(annonceDetails.getTitle());
            existingAnnonce.setDescription(annonceDetails.getDescription());
            existingAnnonce.setLocation(annonceDetails.getLocation());
            existingAnnonce.setStatus(annonceDetails.getStatus());

            // If new images are uploaded, update them
            if (images != null && !images.isEmpty()) {
                if (images.size() > 4) {
                    throw new IllegalArgumentException("You can upload a maximum of 4 images.");
                }

                List<String> imageUrls = new ArrayList<>();
                try {
                    for (MultipartFile image : images) {
                        String imageUrl = imageStorageServiceCloud.saveImage(image);
                        imageUrls.add(imageUrl);
                    }
                    existingAnnonce.setImageUrls(imageUrls);
                } catch (IOException e) {
                    throw new RuntimeException("Error saving image", e);
                }
            }

            return annonceRepository.save(existingAnnonce);
        }).orElseThrow(() -> new RuntimeException("Annonce not found with id " + id));
    }


    // Delete
    public void deleteAnnonce(Long id) {
        if (!annonceRepository.existsById(id)) {
            throw new EntityNotFoundException("Annonce not found with id " + id);
        }
        annonceRepository.deleteById(id);
    }
    public List<Annonce> getAll() {
        return annonceRepository.findAll();
    }

    public List<Annonce> findAnnoncesByImageUrls(List<String> imageUrls) {
        return annonceRepository.findByImageUrls(imageUrls);
    }
    public List<Annonce> findAnnoncesByImageUrlsOrdered(List<String> orderedUrls) {
        List<Annonce> rawAnnonces = annonceRepository.findByImageUrls(orderedUrls);

        // Map each image URL to its position
        Map<String, Integer> urlIndexMap = new HashMap<>();
        for (int i = 0; i < orderedUrls.size(); i++) {
            urlIndexMap.put(orderedUrls.get(i), i);
        }

        // Sort annonces by the first matching image URL in the provided list
        rawAnnonces.sort(Comparator.comparingInt(a -> {
            OptionalInt minIndex = a.getImageUrls().stream()
                    .filter(urlIndexMap::containsKey)
                    .mapToInt(urlIndexMap::get)
                    .min();
            return minIndex.orElse(Integer.MAX_VALUE);
        }));

        return rawAnnonces;
    }



}
