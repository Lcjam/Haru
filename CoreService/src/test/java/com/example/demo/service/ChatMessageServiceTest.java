package com.example.demo.service;

import com.example.demo.dto.chat.ChatMessageRequest;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.ChatRoomMapper;
import com.example.demo.mapper.Market.ProductMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Market.Product;
import com.example.demo.model.User;
import com.example.demo.model.chat.ChatMessage;
import com.example.demo.model.chat.ChatRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageService 테스트")
class ChatMessageServiceTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private ChatRoomMapper chatRoomMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ChannelTopic chatChannelTopic;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatRoom chatRoom;
    private Product product;
    private User sender;
    private ChatMessageRequest request;

    @BeforeEach
    void setUp() {
        chatRoom = ChatRoom.builder()
                .chatroomId(1)
                .productId(100L)
                .requestEmail("buyer@example.com")
                .sellerEmail("seller@example.com")
                .build();

        product = Product.builder()
                .id(100L)
                .email("seller@example.com")
                .title("테스트 상품")
                .build();

        sender = User.builder()
                .email("buyer@example.com")
                .nickname("구매자")
                .build();

        request = ChatMessageRequest.builder()
                .chatroomId(1)
                .productId(999L)
                .content("안녕하세요")
                .messageType("TEXT")
                .build();

        when(chatChannelTopic.getTopic()).thenReturn("chat.channel.topic");
    }

    @Test
    @DisplayName("sendMessage 성공 - Redis publish까지 정상 수행")
    void sendMessage_Success_WhenRedisPublishSucceeds() {
        // given
        when(chatRoomMapper.findChatRoomById(1, "buyer@example.com")).thenReturn(chatRoom);
        when(productMapper.findById(100L, "buyer@example.com")).thenReturn(product);
        when(userMapper.findByEmail("buyer@example.com")).thenReturn(sender);
        when(redisTemplate.convertAndSend(eq("chat.channel.topic"), any(ChatMessage.class)))
                .thenReturn(1L);

        // when
        ChatMessage result = chatMessageService.sendMessage("buyer@example.com", request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChatroomId()).isEqualTo(1);
        assertThat(result.getSenderEmail()).isEqualTo("buyer@example.com");
        assertThat(result.getProductId()).isEqualTo(100L);
        assertThat(result.getContent()).isEqualTo("안녕하세요");
        assertThat(result.getSenderName()).isEqualTo("구매자");
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageMapper).saveChatMessage(messageCaptor.capture());
        verify(chatRoomMapper).updateChatRoomLastMessage(eq(1), eq("안녕하세요"), any());
        verify(redisTemplate).convertAndSend(eq("chat.channel.topic"), any(ChatMessage.class));
        assertThat(messageCaptor.getValue().getProductId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("sendMessage 실패 - Redis publish 예외 시 RuntimeException 전파")
    void sendMessage_Fail_WhenRedisPublishThrows() {
        // given
        when(chatRoomMapper.findChatRoomById(1, "buyer@example.com")).thenReturn(chatRoom);
        when(productMapper.findById(100L, "buyer@example.com")).thenReturn(product);
        when(userMapper.findByEmail("buyer@example.com")).thenReturn(sender);
        when(redisTemplate.convertAndSend(eq("chat.channel.topic"), any(ChatMessage.class)))
                .thenThrow(new RuntimeException("redis down"));

        // when & then
        assertThatThrownBy(() -> chatMessageService.sendMessage("buyer@example.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis 메시지 발행 실패")
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("redis down");

        verify(chatMessageMapper).saveChatMessage(any(ChatMessage.class));
        verify(chatRoomMapper).updateChatRoomLastMessage(eq(1), eq("안녕하세요"), any());
        verify(redisTemplate).convertAndSend(eq("chat.channel.topic"), any(ChatMessage.class));
    }
}
