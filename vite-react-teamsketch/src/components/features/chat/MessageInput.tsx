import React, { useEffect, useState, useRef } from "react";
import { FaPaperclip, FaPaperPlane } from "react-icons/fa";

// RF-003: backend upload contract only accepts image attachments.
// Keep `url` for backward compatibility with existing consumers while making the
// preview intent explicit through `previewUrl`.
export type ImageAttachment = File & { previewUrl: string; url: string };

interface MessageInputProps {
  onSendMessage?: (message: string, imageFile?: ImageAttachment) => void;
  onFocus?: () => void;
  onBlur?: () => void;
}

const MessageInput: React.FC<MessageInputProps> = ({onSendMessage, onFocus, onBlur }) => {
  const [message, setMessage] = useState("");
  const [imageFile, setImageFile] = useState<ImageAttachment | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    return () => {
      if (imageFile) {
        URL.revokeObjectURL(imageFile.previewUrl);
      }
    };
  }, [imageFile]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setMessage(e.target.value);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile?.type.startsWith('image/')) {
      const objectUrl = URL.createObjectURL(selectedFile);
      setImageFile(Object.assign(selectedFile, { previewUrl: objectUrl, url: objectUrl }) as ImageAttachment);
    } else if (selectedFile) {
      setImageFile(null);
    }
    e.target.value = "";
  };

  const handleSend = () => {
    if (!message.trim() && !imageFile) return;
    onSendMessage?.(message, imageFile || undefined);
    setMessage("");
    setImageFile(null);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div>
      {/* 이미지 미리보기 */}
      {imageFile && (
        <div className="relative w-40">
          {imageFile.type.startsWith("image/") && (
            <img
              src={imageFile.previewUrl}
              alt="선택한 이미지 미리보기"
              className="w-full h-32 object-cover rounded-md shadow-md"
            />
          )}

          <button
            onClick={() => {
              setImageFile(null);
              if (fileInputRef.current) {
                fileInputRef.current.value = "";
              }
            }}
            className="absolute top-1 right-1 hover:bg-secondary-light font-bold bg-secondary-light text-secondary-dark text-xs px-2 py-1 rounded-full"
          >
            X
          </button>
        </div>
      )}
      
      {/* 메시지 입력 및 전송 영역 */}
      <div className="flex items-center bg-white shadow-md pb-5 pt-3 border-t border-primary-200 dark:border-border-dark">
        {/* 이미지 선택 버튼 */}
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className="p-2 text-gray-500 hover:text-primary-500 transition-colors"
        >
          <FaPaperclip className="w-5 h-5" />
        </button>
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          className="hidden"
          accept="image/*"
        />

        {/* 메시지 입력창: 텍스트는 WS, 이미지는 REST multipart */}
        <div className="flex-1 ml-2 mr-3 relative">
          <input
            type="text"
            value={message}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            className="w-full p-2 pr-12 border rounded-full"
            placeholder="메시지를 입력하세요..."
            onFocus={onFocus}
            onBlur={onBlur}
          />
          {(message.trim() || imageFile) && (
            <button
              onClick={handleSend}
              className="absolute right-1 top-1/2 -translate-y-1/2 text-white hover:text-primary-600 transition-colors rounded-full p-2"
            >
              <FaPaperPlane className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default MessageInput;
