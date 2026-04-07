package com.example.demo.controller;

import com.example.demo.dto.chat.ChatMessageRequest;
import com.example.demo.mapper.ChatRoomMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.chat.ChatMessage;
import com.example.demo.model.chat.ChatRoom;
import com.example.demo.service.ChatMessageService;
import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.security.Principal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageController 테스트")
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ChatRoomMapper chatRoomMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ChatMessageController chatMessageController;

    @Test
    @DisplayName("processMessage는 메시지 저장 후 Redis 경로만 사용하고 직접 발행하지 않음")
    void processMessage_ShouldDelegateBroadcastToRedisListener() {
        // given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatroomId(10)
                .productId(7L)
                .content("hello")
                .messageType("TEXT")
                .build();

        ChatMessage savedMessage = ChatMessage.builder()
                .messageId(99)
                .chatroomId(10)
                .senderEmail("buyer@example.com")
                .content("hello")
                .messageType("TEXT")
                .build();

        User sender = User.builder()
                .email("buyer@example.com")
                .nickname("구매자")
                .build();

        ChatRoom chatRoom = ChatRoom.builder()
                .chatroomId(10)
                .productId(100L)
                .sellerEmail("seller@example.com")
                .requestEmail("buyer@example.com")
                .build();

        Principal principal = () -> "buyer@example.com";
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();

        when(chatMessageService.sendMessage("buyer@example.com", request)).thenReturn(savedMessage);
        when(userMapper.findByEmail("buyer@example.com")).thenReturn(sender);
        when(chatRoomMapper.findChatRoomById(10, "buyer@example.com")).thenReturn(chatRoom);

        // when
        chatMessageController.processMessage(request, headerAccessor, principal);

        // then
        verify(chatMessageService).sendMessage("buyer@example.com", request);
        verify(notificationService).sendNotification(
                "seller@example.com",
                "[100] 구매자: hello",
                "CHAT_MESSAGE",
                10,
                100L
        );
        verify(userMapper).findByEmail("buyer@example.com");
        verify(chatRoomMapper).findChatRoomById(10, "buyer@example.com");
    }
}
