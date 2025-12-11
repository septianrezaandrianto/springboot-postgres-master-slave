package com.demo.java_25_rnd.services;

import com.demo.java_25_rnd.entities.User;
import com.demo.java_25_rnd.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User buildUser(String id) {
        User u = new User();
        u.setId(id);
        u.setName("User " + id);
        u.setAddress("Some address");
        u.setPhoneNumber("08123456789");
        return u;
    }

    @Test
    void getAll_shouldReturnAllUsersFromRepository() {
        User u1 = buildUser("1");
        User u2 = buildUser("2");
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<User> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals("1", result.getFirst().getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getById_whenUserExists_shouldReturnUser() {
        User u = buildUser("1");
        when(userRepository.findById("1")).thenReturn(Optional.of(u));

        User result = userService.getById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(userRepository, times(1)).findById("1");
    }

    @Test
    void getById_whenUserDoesNotExist_shouldReturnNull() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        User result = userService.getById("999");

        assertNull(result);
        verify(userRepository, times(1)).findById("999");
    }

    @Test
    void create_shouldSaveAndReturnUser() {
        User input = buildUser(null);
        User saved = buildUser("1");
        when(userRepository.save(input)).thenReturn(saved);

        User result = userService.create(input);

        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(userRepository, times(1)).save(input);
    }

    @Test
    void update_whenUserExists_shouldUpdateAndReturnUser() {
        String id = "1";
        User existing = buildUser(id);
        existing.setName("Old Name");

        User input = new User();
        input.setName("New Name");
        input.setAddress("New Address");
        input.setPhoneNumber("0899999999");

        User saved = buildUser(id);
        saved.setName("New Name");
        saved.setAddress("New Address");
        saved.setPhoneNumber("0899999999");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(saved);


        User result = userService.update(id, input);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Address", result.getAddress());
        assertEquals("0899999999", result.getPhoneNumber());

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void update_whenUserDoesNotExist_shouldReturnNull() {
        String id = "1";
        User input = buildUser(id);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.update(id, input);

        assertNull(result);
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_shouldCallDeleteById() {
        String id = "1";

        userService.delete(id);

        verify(userRepository, times(1)).deleteById(id);
    }
}
