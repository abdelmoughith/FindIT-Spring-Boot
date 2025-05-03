package pack.smartwaste.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.services.AnnonceService;
import pack.smartwaste.services.CustomUserService;

import java.util.Set;

@RestController
@RequestMapping("/save")
@RequiredArgsConstructor
public class PostSaverController {

    private final AnnonceService annonceService;
    private final CustomUserService userService;
    private final JwtUtils jwtUtil;

    @PostMapping("/save/{annonceId}")
    public ResponseEntity<String> saveAnnonce(@RequestHeader("Authorization") String token,
                                              @PathVariable Long annonceId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        Long userId = userService.loadUserByUsername(username).getId();

        boolean success = annonceService.saveAnnonce(userId, annonceId);
        return success ?
                ResponseEntity.ok("Annonce saved!") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or annonce not found.");
    }

    @DeleteMapping("/unsave/{annonceId}")
    public ResponseEntity<String> unsaveAnnonce(@RequestHeader("Authorization") String token,
                                                @PathVariable Long annonceId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        Long userId = userService.loadUserByUsername(username).getId();

        boolean success = annonceService.unsaveAnnonce(userId, annonceId);
        return success ?
                ResponseEntity.ok("Annonce unsaved.") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or annonce not found.");
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSavedAnnonces(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        Long userId = userService.loadUserByUsername(username).getId();

        Set<Annonce> annonces = annonceService.getSavedAnnonces(userId);
        return ResponseEntity.ok(annonces);
    }
}
