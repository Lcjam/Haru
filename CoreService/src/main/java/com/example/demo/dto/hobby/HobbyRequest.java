package com.example.demo.dto.hobby;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "취미 요청 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HobbyRequest {
    @Schema(description = "취미 ID", example = "1", required = true)
    private Long hobbyId;
    
    @Schema(description = "카테고리 ID", example = "1", required = true)
    private Long categoryId;
}
