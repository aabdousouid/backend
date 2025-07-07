package com.bezkoder.spring.security.postgresql.mappers;

import com.bezkoder.spring.security.postgresql.dto.UserProfileRequest;
import com.bezkoder.spring.security.postgresql.models.EducationHistoryItem;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.models.UserProfile;
import com.bezkoder.spring.security.postgresql.models.WorkHistoryItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserProfileMapper {
    public UserProfile toEntity(UserProfileRequest dto, User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setTitle(dto.getTitle());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setAddress(dto.getAddress());
        profile.setLinks(dto.getLinks());
        profile.setSummary(dto.getSummary());
        profile.setSkills(dto.getSkills());
        profile.setExperienceYears(dto.getExperienceYears());
        profile.setLanguages(dto.getLanguages());
        profile.setCertifications(dto.getCertifications());
        profile.setCvFilePath(dto.getCvFilePath());

        // Work history
        if (dto.getWorkHistory() != null) {
            profile.setWorkHistory(dto.getWorkHistory().stream()
                    .map(w -> new WorkHistoryItem(w.getCompany(), w.getTitle(), w.getDuration(), w.getDescription()))
                    .collect(Collectors.toList()));
        }

        // Education
        if (dto.getEducation() != null) {
            profile.setEducation(dto.getEducation().stream()
                    .map(e -> new EducationHistoryItem(e.getDegree(), e.getSchool(), e.getDuration()))
                    .collect(Collectors.toList()));
        }

        return profile;
    }
}
