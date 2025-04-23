package pack.smartwaste.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pack.smartwaste.config.JwtUtils;
import pack.smartwaste.models.user.AuthResponseDto;
import pack.smartwaste.models.user.LoginDto;
import pack.smartwaste.models.user.User;
import pack.smartwaste.services.CustomUserService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserService customUserService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserController(CustomUserService customUserService) {
        this.customUserService = customUserService;
    }

    @PostMapping("/check")
    public Boolean checkUser(@RequestBody Map<String, String> map) {
        return customUserService.loadUserByUsername(map.get("username")) != null
                && customUserService.loadUserByUsername(map.get("username")) != null;
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User registeredUser = customUserService.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto){
        User userFound = customUserService.loadUserByUsername(loginDto.getUsernameOrEmail());
        if (userFound == null) {
            return new ResponseEntity<>("No user associated with this username",HttpStatus.BAD_REQUEST);
        }
        // we have now successfully get the username, Now I need to check the password
        if (!passwordEncoder.matches(loginDto.getPassword(), userFound.getPassword())) {
            return new ResponseEntity<>("Incorrect password",HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userFound.getUsername(),
                        loginDto.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();


        //01 - Receive the token from AuthService
        String token = jwtUtils.generateToken(user);


        AuthResponseDto authResponseDto = new AuthResponseDto();
        authResponseDto.setAccessToken(token);
        return new ResponseEntity<>(authResponseDto, HttpStatus.OK);
    }
    @GetMapping("")
    public String home() {
        return "Welcome to FINDIT";
    }
}
