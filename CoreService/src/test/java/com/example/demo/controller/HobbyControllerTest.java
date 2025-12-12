package com.example.demo.controller;

import com.example.demo.dto.hobby.HobbyRequest;
import com.example.demo.model.Category;
import com.example.demo.model.Hobby;
import com.example.demo.model.UserHobby;
import com.example.demo.service.HobbyService;
import com.example.demo.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HobbyController.class)
@DisplayName("HobbyController 테스트")
class HobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HobbyService hobbyService;

    @MockBean
    private TokenUtils tokenUtils;

    private Hobby hobby1;
    private Hobby hobby2;
    private Category category1;
    private List<Hobby> hobbies;
    private List<Category> categories;
    private List<UserHobby> userHobbies;
    private List<HobbyRequest> hobbyRequests;

    @BeforeEach
    void setUp() {
        category1 = Category.builder()
                .categoryId(1L)
                .categoryName("스포츠")
                .build();

        hobby1 = Hobby.builder()
                .hobbyId(1L)
                .hobbyName("축구")
                .categories(Arrays.asList(category1))
                .build();

        hobby2 = Hobby.builder()
                .hobbyId(2L)
                .hobbyName("피아노")
                .categories(Collections.emptyList())
                .build();

        hobbies = Arrays.asList(hobby1, hobby2);
        categories = Arrays.asList(category1);
        userHobbies = Collections.emptyList();

        HobbyRequest hobbyRequest1 = HobbyRequest.builder()
                .hobbyId(1L)
                .categoryId(1L)
                .build();
        hobbyRequests = Arrays.asList(hobbyRequest1);
    }

    @Test
    @DisplayName("모든 취미 목록 조회 성공 - 200 OK")
    void getAllHobbies_Success() throws Exception {
        // given
        when(hobbyService.getAllHobbiesWithCategories()).thenReturn(hobbies);

        // when & then
        mockMvc.perform(get("/api/core/hobbies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].hobbyId").value(1L))
                .andExpect(jsonPath("$.data[0].hobbyName").value("축구"));
        verify(hobbyService, times(1)).getAllHobbiesWithCategories();
    }

    @Test
    @DisplayName("모든 취미 목록 조회 (카테고리 정보 미포함) 성공 - 200 OK")
    void getAllHobbiesSimple_Success() throws Exception {
        // given
        when(hobbyService.getAllHobbies()).thenReturn(hobbies);

        // when & then
        mockMvc.perform(get("/api/core/hobbies/simple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
        verify(hobbyService, times(1)).getAllHobbies();
    }

    @Test
    @DisplayName("모든 카테고리 목록 조회 성공 - 200 OK")
    void getAllCategories_Success() throws Exception {
        // given
        when(hobbyService.getAllCategories()).thenReturn(categories);

        // when & then
        mockMvc.perform(get("/api/core/hobbies/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].categoryId").value(1L))
                .andExpect(jsonPath("$.data[0].categoryName").value("스포츠"));
        verify(hobbyService, times(1)).getAllCategories();
    }

    @Test
    @DisplayName("특정 카테고리에 속한 취미 목록 조회 성공 - 200 OK")
    void getHobbiesByCategoryId_Success() throws Exception {
        // given
        List<Hobby> categoryHobbies = Arrays.asList(hobby1);
        when(hobbyService.getHobbiesByCategoryId(1L)).thenReturn(categoryHobbies);

        // when & then
        mockMvc.perform(get("/api/core/hobbies/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].hobbyId").value(1L));
        verify(hobbyService, times(1)).getHobbiesByCategoryId(1L);
    }

    @Test
    @DisplayName("사용자의 취미 목록 조회 성공 - 200 OK")
    void getUserHobbies_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader("Bearer testToken")).thenReturn("test@example.com");
        when(hobbyService.getUserHobbies("test@example.com")).thenReturn(userHobbies);

        // when & then
        mockMvc.perform(get("/api/core/hobbies/user")
                        .header("Authorization", "Bearer testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(hobbyService, times(1)).getUserHobbies("test@example.com");
    }

    @Test
    @DisplayName("사용자의 취미 목록 조회 실패 - 인증 실패 - 401 Unauthorized")
    void getUserHobbies_Fail_Unauthorized() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader("Bearer invalidToken")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/core/hobbies/user")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
        verify(hobbyService, never()).getUserHobbies(anyString());
    }

    @Test
    @DisplayName("사용자의 취미 등록/수정 성공 - 200 OK")
    void updateUserHobbies_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader("Bearer testToken")).thenReturn("test@example.com");
        doNothing().when(hobbyService).registerUserHobbies(anyString(), anyList());

        // when & then
        mockMvc.perform(post("/api/core/hobbies/user")
                        .header("Authorization", "Bearer testToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hobbyRequests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.message").value("취미 정보가 업데이트되었습니다."));
        verify(hobbyService, times(1)).registerUserHobbies("test@example.com", hobbyRequests);
    }

    @Test
    @DisplayName("사용자의 취미 등록/수정 실패 - 인증 실패 - 401 Unauthorized")
    void updateUserHobbies_Fail_Unauthorized() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader("Bearer invalidToken")).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/core/hobbies/user")
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hobbyRequests)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
        verify(hobbyService, never()).registerUserHobbies(anyString(), anyList());
    }

    @Test
    @DisplayName("사용자의 취미 등록/수정 실패 - 유효하지 않은 취미 정보 - 400 Bad Request")
    void updateUserHobbies_Fail_InvalidHobby() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader("Bearer testToken")).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("해당 취미가 카테고리에 속하지 않습니다."))
                .when(hobbyService).registerUserHobbies(anyString(), anyList());

        // when & then
        mockMvc.perform(post("/api/core/hobbies/user")
                        .header("Authorization", "Bearer testToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hobbyRequests)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
        verify(hobbyService, times(1)).registerUserHobbies("test@example.com", hobbyRequests);
    }
}
