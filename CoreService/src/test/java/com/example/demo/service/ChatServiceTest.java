package com.example.demo.service;

import com.example.demo.dto.chat.ChatRoomRequest;
import com.example.demo.mapper.ChatRoomMapper;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.Market.ProductMapper;
import com.example.demo.mapper.Market.ProductRequestMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.Market.ProductImageMapper;
import com.example.demo.model.Market.Product;
import com.example.demo.model.Market.ProductRequest;
import com.example.demo.model.User;
import com.example.demo.model.chat.ChatRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService 테스트")
class ChatServiceTest {

    @Mock
    private ChatRoomMapper chatRoomMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private ProductRequestMapper productRequestMapper;

    @InjectMocks
    private ChatService chatService;

    private Product product;
    private User seller;
    private User buyer;
    private ChatRoom chatRoom;
    private ChatRoomRequest chatRoomRequest;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setTitle("테스트 상품");
        product.setEmail("seller@example.com");
        product.setVisible(true);

        seller = User.builder()
                .email("seller@example.com")
                .nickname("판매자")
                .build();

        buyer = User.builder()
                .email("buyer@example.com")
                .nickname("구매자")
                .build();

        chatRoom = ChatRoom.builder()
                .chatroomId(1)
                .chatname("테스트 채팅방")
                .productId(1L)
                .sellerEmail("seller@example.com")
                .requestEmail("buyer@example.com")
                .lastMessage("채팅이 시작되었습니다.")
                .lastMessageTime(LocalDateTime.now())
                .status("ACTIVE")
                .build();

        chatRoomRequest = new ChatRoomRequest();
        chatRoomRequest.setProductId(1L);
        chatRoomRequest.setChatname("테스트 채팅방");

        productRequest = new ProductRequest();
        productRequest.setProductId(1L);
        productRequest.setRequesterEmail("buyer@example.com");
        productRequest.setApprovalStatus("승인");
    }

    @Test
    @DisplayName("채팅방 생성 성공 - 새 채팅방")
    void createOrGetChatRoom_Success_NewRoom() {
        // given
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);
        when(chatRoomMapper.findChatRoomByProductAndBuyer(1L, "buyer@example.com"))
                .thenReturn(null);
        doNothing().when(chatRoomMapper).createChatRoom(any(ChatRoom.class));
        when(userMapper.findByEmail("seller@example.com")).thenReturn(seller);
        when(userMapper.findByEmail("buyer@example.com")).thenReturn(buyer);
        when(productImageMapper.findByProductId(1L)).thenReturn(Collections.emptyList());

        // when
        var result = chatService.createOrGetChatRoom("buyer@example.com", chatRoomRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChatroomId()).isNotNull();
        verify(chatRoomMapper, times(1)).createChatRoom(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 성공 - 기존 채팅방 조회")
    void createOrGetChatRoom_Success_ExistingRoom() {
        // given
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);
        when(chatRoomMapper.findChatRoomByProductAndBuyer(1L, "buyer@example.com"))
                .thenReturn(chatRoom);
        when(userMapper.findByEmail("seller@example.com")).thenReturn(seller);
        when(userMapper.findByEmail("buyer@example.com")).thenReturn(buyer);
        when(productImageMapper.findByProductId(1L)).thenReturn(Collections.emptyList());

        // when
        var result = chatService.createOrGetChatRoom("buyer@example.com", chatRoomRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChatroomId()).isEqualTo(1);
        verify(chatRoomMapper, never()).createChatRoom(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 상품이 없음")
    void createOrGetChatRoom_Fail_ProductNotFound() {
        // given
        when(productMapper.findById(999L, "buyer@example.com")).thenReturn(null);
        chatRoomRequest.setProductId(999L);

        // when
        var result = chatService.createOrGetChatRoom("buyer@example.com", chatRoomRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("존재하지 않는 상품");
        verify(chatRoomMapper, never()).createChatRoom(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 모집 완료된 상품에 승인되지 않은 사용자")
    void createOrGetChatRoom_Fail_NotApproved() {
        // given
        product.setVisible(false);
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);
        when(productRequestMapper.findByProductIdAndRequesterEmail(1L, "buyer@example.com"))
                .thenReturn(null);

        // when
        var result = chatService.createOrGetChatRoom("buyer@example.com", chatRoomRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("모집이 완료된 상품");
        verify(chatRoomMapper, never()).createChatRoom(any(ChatRoom.class));
    }

    @Test
    @DisplayName("사용자의 채팅방 목록 조회 성공")
    void getChatRoomsByUser_Success() {
        // given
        List<ChatRoom> chatRooms = Arrays.asList(chatRoom);
        when(chatRoomMapper.findChatRoomsByUser("buyer@example.com")).thenReturn(chatRooms);
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);
        when(userMapper.findByEmail("seller@example.com")).thenReturn(seller);
        when(productImageMapper.findByProductId(1L)).thenReturn(Collections.emptyList());
        when(chatMessageMapper.countUnreadMessages(1, "buyer@example.com")).thenReturn(0);

        // when
        var result = chatService.getChatRoomsByUser("buyer@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChatRooms()).isNotNull();
        verify(chatRoomMapper, times(1)).findChatRoomsByUser("buyer@example.com");
    }

    @Test
    @DisplayName("채팅방 상세 정보 조회 성공")
    void getChatRoomDetail_Success() {
        // given
        when(chatRoomMapper.findChatRoomById(1, "buyer@example.com")).thenReturn(chatRoom);
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);
        when(userMapper.findByEmail("seller@example.com")).thenReturn(seller);
        when(productImageMapper.findByProductId(1L)).thenReturn(Collections.emptyList());
        doNothing().when(chatMessageMapper).updateMessageReadStatus(1, "buyer@example.com");

        // when
        var result = chatService.getChatRoomDetail("buyer@example.com", 1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChatroomId()).isEqualTo(1);
        verify(chatMessageMapper, times(1)).updateMessageReadStatus(1, "buyer@example.com");
    }

    @Test
    @DisplayName("채팅방 상세 정보 조회 실패 - 채팅방이 없음")
    void getChatRoomDetail_Fail_ChatRoomNotFound() {
        // given
        when(chatRoomMapper.findChatRoomById(999, "buyer@example.com")).thenReturn(null);

        // when
        var result = chatService.getChatRoomDetail("buyer@example.com", 999);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("존재하지 않는 채팅방");
    }

    @Test
    @DisplayName("채팅방 상세 정보 조회 실패 - 접근 권한 없음")
    void getChatRoomDetail_Fail_NoAccess() {
        // given
        ChatRoom otherChatRoom = ChatRoom.builder()
                .chatroomId(1)
                .productId(1L)
                .sellerEmail("other@example.com")
                .requestEmail("other2@example.com")
                .build();
        when(chatRoomMapper.findChatRoomById(1, "buyer@example.com")).thenReturn(otherChatRoom);
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);

        // when
        var result = chatService.getChatRoomDetail("buyer@example.com", 1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("접근 권한이 없습니다");
    }

    @Test
    @DisplayName("활성 채팅방 목록 조회 성공")
    void getActiveChatRoomsByUser_Success() {
        // given
        List<ChatRoom> chatRooms = Arrays.asList(chatRoom);
        when(chatRoomMapper.findActiveChatRoomsByUser("buyer@example.com")).thenReturn(chatRooms);
        when(productMapper.findById(1L, "buyer@example.com")).thenReturn(product);
        when(userMapper.findByEmail("seller@example.com")).thenReturn(seller);
        when(productImageMapper.findByProductId(1L)).thenReturn(Collections.emptyList());
        when(chatMessageMapper.countUnreadMessages(1, "buyer@example.com")).thenReturn(0);

        // when
        var result = chatService.getActiveChatRoomsByUser("buyer@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getChatRooms()).isNotNull();
        verify(chatRoomMapper, times(1)).findActiveChatRoomsByUser("buyer@example.com");
    }
}
