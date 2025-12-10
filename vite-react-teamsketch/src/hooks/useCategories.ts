import { useEffect } from 'react';
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

  const fetchCategories = async () => {
    try {
      dispatch(setLoading(true));
      const response = await axiosInstance.get(apiConfig.endpoints.core.getCategory);
      console.log('카테고리 API 응답:', response.data); // 디버깅용
      if (response.data.status === 'success') {
        dispatch(setCategories(response.data.data));
        dispatch(setConstantCategories(response.data.data));
        console.log('카테고리 데이터 저장 완료:', response.data.data); // 디버깅용
      } else {
        console.error('카테고리 API 응답 오류:', response.data);
        dispatch(setError(response.data.message || '카테고리 로딩 중 오류가 발생했습니다.'));
      }
    } catch (error: any) {
      console.error('카테고리 API 호출 실패:', error);
      console.error('에러 상세:', error.response?.data || error.message);
      dispatch(
        setError(error.response?.data?.message || error.message || '카테고리 로딩 중 오류가 발생했습니다.')
      );
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
        if (!error) {
          dispatch(setError(response.data.message || '취미 로딩 중 오류가 발생했습니다.'));
        }
      }
    } catch (error: any) {
      console.error('취미 API 호출 실패:', error);
      console.error('에러 상세:', error.response?.data || error.message);
      // 취미 로딩 실패는 카테고리 에러로 덮어쓰지 않음
      // dispatch(setError(...)) 제거 - 카테고리 에러만 표시
    }
  };

  useEffect(() => {
    const initializeData = async () => {
      await fetchCategories();
      await fetchAllHobbies();
    };

    initializeData();
  }, []);

  return {
    categories,
    selectedCategoryId,
    loading,
    error,
    constantCategories,
    constantHobbies
  };
};
