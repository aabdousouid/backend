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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfile addProfile(UserProfileRequest userProfile) {

        User user = userRepository.findById(userProfile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));


        UserProfile profile = userProfileMapper.toEntity(userProfile, user);
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
        else return null;
    }
}

