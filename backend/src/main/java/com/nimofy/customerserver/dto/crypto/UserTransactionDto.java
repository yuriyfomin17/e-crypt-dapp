package com.nimofy.customerserver.dto.crypto;

import com.nimofy.customerserver.model.transaction.crypto.State;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTransactionDto {
    private Long id;
    private Long userId;
    private String percentage;
    private String signature;
    private String ethAmount;
    private String address;
    private String hash;
    private State state;
    private Type type;
    private LocalDateTime createdAt;
}