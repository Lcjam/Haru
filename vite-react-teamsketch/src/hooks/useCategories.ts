import { useEffect, useRef } from 'react';
import { useAppDispatch, useAppSelector } from '../store/hooks';
import {
  setCategories,
  setLoading,
  setError,
  setConstantCategories,
  setConstantHobbies
} from '../store/slices/categorySlice';
import { axiosInstance } from '../services/api/axiosInstance';
import { apiConfig } from '../services/api/apiConfig';

export const useCategories = () => {
  const dispatch = useAppDispatch();
  const { categories, selectedCategoryId, loading, error, constantCategories, constantHobbies } =
    useAppSelector((state) => state.category);
  
  // 에러 상태를 추적하기 위한 ref (취미 에러가 카테고리 에러를 덮어쓰지 않도록)
  const categoryErrorRef = useRef<string | null>(null);

  const fetchCategories = async () => {
    try {
      dispatch(setLoading(true));
      const response = await axiosInstance.get(apiConfig.endpoints.core.getCategory);
      console.log('카테고리 API 응답:', response.data); // 디버깅용
      if (response.data.status === 'success') {
        dispatch(setCategories(response.data.data));
        dispatch(setConstantCategories(response.data.data));
        console.log('카테고리 데이터 저장 완료:', response.data.data); // 디버깅용
        categoryErrorRef.current = null; // 성공 시 에러 초기화
      } else {
        console.error('카테고리 API 응답 오류:', response.data);
        const errorMessage = response.data.message || '카테고리 로딩 중 오류가 발생했습니다.';
        categoryErrorRef.current = errorMessage;
        dispatch(setError(errorMessage));
      }
    } catch (error: any) {
      console.error('카테고리 API 호출 실패:', error);
      console.error('에러 상세:', error.response?.data || error.message);
      
      // 사용자 친화적인 에러 메시지 생성
      let errorMessage = '카테고리 로딩 중 오류가 발생했습니다.';
      
      if (error.response) {
        // HTTP 응답이 있는 경우 (4xx, 5xx)
        const status = error.response.status;
        if (status === 401) {
          errorMessage = '인증이 필요합니다. 페이지를 새로고침해주세요.';
        } else if (status === 403) {
          errorMessage = '접근 권한이 없습니다.';
        } else if (status === 404) {
          errorMessage = '카테고리 정보를 찾을 수 없습니다.';
        } else if (status >= 500) {
          errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
        } else {
          errorMessage = error.response.data?.message || `서버 오류 (${status})가 발생했습니다.`;
        }
      } else if (error.request) {
        // 요청은 보냈지만 응답을 받지 못한 경우 (네트워크 에러)
        errorMessage = '서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.';
      } else {
        // 요청 설정 중 에러가 발생한 경우
        errorMessage = error.message || '카테고리 로딩 중 오류가 발생했습니다.';
      }
      
      categoryErrorRef.current = errorMessage;
      dispatch(setError(errorMessage));
    } finally {
      dispatch(setLoading(false));
    }
  };

  // const fetchHobbiesByCategory = async (categoryId: number) => {
  //   const response = await axiosInstance.get(
  //     apiConfig.endpoints.core.getHobbiesByCategory(categoryId)
  //   );
  //   return response.data.data;
  // };

  const fetchAllHobbies = async () => {
    try {
      // 카테고리 로딩이 완료된 후에만 로딩 상태 변경 (중복 방지)
      const response = await axiosInstance.get(apiConfig.endpoints.core.getHobbies);
      console.log('취미 API 응답:', response.data); // 디버깅용
      if (response.data.status === 'success') {
        dispatch(setConstantHobbies(response.data.data));
        console.log('취미 데이터 저장 완료:', response.data.data); // 디버깅용
      } else {
        console.error('취미 API 응답 오류:', response.data);
        // 취미 로딩 실패는 카테고리 에러로 덮어쓰지 않음
        if (!categoryErrorRef.current) {
          dispatch(setError(response.data.message || '취미 로딩 중 오류가 발생했습니다.'));
        }
      }
    } catch (error: any) {
      console.error('취미 API 호출 실패:', error);
      console.error('에러 상세:', error.response?.data || error.message);
      // 취미 로딩 실패는 카테고리 에러로 덮어쓰지 않음
      // 카테고리 에러가 이미 있으면 취미 에러는 무시
    }
  };

  useEffect(() => {
    const initializeData = async () => {
      await fetchCategories();
      await fetchAllHobbies();
    };

    initializeData();
  }, []);

  // 재시도 함수 추가
  const retry = () => {
    dispatch(setError(null));
    categoryErrorRef.current = null;
    fetchCategories();
  };

  return {
    categories,
    selectedCategoryId,
    loading,
    error,
    constantCategories,
    constantHobbies,
    retry
  };
};
