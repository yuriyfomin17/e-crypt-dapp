package com.nimofy.customerserver.converter.testutil;

import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.bet.BetTransactionDto;
import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.bet.BetStatus;
import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.model.transaction.crypto.State;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import com.nimofy.customerserver.model.user.Authority;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.bet.BetRepository;
import com.nimofy.customerserver.repository.transaction.bet.BetTransactionRepository;
import com.nimofy.customerserver.repository.transaction.crypto.CryptoTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.auth.JwtService;
import com.nimofy.customerserver.service.users.UserBetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class TestUtilService {

    public static final String LOCALHOST = "http://localhost:";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final BigDecimal INITIAL_USER_BALANCE = BigDecimal.ONE;

    private static final Random RANDOM = new Random();
    private static final Long USER_LIMIT_PER_TEST = 1000L;

    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BetRepository betRepository;
    @Autowired
    BetConverter betConverter;
    @Autowired
    UserBetService userBetService;
    @Autowired
    NimbusJwtEncoder jwtEncoder;
    @Autowired
    CryptoTransactionRepository cryptoTransactionRepository;
    @Autowired
    BetTransactionRepository betTransactionRepository;


    public User createAdminUser() {
        return userRepository.save(
                User.builder()
                        .email(UUID.randomUUID().toString())
                        .enabled(true)
                        .authority(Authority.ROLE_ADMIN)
                        .build()
        );
    }

    public User createUser() {
        return User.builder()
                .email(UUID.randomUUID().toString())
                .ethBalance(INITIAL_USER_BALANCE)
                .enabled(true)
                .authority(Authority.ROLE_USER)
                .build();
    }

    public String createToken() {
        User user = userRepository.findById(1L).orElseThrow();
        Jwt token = jwtService.createToken(user);
        return token.getTokenValue();
    }

    public Bet createAndSaveBet(Long adminUserId, BetStatus betStatus) {
        return betRepository.save(betConverter.convert(
                BetDto.builder()
                        .title("title")
                        .status(betStatus)
                        .adminUserId(adminUserId)
                        .ethTicketPrice("0.1")
                        .build())
        );
    }

    public BetTransactionDto createBetTransactionDto(Long userId, Long betId) {
        int randomIdx = RANDOM.nextInt(2);
        Outcome[] outcomes = Outcome.values();
        return BetTransactionDto.builder()
                .userId(userId)
                .betId(betId)
                .predictedOutcome(outcomes[randomIdx])
                .build();
    }

    public List<Long> createUsersLinkedToBetAndGetUserIds(Bet bet) {
        return Stream.generate(() -> createUserWithLinkedBet(bet)).parallel()
                .limit(USER_LIMIT_PER_TEST)
                .toList();
    }

    public CryptoTransaction createCryptoTransaction(User user) {
        int randomIdx = RANDOM.nextInt(2);
        Type[] types = Type.values();
        int stateRandomIdx = RANDOM.nextInt(3);
        State[] states = State.values();
        return cryptoTransactionRepository.save(
                CryptoTransaction.builder()
                        .hash(UUID.randomUUID().toString())
                        .user(user)
                        .state(states[stateRandomIdx])
                        .type(types[randomIdx])
                        .ethAmount(BigDecimal.ONE)
                        .build()
        );
    }

    public UserTransactionDto createUserTransactionDto(Long userId, Type type) {
        return UserTransactionDto.builder()
                .hash(UUID.randomUUID().toString())
                .userId(userId)
                .type(type)
                .state(State.PROCESSING)
                .build();
    }

    private Long createUserWithLinkedBet(Bet bet) {
        Long userId = userRepository.save(createUser()).getId();
        userBetService.linkGameToUser(userId, bet.getId());
        return userId;
    }
}