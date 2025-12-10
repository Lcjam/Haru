import Select from '../../common/Select';
import { useAppSelector } from '../../../store/hooks';

interface InterestSelectProps {
  onInterestSelect: (categoryId: number) => void;
  selectedCategory?: number;
  categories?: Array<{
    categoryId: number;
    categoryName: string;
  }>;
}

const InterestSelect: React.FC<InterestSelectProps> = ({ onInterestSelect, selectedCategory }) => {
  const { categories, loading, error } = useAppSelector((state) => state.category);
  
  // 디버깅용 로그
  console.log('InterestSelect - categories:', categories);
  console.log('InterestSelect - loading:', loading);
  console.log('InterestSelect - error:', error);
  
  const handleCategorySelect = (value: string) => {
    onInterestSelect(Number(value));
  };

  // 카테고리가 없을 때 처리
  if (loading) {
    return (
      <Select
        options={[]}
        onChange={handleCategorySelect}
        className="w-full bg-primary-300 text-text-light dark:bg-gray-800"
        placeholder="로딩 중..."
        value=""
        disabled
      />
    );
  }

  if (error) {
    console.error('InterestSelect 에러:', error);
  }

  return (
    <Select
      options={categories.map((category) => ({
        value: category.categoryId.toString(),
        label: category.categoryName
      }))}
      onChange={handleCategorySelect}
      className="w-full bg-primary-300 text-text-light dark:bg-gray-800"
      placeholder={error ? "카테고리 로딩 실패" : "관심사를 선택해주세요"}
      value={selectedCategory ? selectedCategory.toString() : ''}
    />
  );
};

export default InterestSelect;
