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

  // 카테고리가 비어있고 에러가 있으면 에러 메시지 표시
  const hasError = error && categories.length === 0;

  return (
    <Select
      options={categories.map((category) => ({
        value: category.categoryId.toString(),
        label: category.categoryName
      }))}
      onChange={handleCategorySelect}
      className="w-full bg-primary-300 text-text-light dark:bg-gray-800"
      placeholder={
        hasError 
          ? error.length > 40 
            ? `${error.substring(0, 40)}...` 
            : error
          : categories.length === 0 
            ? "로딩 중..." 
            : "관심사를 선택해주세요"
      }
      value={selectedCategory ? selectedCategory.toString() : ''}
      disabled={hasError}
    />
  );
};

export default InterestSelect;
