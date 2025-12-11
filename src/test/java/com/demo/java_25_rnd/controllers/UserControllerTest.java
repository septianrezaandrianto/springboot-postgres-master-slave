package com.demo.java_25_rnd.controllers;

import com.demo.java_25_rnd.entities.User;
import com.demo.java_25_rnd.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User buildUser(String id) {
        User u = new User();
        u.setId(id);
        u.setName("User " + id);
        u.setAddress("Some address");
        u.setPhoneNumber("08123456789");
        return u;
    }

    @Test
    void getAll_shouldReturnListOfUsers() throws Exception {
        User u1 = buildUser("1");
        User u2 = buildUser("2");

        when(userService.getAll()).thenReturn(Arrays.asList(u1, u2));

        mockMvc.perform(get("/user/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[1].id", is("2")));

        verify(userService, times(1)).getAll();
    }

    @Test
    void getById_whenUserExists_shouldReturnUser() throws Exception {
        User u = buildUser("1");
        when(userService.getById("1")).thenReturn(u);

        mockMvc.perform(get("/user/getById/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("User 1")));

        verify(userService, times(1)).getById("1");
    }

    @Test
    void getById_whenUserNotFound_shouldReturn404() throws Exception {
        when(userService.getById("999")).thenReturn(null);

        mockMvc.perform(get("/user/getById/{id}", "999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getById("999");
    }

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        User input = buildUser(null);
        User saved = buildUser("1");

        when(userService.create(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("User 1")));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    void update_whenUserExists_shouldReturnUpdatedUser() throws Exception {
        String id = "1";
        User input = buildUser(null);
        input.setName("Updated Name");

        User updated = buildUser(id);
        updated.setName("Updated Name");

        when(userService.update(eq(id), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/user/update/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Updated Name")));

        verify(userService, times(1)).update(eq(id), any(User.class));
    }

    @Test
    void update_whenUserNotFound_shouldReturn404() throws Exception {
        String id = "1";
        User input = buildUser(null);

        when(userService.update(eq(id), any(User.class))).thenReturn(null);

        mockMvc.perform(put("/user/update/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).update(eq(id), any(User.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        String id = "1";

        mockMvc.perform(delete("/user/delete/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(id);
    }
}
