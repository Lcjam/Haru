-- ============================================
-- Haru 프로젝트 DB 초기 설정 스크립트
-- ============================================

-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS haru_db;
USE haru_db;

-- 2. 사용자 생성 및 권한 부여 (로컬 개발용)
CREATE USER IF NOT EXISTS 'user'@'localhost' IDENTIFIED BY 'user';
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'user';
GRANT ALL PRIVILEGES ON haru_db.* TO 'user'@'localhost';
GRANT ALL PRIVILEGES ON haru_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

-- ============================================
-- 기본 테이블 생성 (외래키 의존성 순서)
-- ============================================

-- 3. Users 테이블 (가장 기본)
CREATE TABLE IF NOT EXISTS Users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15),
    nickname VARCHAR(50) UNIQUE NOT NULL,
    hobby TEXT,
    bio TEXT,
    login_method ENUM('EMAIL', 'SOCIAL') NOT NULL,
    social_provider ENUM('GOOGLE', 'KAKAO', 'NAVER', 'NONE') DEFAULT 'NONE',
    account_status ENUM('Active', 'Deactivated', 'Dormant', 'Withdrawal') NOT NULL DEFAULT 'Active',
    authority ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    signup_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_time DATETIME NULL,
    login_failed_attempts INT DEFAULT 0,
    login_is_locked BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Categories 테이블
CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL UNIQUE COMMENT '카테고리명'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Hobbies 테이블
CREATE TABLE IF NOT EXISTS hobbies (
    hobby_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    hobby_name VARCHAR(255) NOT NULL UNIQUE COMMENT '취미명'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Category_Hobbies 테이블 (관계 테이블)
CREATE TABLE IF NOT EXISTS category_hobbies (
    category_id BIGINT NOT NULL COMMENT '카테고리 ID',
    hobby_id BIGINT NOT NULL COMMENT '취미 ID',
    PRIMARY KEY (category_id, hobby_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (hobby_id) REFERENCES hobbies(hobby_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. User_Hobbies 테이블
CREATE TABLE IF NOT EXISTS user_hobbies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL COMMENT '사용자 이메일',
    hobby_id BIGINT NOT NULL COMMENT '취미 ID',
    category_id BIGINT NOT NULL COMMENT '선택한 카테고리 ID',
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (hobby_id) REFERENCES hobbies(hobby_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY unique_user_hobby (email, hobby_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Products 테이블
CREATE TABLE IF NOT EXISTS Products (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(36) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    hobby_id BIGINT NOT NULL,
    transaction_type ENUM('대면', '비대면') NOT NULL,
    registration_type ENUM('구매', '판매', '구매 요청', '판매 요청') NOT NULL,
    max_participants INT NOT NULL DEFAULT 1,
    current_participants INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    days VARCHAR(255) NOT NULL,
    start_date DATETIME NULL,
    end_date DATETIME NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    meeting_place VARCHAR(255) NULL,
    address VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT,
    FOREIGN KEY (hobby_id) REFERENCES hobbies(hobby_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. ProductImages 테이블
CREATE TABLE IF NOT EXISTS ProductImages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Boards 테이블
CREATE TABLE IF NOT EXISTS boards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image_path VARCHAR(255),
    host_email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (host_email) REFERENCES Users(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Posts 테이블
CREATE TABLE IF NOT EXISTS Posts (
    post_id INT PRIMARY KEY AUTO_INCREMENT,
    post_title VARCHAR(255) NOT NULL,
    post_content TEXT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    post_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    post_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    email VARCHAR(255) NOT NULL,
    board_id BIGINT,
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Comments 테이블
CREATE TABLE IF NOT EXISTS Comments (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    parent_comment_id INT NULL,
    comment_content TEXT NOT NULL,
    comment_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    comment_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    post_id INT,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES Comments(comment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Chatrooms 테이블
CREATE TABLE IF NOT EXISTS chatrooms (
    chatroom_id INT AUTO_INCREMENT PRIMARY KEY,
    chatname VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    request_email VARCHAR(255) NOT NULL,
    last_message TEXT,
    last_message_time DATETIME,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    FOREIGN KEY (request_email) REFERENCES Users(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. Messages 테이블
CREATE TABLE IF NOT EXISTS messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    chatroom_id INT NOT NULL,
    sender_email VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (chatroom_id) REFERENCES chatrooms(chatroom_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_email) REFERENCES Users(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. Transactions 테이블
CREATE TABLE IF NOT EXISTS Transactions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_email VARCHAR(255) NOT NULL,
    seller_email VARCHAR(255) NOT NULL,
    transaction_status ENUM('진행중', '완료', '취소') DEFAULT '진행중',
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('완료', '미완료') DEFAULT '미완료',
    price INT NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_email) REFERENCES Users(email) ON DELETE CASCADE,
    FOREIGN KEY (seller_email) REFERENCES Users(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. Payments 테이블
CREATE TABLE IF NOT EXISTS Payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    amount INT NOT NULL,
    payment_method ENUM('포인트', '카드', '계좌이체') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES Transactions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. UserLocation 테이블
CREATE TABLE IF NOT EXISTS UserLocation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    location_name VARCHAR(255),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 18. Notices 테이블
CREATE TABLE IF NOT EXISTS Notices (
    notice_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notice_message TEXT NOT NULL,
    notice_type ENUM('TRADE', 'CHAT', 'BOARD') NOT NULL,
    notice_status ENUM('UNREAD', 'READ') NOT NULL,
    notice_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 19. UnreadNotices 테이블
CREATE TABLE IF NOT EXISTS UnreadNotices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (notice_id) REFERENCES Notices(notice_id) ON DELETE CASCADE,
    FOREIGN KEY (email) REFERENCES Users(email) ON DELETE CASCADE,
    UNIQUE KEY unique_unread_notice (notice_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 초기 데이터 삽입 (선택사항)
-- ============================================

-- 샘플 카테고리 데이터 (필요시)
-- INSERT INTO categories (category_name) VALUES 
-- ('운동'), ('문화'), ('취미'), ('기타');

-- 샘플 취미 데이터 (필요시)
-- INSERT INTO hobbies (hobby_name) VALUES 
-- ('축구'), ('독서'), ('요리'), ('사진');

-- ============================================
-- 완료 메시지
-- ============================================
SELECT 'Database setup completed successfully!' AS Status;

