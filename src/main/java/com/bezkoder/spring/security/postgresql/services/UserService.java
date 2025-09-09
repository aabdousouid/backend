package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.User;

import java.util.List;

public interface UserService {
public List<User> findAll ();
    User setActive(Long userId, boolean isActive);
    User activate(Long userId);
    User deactivate(Long userId);

}
