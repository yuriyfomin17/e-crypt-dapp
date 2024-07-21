package com.nimofy.customerserver.rest;

import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.response.ResponseMessage;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.service.messaging.MessageService;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import com.nimofy.customerserver.service.users.AdminBetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.nimofy.customerserver.dto.response.ResponseMessage.MISSING_TITLE_ERROR;
import static com.nimofy.customerserver.dto.response.ResponseMessage.NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR;

@RestController
@RequestMapping("/v1/all-bet/admin/")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminBetService adminBetService;
    private final MessageService messageService;
    private final BetConverter betConverter;
    private final BigDecimalCalculator bigDecimalCalculator;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/bets")
    public ResponseEntity<ResponseDto> createAdminBet(@RequestBody BetDto betDto) {
        ResponseMessage errorResponseMessage = validateNewBet(betDto);
        if (errorResponseMessage != null) {
            return ResponseEntity.ok(ResponseDto.builder()
                    .responseType(errorResponseMessage)
                    .type(errorResponseMessage.getResponseType())
                    .build()
            );
        }
        return ResponseEntity.ok(adminBetService.createAdminBet(betDto));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/bets/status")
    public ResponseEntity<BetDto> changeAdminBetStatus(@RequestBody BetDto betDto) {
        Bet bet = adminBetService.changeAdminBetStatus(betDto);
        if (bet == null) {
            return ResponseEntity.badRequest().build();
        }
        messageService.messageBetUpdate(bet);
        return ResponseEntity.ok(betConverter.convert(bet));
    }

    private ResponseMessage validateNewBet(BetDto betDto) {
        if (StringUtils.isEmpty(betDto.getTitle())) {
            return MISSING_TITLE_ERROR;
        }
        if (StringUtils.isEmpty(betDto.getEthTicketPrice()) || !NumberUtils.isCreatable(betDto.getEthTicketPrice()) || bigDecimalCalculator.isLessOrEqualZero(bigDecimalCalculator.convertStringToBigDecimal(betDto.getEthTicketPrice()))) {
            return NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR;
        }
        return null;
    }
}