package com.example.demo.listener;

import com.example.demo.model.chat.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisMessageListener 테스트")
class RedisMessageListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RedisMessageListener redisMessageListener;

    @Test
    @DisplayName("Redis 메시지 수신 시 /topic/room.{chatroomId}로 WebSocket 발행")
    void onMessage_ShouldSendToRoomTopic() throws Exception {
        // given
        ChatMessage chatMessage = ChatMessage.builder()
                .chatroomId(42)
                .senderEmail("sender@example.com")
                .content("hello")
                .messageType("TEXT")
                .build();
        String message = "{\"chatroomId\":42,\"senderEmail\":\"sender@example.com\",\"content\":\"hello\",\"messageType\":\"TEXT\"}";
        org.mockito.Mockito.when(objectMapper.readValue(message, ChatMessage.class)).thenReturn(chatMessage);

        // when
        redisMessageListener.onMessage(message);

        // then
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/room.42");
        assertThat(messageCaptor.getValue().getChatroomId()).isEqualTo(42);
        assertThat(messageCaptor.getValue().getSenderEmail()).isEqualTo("sender@example.com");
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("hello");
    }
}
