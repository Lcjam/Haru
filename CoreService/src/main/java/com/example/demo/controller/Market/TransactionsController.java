package com.example.demo.controller.Market;

import com.example.demo.dto.Market.TransactionsRequest;
import com.example.demo.dto.Market.TransactionsResponse;
import com.example.demo.dto.Market.PaymentsRequest;
import com.example.demo.dto.Market.PaymentsResponse;
import com.example.demo.service.Market.TransactionsService;
import com.example.demo.service.Market.PaymentsService;
import com.example.demo.util.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "거래 및 결제 관리", description = "거래 생성, 조회 및 결제 관련 API")
@RestController
@RequestMapping("/api/core/market")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionsService transactionsService;
    private final PaymentsService paymentsService;

    @Operation(
            summary = "거래 생성",
            description = "새로운 거래를 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "거래 생성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "거래 생성 실패 (유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/transactions")
    public ResponseEntity<BaseResponse<TransactionsResponse>> createTransaction(
            @Parameter(description = "거래 생성 요청 정보", required = true)
            @RequestBody TransactionsRequest request) {
        TransactionsResponse response = transactionsService.createTransaction(request);
        return ResponseEntity.ok(new BaseResponse<>(response, "거래가 생성되었습니다."));
    }

    @Operation(
            summary = "거래 상세 조회",
            description = "거래 ID로 거래의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "거래 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "거래 조회 실패 (거래가 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/transactions/{id}")
    public ResponseEntity<BaseResponse<TransactionsResponse>> getTransactionById(
            @Parameter(description = "거래 ID", required = true, example = "1")
            @PathVariable Long id) {
        return transactionsService.getTransactionById(id);
    }

    @Operation(
            summary = "사용자별 거래 내역 조회",
            description = "인증된 사용자의 거래 내역을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "거래 내역 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/transactions/user")
    public ResponseEntity<BaseResponse<List<TransactionsResponse>>> getUserTransactions(
            @Parameter(description = "인증된 사용자 정보 (JWT에서 자동 추출)", hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(new BaseResponse<>(null, "인증되지 않은 사용자입니다."));
        }

        String email = userDetails.getUsername(); // JWT에서 이메일 추출
        return transactionsService.getUserTransactions(email);
    }

    @Operation(
            summary = "결제 요청",
            description = "거래에 대한 결제를 요청합니다. 결제 요청 시 자동으로 결제 상태가 업데이트됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 요청 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "결제 요청 실패 (유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/transactions/payments")
    public ResponseEntity<BaseResponse<PaymentsResponse>> createPayment(
            @Parameter(description = "결제 요청 정보", required = true)
            @RequestBody PaymentsRequest request) {
        // 결제 요청 시 자동으로 결제 상태 업데이트 실행됨
        ResponseEntity<BaseResponse<PaymentsResponse>> response = paymentsService.createPayment(request);
        return response;
    }

    @Operation(
            summary = "거래의 결제 내역 조회",
            description = "특정 거래의 모든 결제 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 내역 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "결제 내역 조회 실패 (거래가 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/transactions/payments/{transactionId}")
    public ResponseEntity<BaseResponse<List<PaymentsResponse>>> getPaymentsByTransaction(
            @Parameter(description = "거래 ID", required = true, example = "1")
            @PathVariable Long transactionId) {
        return paymentsService.getPaymentsByTransaction(transactionId);
    }
}