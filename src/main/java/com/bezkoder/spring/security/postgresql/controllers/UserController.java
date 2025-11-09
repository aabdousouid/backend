package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.dto.UserProfileRequest;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.models.UserProfile;
import com.bezkoder.spring.security.postgresql.repository.UserProfileRepository;
import com.bezkoder.spring.security.postgresql.services.ProfileServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor
@RequestMapping("/api/profile")
public class UserController {
    private final ProfileServiceImpl profileService;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/addProfile")
    public ResponseEntity<UserProfile> addProfile(@RequestBody UserProfileRequest userProfile) throws IOException {

        try {
            UserProfile savedProfile = profileService.addProfile(userProfile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping(value = "/add-with-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfile> addProfileWithCV(
            @RequestParam("profile") String profileJson,
            @RequestParam(value = "cv", required = false) MultipartFile cvFile) {
        try {
            UserProfileRequest userProfile = objectMapper.readValue(profileJson, UserProfileRequest.class);
            UserProfile savedProfile = profileService.addProfileWithCV(userProfile, cvFile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



    @PutMapping(value = "/{profileId}/update-with-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfile> updateProfileWithCV(
            @PathVariable Long profileId,
            @RequestParam("profile") String profileJson,
            @RequestParam(value = "cv", required = false) MultipartFile cvFile) {
        try {
            UserProfile userProfile = objectMapper.readValue(profileJson, UserProfile.class);
            return profileService.updateProfileWithCV(profileId, userProfile, cvFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/getProfile/{id}")
    public Optional<UserProfile> getProfile(@PathVariable Long id){
        return this.profileService.getProfile(id);
    }

    @GetMapping("/getUser/{id}")
    public Optional<User> getUser(@PathVariable Long id){
        return this.profileService.getUserById(id);
    }


    @PutMapping("/updateProfile/{id}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable Long id, @RequestBody UserProfile userProfile){
        return this.profileService.updateProfile(id,userProfile);
    }

    @GetMapping("/getProfileByUser/{userId}")
    public UserProfile getProfileByUser(@PathVariable Long userId){
        User user = this.profileService.getUserById(userId).orElseThrow(null);
        if(user!=null){

            return user.getProfile();
        }
        else return null;
    }


    @PostMapping("/profiles/{userId}/upload-cv")
    public ResponseEntity<String> uploadCv(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        Optional<UserProfile> userProfileOpt = profileService.getProfileByUser( userId);
        if (!userProfileOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        UserProfile profile = userProfileOpt.get();

        try {
            // Save file to a directory
            String uploadDir = "uploads/cvs/";
            String fileName = userId + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            file.transferTo(new File(filePath));

            // Save path in profile
            profile.setCvFilePath(filePath);
            userProfileRepository.save(profile);

            return ResponseEntity.ok("CV uploaded successfully: " + filePath);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Could not upload file");
        }
    }

    @GetMapping("/profiles/{userId}/download-cv")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long userId) {
        Optional<UserProfile> userProfileOpt = profileService.getProfileByUser(userId);
        if (!userProfileOpt.isPresent() || userProfileOpt.get().getCvFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        String filePath = userProfileOpt.get().getCvFilePath();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }

    @GetMapping("/{profileId}/download-cv")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long profileId) {
        try {
            Resource resource = profileService.downloadCV(profileId);

            // Get the original filename for the download
            String filename = "cv.pdf"; // Default filename
            Optional<UserProfile> profile = profileService.getProfile(profileId);
            if (profile.isPresent() && profile.get().getCvFilePath() != null) {
                String cvPath = profile.get().getCvFilePath();
                filename = cvPath.substring(cvPath.lastIndexOf("/") + 1);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }





}
