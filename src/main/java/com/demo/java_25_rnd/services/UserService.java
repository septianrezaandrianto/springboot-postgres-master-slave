package com.demo.java_25_rnd.services;

import com.demo.java_25_rnd.entities.User;
import com.demo.java_25_rnd.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User update(String id, User user) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(user.getName());
        existing.setAddress(user.getAddress());
        existing.setPhoneNumber(user.getPhoneNumber());
        return userRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        userRepository.deleteById(id);
    }
}
