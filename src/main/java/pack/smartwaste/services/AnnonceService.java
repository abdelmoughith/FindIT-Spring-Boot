package pack.smartwaste.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.models.user.City;
import pack.smartwaste.models.user.User;
import pack.smartwaste.rep.AnnonceRepository;
import pack.smartwaste.rep.UserRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static pack.smartwaste.Utils.UrlUtils.BASE_URL;

@RequiredArgsConstructor
@Service
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final VarStorageService imageStorageServiceCloud;
    private final CityService cityService;
    private final FastApiService fastApiService;
    private final UserRepository userRepo;

    /* ================= SAFE FIX URL ================= */
    private Annonce fixAnnonceUrl(Annonce annonce) {
        if (annonce == null) return null;

        // Create a shallow copy to avoid mutating the entity
        Annonce copy = new Annonce();
        copy.setId(annonce.getId());
        copy.setTitle(annonce.getTitle());
        copy.setDescription(annonce.getDescription());
        copy.setLocation(annonce.getLocation());
        copy.setStatus(annonce.getStatus());
        copy.setDatePublished(annonce.getDatePublished());
        copy.setCity(annonce.getCity());

        // Fix image URLs
        if (annonce.getImageUrls() != null) {
            List<String> fixedUrls = annonce.getImageUrls().stream()
                    .map(url -> (url != null && !url.startsWith(BASE_URL)) ? BASE_URL + url : url)
                    .collect(Collectors.toList());
            copy.setImageUrls(fixedUrls);
        }

        // Fix user profile URL
        if (annonce.getUser() != null) {
            User user = annonce.getUser();
            User userCopy = new User();
            userCopy.setId(user.getId());
            userCopy.setUsername(user.getUsername());
            userCopy.setEmail(user.getEmail());
            userCopy.setProfileImage((user.getProfileImage() != null && !user.getProfileImage().startsWith(BASE_URL))
                    ? BASE_URL + user.getProfileImage()
                    : user.getProfileImage());
            copy.setUser(userCopy);
        }

        return copy;
    }

    /* ================= SAVE / UNSAVE ================= */

    public boolean saveAnnonce(Long userId, Long annonceId) {
        Optional<User> userOpt = userRepo.findById(userId);
        Optional<Annonce> annonceOpt = annonceRepository.findById(annonceId);

        if (userOpt.isPresent() && annonceOpt.isPresent()) {
            User user = userOpt.get();
            Annonce annonce = annonceOpt.get();
            user.getSavedAnnonces().add(annonce);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    public boolean unsaveAnnonce(Long userId, Long annonceId) {
        Optional<User> userOpt = userRepo.findById(userId);
        Optional<Annonce> annonceOpt = annonceRepository.findById(annonceId);

        if (userOpt.isPresent() && annonceOpt.isPresent()) {
            User user = userOpt.get();
            Annonce annonce = annonceOpt.get();
            user.getSavedAnnonces().remove(annonce);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    public Set<Annonce> getSavedAnnonces(Long userId) {
        return userRepo.findById(userId)
                .map(User::getSavedAnnonces)
                .orElse(Collections.emptySet())
                .stream()
                .map(this::fixAnnonceUrl)
                .collect(Collectors.toSet());
    }

    /* ================= CREATE / READ ================= */

    @Transactional
    public Annonce createAnnonce(Annonce annonce, List<MultipartFile> images, String city) throws IOException {
        if (images != null && !images.isEmpty()) {
            if (images.size() > 4) throw new IllegalArgumentException("You can upload a maximum of 4 images.");

            City cityObject = cityService.getCityByName(city)
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
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

    public Optional<Annonce> getAnnonceById(Long id) {
        return annonceRepository.findById(id)
                .map(this::fixAnnonceUrl);
    }

    public List<Annonce> getAllAnnoncesDescOrByCity(String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (city == null) {
            return annonceRepository.findAllByOrderByDatePublishedDesc(pageable)
                    .getContent().stream()
                    .map(this::fixAnnonceUrl)
                    .toList();
        }
        City relatedCity = cityService.getCityByName(city)
                .orElseThrow(() -> new EntityNotFoundException("City not found"));
        return annonceRepository.findAllByCityOrderByDatePublishedDesc(relatedCity, pageable)
                .getContent().stream()
                .map(this::fixAnnonceUrl)
                .toList();
    }

    public List<Annonce> searchAnnoncesByPattern(String pattern, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (pattern == null || pattern.isEmpty()) throw new EntityNotFoundException("No pattern entered");
        return annonceRepository.findByTitleContainingOrDescriptionContainingOrLocationContaining(pattern, pattern, pattern, pageable)
                .getContent().stream()
                .map(this::fixAnnonceUrl)
                .toList();
    }

    public List<Annonce> getAllAnnoncesByCityAndPattern(String city, String pattern, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (city == null && pattern == null) {
            return annonceRepository.findAllByOrderByDatePublishedDesc(pageable)
                    .getContent().stream().map(this::fixAnnonceUrl).toList();
        }
        if (city == null) return searchAnnoncesByPattern(pattern, page, size).stream().map(this::fixAnnonceUrl).toList();
        if (pattern == null) return getAllAnnoncesDescOrByCity(city, page, size).stream().map(this::fixAnnonceUrl).toList();

        City relatedCity = cityService.getCityByName(city)
                .orElseThrow(() -> new EntityNotFoundException("City not found"));
        return annonceRepository.findByCityAndTitleContainingIgnoreCaseOrCityAndDescriptionContainingIgnoreCaseOrCityAndLocationContainingIgnoreCaseOrderByDatePublishedDesc(
                        relatedCity, pattern, relatedCity, pattern, relatedCity, pattern, pageable)
                .getContent().stream().map(this::fixAnnonceUrl).toList();
    }

    /* ================= UPDATE / DELETE ================= */

    public Annonce updateAnnonce(Long id, Annonce annonceDetails, List<MultipartFile> images) throws IOException {
        return annonceRepository.findById(id).map(existingAnnonce -> {
            existingAnnonce.setTitle(annonceDetails.getTitle());
            existingAnnonce.setDescription(annonceDetails.getDescription());
            existingAnnonce.setLocation(annonceDetails.getLocation());
            existingAnnonce.setStatus(annonceDetails.getStatus());

            if (images != null && !images.isEmpty()) {
                if (images.size() > 4) throw new IllegalArgumentException("You can upload a maximum of 4 images.");

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

    public void deleteAnnonce(Long id) {
        if (!annonceRepository.existsById(id)) throw new EntityNotFoundException("Annonce not found with id " + id);
        annonceRepository.deleteById(id);
    }

    public List<Annonce> getAll() {
        return annonceRepository.findAll().stream()
                .map(this::fixAnnonceUrl)
                .toList();
    }

    public List<Annonce> getAnnoncesByUser(User user) {
        return annonceRepository.findAllByUserOrderByDatePublishedDesc(user).stream()
                .map(this::fixAnnonceUrl)
                .toList();
    }

    public List<Annonce> findAnnoncesByImageUrlsOrdered(List<String> orderedUrls) {
        List<Annonce> rawAnnonces = annonceRepository.findByImageUrls(orderedUrls);

        Map<String, Integer> urlIndexMap = new HashMap<>();
        for (int i = 0; i < orderedUrls.size(); i++) {
            urlIndexMap.put(orderedUrls.get(i), i);
        }

        rawAnnonces.sort(Comparator.comparingInt(a -> a.getImageUrls().stream()
                .filter(urlIndexMap::containsKey)
                .mapToInt(urlIndexMap::get)
                .min()
                .orElse(Integer.MAX_VALUE)
        ));

        return rawAnnonces.stream().map(this::fixAnnonceUrl).toList();
    }
}
