package com.nimofy.customerserver.service.messaging;

import com.nimofy.customerserver.config.websocket.CustomWebSocketHandler;
import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.converter.UserConverter;
import com.nimofy.customerserver.dto.UpdateType;
import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.user.UserDto;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.bet.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final CustomWebSocketHandler customWebSocketHandler;
    private final BetConverter betConverter;
    private final UserConverter userConverter;
    private final BetRepository betRepository;

    @Async(value = "cryptoTaskExecutor")
    public void messageBetUpdate(Bet bet) {
        switch (bet.getStatus()) {
            case OPENED, CLOSED -> messageUpdate(bet, UpdateType.BET_INFORMATION);
            case RESOLVED -> {
                messageUpdate(bet, UpdateType.BET_INFORMATION);
                messageUpdate(bet, UpdateType.ACCOUNT_INFORMATION);
            }
        }
    }

    @Async(value = "cryptoTaskExecutor")
    public void messageUpdate(Bet bet, UpdateType updateType) {
        switch (updateType) {
            case BET_INFORMATION -> sendBetInformationToParticipants(bet);
            case ACCOUNT_INFORMATION -> sendAccountInformationToBetParticipants(bet);
        }
    }

    public void sendAccountInformationToUser(User user) {
        UserDto userDto = userConverter.convert(user);
        customWebSocketHandler.getWebSocketSessions()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(user.getId()))
                .findFirst()
                .ifPresent(entry -> sendJsonUpdate(entry.getValue(), userDto.toJson()));
    }

    private void sendBetInformationToParticipants(Bet bet) {
        Set<Long> userIdSet = betRepository.findParticipantsIds(bet.getId());
        log.info("sending update to [{}] users for betId [{}]", userIdSet.size(), bet.getId());
        customWebSocketHandler.getWebSocketSessions().entrySet().stream().filter(entry -> userIdSet.contains(entry.getKey())).forEach(entry -> {
            BetDto betUpdateDto = betConverter.convert(bet);
            sendJsonUpdate(entry.getValue(), betUpdateDto.toJson());
        });
    }

    private void sendAccountInformationToBetParticipants(Bet bet) {
        betRepository.fetchBetParticipants(bet.getId()).ifPresent(managedBet -> managedBet.getParticipants()
                .stream()
                .filter(gameParticipant -> customWebSocketHandler.getWebSocketSessions().containsKey(gameParticipant.getId()))
                .forEach(gameParticipant -> {
                    WebSocketSession webSocketSession = customWebSocketHandler.getWebSocketSessions().get(gameParticipant.getId());
                    UserDto userDto = userConverter.convert(gameParticipant);
                    sendJsonUpdate(webSocketSession, userDto.toJson());
                }));
    }

    private void sendJsonUpdate(WebSocketSession webSocketSession, String jsonPayload) {
        try {
            webSocketSession.sendMessage(new TextMessage(jsonPayload));
        } catch (IOException e) {
            log.error("Could not send jsonPayload:[{}]", jsonPayload, e);
        }
    }
}