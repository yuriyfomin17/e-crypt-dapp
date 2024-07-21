package com.nimofy.customerserver.rest;

import com.nimofy.customerserver.dto.UpdateType;
import com.nimofy.customerserver.dto.bet.BetTransactionDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.response.TransactionResponseDto;
import com.nimofy.customerserver.service.messaging.MessageService;
import com.nimofy.customerserver.service.users.UserBetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/all-bet/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserBetService userBetService;
    private final MessageService messageService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/bets/{userId}/{betId}")
    public ResponseEntity<ResponseDto> addNewBet(@PathVariable Long userId, @PathVariable Long betId) {
        return ResponseEntity.ok(userBetService.linkGameToUser(userId, betId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create/transaction")
    public ResponseEntity<ResponseDto> createBetTransactionGetUserBalance(@RequestBody BetTransactionDto betTransactionDto) {
        TransactionResponseDto transactionResponseDto = userBetService.isTransactionCreated(betTransactionDto);
        if (transactionResponseDto.bet() != null) {
            messageService.messageUpdate(transactionResponseDto.bet(), UpdateType.BET_INFORMATION);
        }
        return ResponseEntity.ok(
                ResponseDto.builder().responseType(transactionResponseDto.responseMessage())
                        .type(transactionResponseDto.responseMessage().getResponseType())
                        .build()
        );
    }
}