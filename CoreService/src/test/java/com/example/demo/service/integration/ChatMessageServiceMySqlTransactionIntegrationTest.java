package com.example.demo.service.integration;

import com.example.demo.dto.chat.ChatMessageRequest;
import com.example.demo.mapper.Market.ProductMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Market.Product;
import com.example.demo.model.User;
import com.example.demo.model.chat.ChatMessage;
import com.example.demo.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MybatisTest
@Import({
        ChatMessageService.class,
        ChatMessageServiceMySqlTransactionIntegrationTest.MockBeanConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("ChatMessageService MySQL 트랜잭션 통합 테스트")
class ChatMessageServiceMySqlTransactionIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.39")
            .withDatabaseName("haru_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @TestConfiguration
    static class MockBeanConfig {
        @Bean
        ProductMapper productMapper() {
            return mock(ProductMapper.class);
        }

        @Bean
        UserMapper userMapper() {
            return mock(UserMapper.class);
        }

        @Bean
        RedisTemplate<String, Object> redisTemplate() {
            return mock(RedisTemplate.class);
        }

        @Bean("chatChannelTopic")
        ChannelTopic chatChannelTopic() {
            return new ChannelTopic("chat.channel.topic");
        }
    }

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChannelTopic chatChannelTopic;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpSchema() {
        reset(productMapper, userMapper, redisTemplate);

        jdbcTemplate.execute("DROP TABLE IF EXISTS messages");
        jdbcTemplate.execute("DROP TABLE IF EXISTS chatrooms");
        jdbcTemplate.execute("DROP TABLE IF EXISTS products");

        jdbcTemplate.execute("""
                CREATE TABLE products (
                    id BIGINT PRIMARY KEY,
                    email VARCHAR(255) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE chatrooms (
                    chatroom_id INT AUTO_INCREMENT PRIMARY KEY,
                    chatname VARCHAR(255),
                    product_id BIGINT NOT NULL,
                    request_email VARCHAR(255) NOT NULL,
                    last_message VARCHAR(500),
                    last_message_time DATETIME,
                    status VARCHAR(50),
                    created_at DATETIME,
                    updated_at DATETIME
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE messages (
                    message_id INT AUTO_INCREMENT PRIMARY KEY,
                    chatroom_id INT NOT NULL,
                    sender_email VARCHAR(255) NOT NULL,
                    content TEXT NOT NULL,
                    message_type VARCHAR(50) NOT NULL,
                    sent_at DATETIME NOT NULL,
                    is_read BOOLEAN NOT NULL
                )
                """);

        jdbcTemplate.update("INSERT INTO products (id, email) VALUES (?, ?)", 100L, "seller@test.com");
        jdbcTemplate.update("""
                INSERT INTO chatrooms (
                    chatroom_id, chatname, product_id, request_email,
                    last_message, last_message_time, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, NOW(), ?, NOW(), NOW())
                """, 1, "테스트 채팅방", 100L, "buyer@test.com", "초기 메시지", "ACTIVE");

        when(productMapper.findById(100L, "buyer@test.com"))
                .thenReturn(Product.builder().id(100L).email("seller@test.com").title("테스트 상품").build());
        when(userMapper.findByEmail("buyer@test.com"))
                .thenReturn(User.builder().email("buyer@test.com").nickname("구매자").build());
    }

    @Test
    @DisplayName("정상 publish 시 메시지 저장과 last_message 업데이트가 커밋된다")
    void sendMessage_Commit_WhenRedisPublishSucceeds() {
        when(redisTemplate.convertAndSend(eq(chatChannelTopic.getTopic()), any(ChatMessage.class)))
                .thenReturn(1L);

        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatroomId(1)
                .productId(999L)
                .content("통합 테스트 메시지")
                .messageType("TEXT")
                .build();

        ChatMessage result = chatMessageService.sendMessage("buyer@test.com", request);

        Integer messageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE chatroom_id = 1", Integer.class);
        String lastMessage = jdbcTemplate.queryForObject(
                "SELECT last_message FROM chatrooms WHERE chatroom_id = 1", String.class);

        assertThat(result.getMessageId()).isNotNull();
        assertThat(result.getProductId()).isEqualTo(100L);
        assertThat(messageCount).isEqualTo(1);
        assertThat(lastMessage).isEqualTo("통합 테스트 메시지");

        verify(redisTemplate).convertAndSend(eq(chatChannelTopic.getTopic()), any(ChatMessage.class));
    }

    @Test
    @DisplayName("Redis publish 실패 시 메시지/채팅방 업데이트 전체가 롤백된다")
    void sendMessage_Rollback_WhenRedisPublishFails() {
        when(redisTemplate.convertAndSend(eq(chatChannelTopic.getTopic()), any(ChatMessage.class)))
                .thenThrow(new RuntimeException("redis down"));

        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatroomId(1)
                .productId(999L)
                .content("롤백 검증 메시지")
                .messageType("TEXT")
                .build();

        assertThatThrownBy(() -> chatMessageService.sendMessage("buyer@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis 메시지 발행 실패");

        Integer messageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE chatroom_id = 1", Integer.class);
        String lastMessage = jdbcTemplate.queryForObject(
                "SELECT last_message FROM chatrooms WHERE chatroom_id = 1", String.class);

        assertThat(messageCount).isZero();
        assertThat(lastMessage).isEqualTo("초기 메시지");
    }
}
