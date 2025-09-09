package com.bezkoder.spring.security.postgresql.controllers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.bezkoder.spring.security.postgresql.payload.request.ResenedVerificationRequest;
import com.bezkoder.spring.security.postgresql.services.EmailServiceImpl;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.spring.security.postgresql.models.ERole;
import com.bezkoder.spring.security.postgresql.models.Role;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.payload.request.LoginRequest;
import com.bezkoder.spring.security.postgresql.payload.request.SignupRequest;
import com.bezkoder.spring.security.postgresql.payload.response.JwtResponse;
import com.bezkoder.spring.security.postgresql.payload.response.MessageResponse;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import com.bezkoder.spring.security.postgresql.repository.UserRepository;
import com.bezkoder.spring.security.postgresql.security.jwt.JwtUtils;
import com.bezkoder.spring.security.postgresql.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;


  @Autowired
  private EmailServiceImpl emailService;
  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;





  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {


    Authentication authentication ;

    try {
      authentication = authenticationManager
              .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
    }catch (org.springframework.security.authentication.DisabledException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Error: Account is deactivated or email not verified.");

    }catch (AuthenticationException e){
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body("Error: Invalid username or password");
    }

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());

    if (optionalUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found.");
    }


    if (optionalUser.isPresent() && !optionalUser.get().isEmailVerified()) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body("Error: Email not verified. Please verify your email before logging in.");
    }


    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);




    List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
        .collect(Collectors.toList());



    return ResponseEntity
        .ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),userDetails.getFirstname(),userDetails.getLastname(), userDetails.getEmail(), roles));
  }






  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    try {
      if (userRepository.existsByUsername(signUpRequest.getUsername())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
      }

      if (userRepository.existsByEmail(signUpRequest.getEmail())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
      }

      // Create new user's account
      User user = new User(signUpRequest.getUsername(),  signUpRequest.getFirstname(), signUpRequest.getLastname(),signUpRequest.getEmail(),
              encoder.encode(signUpRequest.getPassword()));

      Set<String> strRoles = signUpRequest.getRole();
      Set<Role> roles = new HashSet<>();

      if (strRoles == null) {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
      } else {
        strRoles.forEach(role -> {
          switch (role) {
            case "admin":
              Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                      .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
              roles.add(adminRole);
              break;
            case "mod":
              Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                      .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
              roles.add(modRole);
              break;
            default:
              Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                      .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
              roles.add(userRole);
          }
        });
      }
      user.setEmailVerified(false);
      user.setRoles(roles);

      // Generate verification token
      String verificationToken = UUID.randomUUID().toString();
      user.setVerificationToken(verificationToken);
      user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

      userRepository.save(user);
      emailService.sendVerificationEmail(user.getEmail(), verificationToken);
      return ResponseEntity.ok(new MessageResponse("User registered successfully!"));

    } catch (Exception e) {
      // Ici, on attrape toute exception et on retourne 500 (Internal Server Error)
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("An error occurred during registration: " + e.getMessage()));
    }
  }




  public boolean verifyEmail(String token) {
    Optional<User> userOpt = userRepository.findByVerificationToken(token);

    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();

    // Check if token is expired
    if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
      return false;
    }

    // Verify the user
    user.setEmailVerified(true);
    user.setVerificationToken(null);
    user.setVerificationTokenExpiry(null);
    userRepository.save(user);

    return true;
  }


  public void resendVerificationEmail(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty() || userOpt.get().isEmailVerified()) {
      throw new RuntimeException("User not found or already verified");
    }

    User user = userOpt.get();

    // Generate new token
    String newToken = UUID.randomUUID().toString();
    user.setVerificationToken(newToken);
    user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
    userRepository.save(user);

    // Send new verification email
    emailService.sendVerificationEmail(email, newToken);
  }


  @GetMapping("/verify")
  public ResponseEntity<?> verifyEmailController(@RequestParam String token) {
    boolean verified = this.verifyEmail(token);

    if (verified) {
      return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
    } else {
      return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired verification token"));
    }
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<?> resendVerification(@RequestBody ResenedVerificationRequest request) {
    try {
      this.resendVerificationEmail(request.getEmail());
      return ResponseEntity.ok(new MessageResponse("Verification email sent!"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    }
  }


}
