package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.UserProfileRequest;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.models.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;


public interface ProfileService {

    public UserProfile addProfile (UserProfileRequest userProfile) throws IOException;

    public Optional<UserProfile> getProfile(Long userProfileId );

    public Optional<User> getUserById(Long id);

    public ResponseEntity<UserProfile> updateProfile(Long profileId, UserProfile Profile);


}
