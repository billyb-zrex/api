package com.sharefable.api.service;

import com.sharefable.api.auth.UserPrincipal;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthUserEntityService implements UserDetailsService {
    private final UserRepo userRepo;

    @Autowired
    public AuthUserEntityService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        Long userId = Long.valueOf(userIdStr);
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent()) {
            return new UserPrincipal(user.get());
        } else {
            throw new UsernameNotFoundException(userIdStr);
        }
    }
}
