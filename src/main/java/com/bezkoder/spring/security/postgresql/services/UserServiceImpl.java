package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{


    private final UserRepository userRepository;


    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }
    @Override
    @Transactional
    public User setActive(Long userId, boolean isActive) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        u.setActive(isActive);
        return userRepository.save(u);
    }

    @Override
    @Transactional
    public User activate(Long userId) {
        return setActive(userId, true);
    }

    @Override
    @Transactional
    public User deactivate(Long userId) {
        return setActive(userId, false);
    }

}
