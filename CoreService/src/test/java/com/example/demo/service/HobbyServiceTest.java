package com.example.demo.service;

import com.example.demo.dto.hobby.HobbyRequest;
import com.example.demo.mapper.HobbyMapper;
import com.example.demo.model.Category;
import com.example.demo.model.Hobby;
import com.example.demo.model.UserHobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HobbyService 테스트")
class HobbyServiceTest {

    @Mock
    private HobbyMapper hobbyMapper;

    @InjectMocks
    private HobbyService hobbyService;

    private Hobby hobby1;
    private Hobby hobby2;
    private Category category1;
    private Category category2;
    private UserHobby userHobby;

    @BeforeEach
    void setUp() {
        category1 = Category.builder()
                .categoryId(1L)
                .categoryName("스포츠")
                .build();

        category2 = Category.builder()
                .categoryId(2L)
                .categoryName("음악")
                .build();

        hobby1 = Hobby.builder()
                .hobbyId(1L)
                .hobbyName("축구")
                .categories(Arrays.asList(category1))
                .build();

        hobby2 = Hobby.builder()
                .hobbyId(2L)
                .hobbyName("피아노")
                .categories(Arrays.asList(category2))
                .build();

        userHobby = UserHobby.builder()
                .email("test@example.com")
                .hobbyId(1L)
                .categoryId(1L)
                .build();
    }

    @Test
    @DisplayName("모든 취미 목록 조회 성공")
    void getAllHobbies_Success() {
        // given
        List<Hobby> expectedHobbies = Arrays.asList(hobby1, hobby2);
        when(hobbyMapper.getAllHobbies()).thenReturn(expectedHobbies);

        // when
        List<Hobby> result = hobbyService.getAllHobbies();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(hobby1, hobby2);
        verify(hobbyMapper, times(1)).getAllHobbies();
    }

    @Test
    @DisplayName("모든 취미 목록 조회 - 빈 리스트")
    void getAllHobbies_EmptyList() {
        // given
        when(hobbyMapper.getAllHobbies()).thenReturn(Collections.emptyList());

        // when
        List<Hobby> result = hobbyService.getAllHobbies();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(hobbyMapper, times(1)).getAllHobbies();
    }

    @Test
    @DisplayName("모든 취미 목록 조회 (카테고리 정보 포함) 성공")
    void getAllHobbiesWithCategories_Success() {
        // given
        List<Hobby> hobbies = Arrays.asList(hobby1, hobby2);
        when(hobbyMapper.getAllHobbies()).thenReturn(hobbies);
        when(hobbyMapper.getCategoriesByHobbyId(1L)).thenReturn(Arrays.asList(category1));
        when(hobbyMapper.getCategoriesByHobbyId(2L)).thenReturn(Arrays.asList(category2));

        // when
        List<Hobby> result = hobbyService.getAllHobbiesWithCategories();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategories()).isNotNull();
        assertThat(result.get(0).getCategories()).hasSize(1);
        assertThat(result.get(1).getCategories()).isNotNull();
        assertThat(result.get(1).getCategories()).hasSize(1);
        verify(hobbyMapper, times(1)).getAllHobbies();
        verify(hobbyMapper, times(1)).getCategoriesByHobbyId(1L);
        verify(hobbyMapper, times(1)).getCategoriesByHobbyId(2L);
    }

    @Test
    @DisplayName("취미 ID로 취미 조회 성공")
    void getHobbyById_Success() {
        // given
        when(hobbyMapper.getHobbyById(1L)).thenReturn(hobby1);

        // when
        Hobby result = hobbyService.getHobbyById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHobbyId()).isEqualTo(1L);
        assertThat(result.getHobbyName()).isEqualTo("축구");
        verify(hobbyMapper, times(1)).getHobbyById(1L);
    }

    @Test
    @DisplayName("취미 ID로 취미 조회 - 존재하지 않는 ID")
    void getHobbyById_NotFound() {
        // given
        when(hobbyMapper.getHobbyById(999L)).thenReturn(null);

        // when
        Hobby result = hobbyService.getHobbyById(999L);

        // then
        assertThat(result).isNull();
        verify(hobbyMapper, times(1)).getHobbyById(999L);
    }

    @Test
    @DisplayName("카테고리 ID로 취미 목록 조회 성공")
    void getHobbiesByCategoryId_Success() {
        // given
        List<Hobby> expectedHobbies = Arrays.asList(hobby1);
        when(hobbyMapper.getHobbiesByCategoryId(1L)).thenReturn(expectedHobbies);

        // when
        List<Hobby> result = hobbyService.getHobbiesByCategoryId(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHobbyId()).isEqualTo(1L);
        verify(hobbyMapper, times(1)).getHobbiesByCategoryId(1L);
    }

    @Test
    @DisplayName("모든 카테고리 목록 조회 성공")
    void getAllCategories_Success() {
        // given
        List<Category> expectedCategories = Arrays.asList(category1, category2);
        when(hobbyMapper.getAllCategories()).thenReturn(expectedCategories);

        // when
        List<Category> result = hobbyService.getAllCategories();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(category1, category2);
        verify(hobbyMapper, times(1)).getAllCategories();
    }

    @Test
    @DisplayName("사용자의 취미 목록 조회 성공")
    void getUserHobbies_Success() {
        // given
        String email = "test@example.com";
        List<UserHobby> expectedUserHobbies = Arrays.asList(userHobby);
        when(hobbyMapper.getUserHobbies(email)).thenReturn(expectedUserHobbies);

        // when
        List<UserHobby> result = hobbyService.getUserHobbies(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo(email);
        verify(hobbyMapper, times(1)).getUserHobbies(email);
    }

    @Test
    @DisplayName("사용자 취미 등록 성공")
    void registerUserHobby_Success() {
        // given
        String email = "test@example.com";
        Long hobbyId = 1L;
        Long categoryId = 1L;
        when(hobbyMapper.isHobbyInCategory(hobbyId, categoryId)).thenReturn(true);
        doNothing().when(hobbyMapper).insertUserHobby(any(UserHobby.class));

        // when
        hobbyService.registerUserHobby(email, hobbyId, categoryId);

        // then
        verify(hobbyMapper, times(1)).isHobbyInCategory(hobbyId, categoryId);
        verify(hobbyMapper, times(1)).insertUserHobby(any(UserHobby.class));
    }

    @Test
    @DisplayName("사용자 취미 등록 실패 - 취미가 카테고리에 속하지 않음")
    void registerUserHobby_Fail_InvalidCategory() {
        // given
        String email = "test@example.com";
        Long hobbyId = 1L;
        Long categoryId = 2L; // 잘못된 카테고리
        when(hobbyMapper.isHobbyInCategory(hobbyId, categoryId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> hobbyService.registerUserHobby(email, hobbyId, categoryId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("속하지 않습니다");
        verify(hobbyMapper, times(1)).isHobbyInCategory(hobbyId, categoryId);
        verify(hobbyMapper, never()).insertUserHobby(any(UserHobby.class));
    }

    @Test
    @DisplayName("사용자 여러 취미 등록 성공")
    void registerUserHobbies_Success() {
        // given
        String email = "test@example.com";
        HobbyRequest hobbyRequest1 = HobbyRequest.builder()
                .hobbyId(1L)
                .categoryId(1L)
                .build();
        HobbyRequest hobbyRequest2 = HobbyRequest.builder()
                .hobbyId(2L)
                .categoryId(2L)
                .build();
        List<HobbyRequest> hobbies = Arrays.asList(hobbyRequest1, hobbyRequest2);

        when(hobbyMapper.isHobbyInCategory(1L, 1L)).thenReturn(true);
        when(hobbyMapper.isHobbyInCategory(2L, 2L)).thenReturn(true);
        doNothing().when(hobbyMapper).deleteAllUserHobbies(email);
        doNothing().when(hobbyMapper).insertUserHobby(any(UserHobby.class));

        // when
        hobbyService.registerUserHobbies(email, hobbies);

        // then
        verify(hobbyMapper, times(1)).deleteAllUserHobbies(email);
        verify(hobbyMapper, times(2)).isHobbyInCategory(anyLong(), anyLong());
        verify(hobbyMapper, times(2)).insertUserHobby(any(UserHobby.class));
    }

    @Test
    @DisplayName("사용자 여러 취미 등록 - 빈 리스트")
    void registerUserHobbies_EmptyList() {
        // given
        String email = "test@example.com";
        List<HobbyRequest> hobbies = new ArrayList<>();

        // when
        hobbyService.registerUserHobbies(email, hobbies);

        // then
        verify(hobbyMapper, never()).deleteAllUserHobbies(anyString());
        verify(hobbyMapper, never()).insertUserHobby(any(UserHobby.class));
    }

    @Test
    @DisplayName("사용자 여러 취미 등록 - null 리스트")
    void registerUserHobbies_NullList() {
        // given
        String email = "test@example.com";

        // when
        hobbyService.registerUserHobbies(email, null);

        // then
        verify(hobbyMapper, never()).deleteAllUserHobbies(anyString());
        verify(hobbyMapper, never()).insertUserHobby(any(UserHobby.class));
    }

    @Test
    @DisplayName("사용자 여러 취미 등록 - categoryId가 null인 경우 스킵")
    void registerUserHobbies_SkipNullCategoryId() {
        // given
        String email = "test@example.com";
        HobbyRequest hobbyRequest = HobbyRequest.builder()
                .hobbyId(1L)
                .categoryId(null)
                .build();
        List<HobbyRequest> hobbies = Arrays.asList(hobbyRequest);

        doNothing().when(hobbyMapper).deleteAllUserHobbies(email);

        // when
        hobbyService.registerUserHobbies(email, hobbies);

        // then
        verify(hobbyMapper, times(1)).deleteAllUserHobbies(email);
        verify(hobbyMapper, never()).isHobbyInCategory(anyLong(), anyLong());
        verify(hobbyMapper, never()).insertUserHobby(any(UserHobby.class));
    }

    @Test
    @DisplayName("취미 ID 유효성 검증 성공")
    void isValidHobby_Success() {
        // given
        when(hobbyMapper.getHobbyById(1L)).thenReturn(hobby1);

        // when
        boolean result = hobbyService.isValidHobby(1L);

        // then
        assertThat(result).isTrue();
        verify(hobbyMapper, times(1)).getHobbyById(1L);
    }

    @Test
    @DisplayName("취미 ID 유효성 검증 실패")
    void isValidHobby_Fail() {
        // given
        when(hobbyMapper.getHobbyById(999L)).thenReturn(null);

        // when
        boolean result = hobbyService.isValidHobby(999L);

        // then
        assertThat(result).isFalse();
        verify(hobbyMapper, times(1)).getHobbyById(999L);
    }

    @Test
    @DisplayName("카테고리 ID 유효성 검증 성공")
    void isValidCategory_Success() {
        // given
        List<Category> categories = Arrays.asList(category1, category2);
        when(hobbyMapper.getAllCategories()).thenReturn(categories);

        // when
        boolean result = hobbyService.isValidCategory(1L);

        // then
        assertThat(result).isTrue();
        verify(hobbyMapper, times(1)).getAllCategories();
    }

    @Test
    @DisplayName("카테고리 ID 유효성 검증 실패")
    void isValidCategory_Fail() {
        // given
        List<Category> categories = Arrays.asList(category1, category2);
        when(hobbyMapper.getAllCategories()).thenReturn(categories);

        // when
        boolean result = hobbyService.isValidCategory(999L);

        // then
        assertThat(result).isFalse();
        verify(hobbyMapper, times(1)).getAllCategories();
    }
}

