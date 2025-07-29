package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.UserProfileRequest;
import com.bezkoder.spring.security.postgresql.mappers.UserProfileMapper;
import com.bezkoder.spring.security.postgresql.models.EducationHistoryItem;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.models.UserProfile;
import com.bezkoder.spring.security.postgresql.models.WorkHistoryItem;
import com.bezkoder.spring.security.postgresql.repository.UserProfileRepository;
import com.bezkoder.spring.security.postgresql.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final FileStorageService fileStorageService;
    private final Path cvStorageLocation = Paths.get("uploads/ProfileCv").toAbsolutePath().normalize();

    @Override
    public UserProfile addProfile(UserProfileRequest userProfile) {

        User user = userRepository.findById(userProfile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));


        UserProfile profile = userProfileMapper.toEntity(userProfile, user);
        return userProfileRepository.save(profile);
    }




    @Override
    public UserProfile addProfileWithCV(UserProfileRequest userProfile, MultipartFile cvFile) throws IOException {
        User user = userRepository.findById(userProfile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileMapper.toEntity(userProfile, user);

        // Handle CV upload
        if (cvFile != null && !cvFile.isEmpty()) {
            String cvPath = fileStorageService.storeFile(cvFile, "cvs");
            profile.setCvFilePath(cvPath);
        }

        return userProfileRepository.save(profile);
    }


    @Override
    public ResponseEntity<UserProfile> updateProfile(Long profileId, UserProfile userProfile){
        Optional<UserProfile> existingProfileOpt = userProfileRepository.findById(profileId);
        if(existingProfileOpt.isPresent()){
            UserProfile existingProfile =existingProfileOpt.get();
            existingProfile.setAddress(userProfile.getAddress());
            existingProfile.setCvFilePath(userProfile.getCvFilePath());
            existingProfile.setExperienceYears(userProfile.getExperienceYears());
            existingProfile.setPhoneNumber(userProfile.getPhoneNumber());
            existingProfile.setSummary(userProfile.getSummary());
            existingProfile.setTitle(userProfile.getTitle());
            existingProfile.setSkills(userProfile.getSkills());
            existingProfile.setCertifications(userProfile.getCertifications());
            existingProfile.setEducation(userProfile.getEducation());
            existingProfile.setWorkHistory(userProfile.getWorkHistory());
            existingProfile.setLanguages(userProfile.getLanguages());
            existingProfile.setLinks(userProfile.getLinks());

            UserProfile updatedProfile = userProfileRepository.save(existingProfile);
            return ResponseEntity.ok(updatedProfile);

        }
        else{
            return ResponseEntity.notFound().build();
        }
    }


    @Override
    public ResponseEntity<UserProfile> updateProfileWithCV(Long profileId, UserProfile userProfile, MultipartFile cvFile) throws IOException {
        Optional<UserProfile> existingProfileOpt = userProfileRepository.findById(profileId);
        if (existingProfileOpt.isPresent()) {
            UserProfile existingProfile = existingProfileOpt.get();

            // Delete old CV if new one is uploaded
            if (cvFile != null && !cvFile.isEmpty()) {
                if (existingProfile.getCvFilePath() != null) {
                    fileStorageService.deleteFile(existingProfile.getCvFilePath());
                }
                String newCvPath = fileStorageService.storeFile(cvFile, "cvs");
                existingProfile.setCvFilePath(newCvPath);
            }

            updateProfileFields(existingProfile, userProfile);
            UserProfile updatedProfile = userProfileRepository.save(existingProfile);
            return ResponseEntity.ok(updatedProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void updateProfileFields(UserProfile existingProfile, UserProfile userProfile) {
        existingProfile.setAddress(userProfile.getAddress());
        existingProfile.setExperienceYears(userProfile.getExperienceYears());
        existingProfile.setPhoneNumber(userProfile.getPhoneNumber());
        existingProfile.setSummary(userProfile.getSummary());
        existingProfile.setTitle(userProfile.getTitle());
        existingProfile.setSkills(userProfile.getSkills());
        existingProfile.setCertifications(userProfile.getCertifications());
        existingProfile.setEducation(userProfile.getEducation());
        existingProfile.setWorkHistory(userProfile.getWorkHistory());
        existingProfile.setLanguages(userProfile.getLanguages());
        existingProfile.setLinks(userProfile.getLinks());
    }

    @Override
    public Optional<UserProfile> getProfile(Long userProfileId) {
        return userProfileRepository.findById(userProfileId);
    }



    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserProfile> getProfileByUser(Long userId){
        User user = userRepository.findById(userId).orElseThrow(null);
        if(user!=null){

            return userProfileRepository.findById(user.getProfile().getProfileId());
        }
        else return Optional.empty();
    }

    @Override
    public Resource downloadCV(Long profileId) throws IOException {
        Optional<UserProfile> profileOpt = userProfileRepository.findById(profileId);
        if (profileOpt.isPresent() && profileOpt.get().getCvFilePath() != null) {
            Path filePath = fileStorageService.getFilePath(profileOpt.get().getCvFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } else {
            throw new RuntimeException("CV not found!");
        }
    }
}

