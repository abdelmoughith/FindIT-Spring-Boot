package pack.smartwaste.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.user.User;
import pack.smartwaste.models.user.UserResponseDTO;

@RestController
@RequestMapping("/user")
public class UserInfoController {

    private final JwtUtils jwtUtils;

    public UserInfoController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> helloAdmin(){
        return ResponseEntity.ok("Hello Admin");
    }

    @PreAuthorize("hasAuthority('VIEWER')")
    @GetMapping("/user")
    public ResponseEntity<String> helloUser(){
        return ResponseEntity.ok("Hello User");
    }

    @GetMapping("/details")
    public ResponseEntity<?> getDetails(
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String authHeader
    ){

        if (user == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.replace("Bearer ", "").trim();
        String usernameFromToken = jwtUtils.extractUsername(token);

        if (!user.getUsername().equals(usernameFromToken)) {
            return new ResponseEntity<>("Unauthorized access: You can only retrieve your own details", HttpStatus.UNAUTHORIZED);
        }


        UserResponseDTO responseDTO = new UserResponseDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
