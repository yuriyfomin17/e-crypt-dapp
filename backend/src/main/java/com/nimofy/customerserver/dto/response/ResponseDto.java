package com.nimofy.customerserver.dto.response;

import com.nimofy.customerserver.dto.bet.BetDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import static com.nimofy.customerserver.dto.response.ResponseMessage.GENERAL_ERROR;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {

    private ResponseMessage responseType;
    private ResponseType type;
    private BetDto betDto;

    public static ResponseDto createErrorResponse() {
        return ResponseDto.builder()
                .responseType(GENERAL_ERROR)
                .type(ResponseType.ERROR)
                .build();
    }

    public static ResponseEntity<ResponseDto> createOkResponse(ResponseMessage responseType) {
        return ResponseEntity.ok(
                ResponseDto.builder().responseType(responseType).type(responseType.getResponseType()).build()
        );
    }
}