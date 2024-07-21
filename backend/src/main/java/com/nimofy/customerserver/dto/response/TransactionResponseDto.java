package com.nimofy.customerserver.dto.response;

import com.nimofy.customerserver.model.bet.Bet;

public record TransactionResponseDto(ResponseMessage responseMessage, Bet bet) {}
