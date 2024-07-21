package com.nimofy.customerserver.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.nimofy.customerserver.dto.response.ResponseType.ERROR;
import static com.nimofy.customerserver.dto.response.ResponseType.SUCCESS;

@RequiredArgsConstructor
@Getter
public enum ResponseMessage {

    RESPONSE_SUCCESS(SUCCESS),
    WITHDRAW_TRANSACTION_CREATED(SUCCESS),
    DEPOSIT_TRANSACTION_CREATED(SUCCESS),
    ADMIN_GAME_CREATION(SUCCESS),
    TOAST_GAME_ADDITION(SUCCESS),
    MINIMUM_WITHDRAW_AMOUNT_ERROR(ERROR),
    GENERAL_ERROR(ERROR),
    GAME_LIMIT_EXCEEDED_ERROR(ERROR),
    GAME_IS_ALREADY_LINKED_ERROR(ERROR),
    NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR(ERROR),
    NOT_ENOUGH_BALANCE_ERROR(ERROR),
    MISSING_DURATION_IN_MINUTES_ERROR(ERROR),
    MISSING_TITLE_ERROR(ERROR),
    GAME_ALREADY_FINISHED_ERROR(ERROR),
    INCORRECT_WALLET_ADDRESS(ERROR);

    private final ResponseType responseType;
}