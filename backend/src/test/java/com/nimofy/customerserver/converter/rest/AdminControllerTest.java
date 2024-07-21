package com.nimofy.customerserver.converter.rest;

import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.converter.ContainerStarter;
import com.nimofy.customerserver.converter.testutil.TestUtilService;
import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.response.ResponseType;
import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.bet.BetStatus;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.bet.BetRepository;
import com.nimofy.customerserver.repository.transaction.bet.BetTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.service.bet.PaymentService;
import com.nimofy.customerserver.service.payment.BigDecimalCalculator;
import com.nimofy.customerserver.service.users.UserBetService;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nimofy.customerserver.converter.testutil.TestUtilService.*;
import static com.nimofy.customerserver.dto.response.ResponseMessage.*;
import static com.nimofy.customerserver.model.bet.BetStatus.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Slf4j
public class AdminControllerTest extends ContainerStarter {

    @Autowired
    BetConverter betConverter;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BetRepository betRepository;
    @Autowired
    BetTransactionRepository betTransactionRepository;
    @Autowired
    UserBetService userBetService;
    @Autowired
    TaskExecutor cryptoTaskExecutor;
    @Autowired
    BigDecimalCalculator bigDecimalCalculator;
    @Autowired
    PaymentService paymentService;
    @Autowired
    TestUtilService testUtilService;
    @LocalServerPort
    private Integer port;
    @Value("${nimofy.user.id}")
    private Long nimofyId;
    @Value("${eth.calculation.scale}")
    private Integer SCALE;

    private static Stream<Arguments> mockBetIncorrectBetDtos() {
        return Stream.of(
                Arguments.of(
                        BetDto.builder().build(),
                        ResponseDto.builder().responseType(MISSING_TITLE_ERROR).type(MISSING_TITLE_ERROR.getResponseType()).build(),
                        "should return missing title error message"
                ),
                Arguments.of(
                        BetDto.builder().title("").build(),
                        ResponseDto.builder().responseType(MISSING_TITLE_ERROR).type(MISSING_TITLE_ERROR.getResponseType()).build(),
                        "should return missing title error message when empty"
                ),
                Arguments.of(
                        BetDto.builder().title("title").ethTicketPrice("not numeric ticket price").build(),
                        ResponseDto.builder().responseType(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR).type(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR.getResponseType()).build(),
                        "should return not numeric error message or greater then zero when not numeric"
                ),
                Arguments.of(
                        BetDto.builder().title("title").ethTicketPrice("0....0").build(),
                        ResponseDto.builder().responseType(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR).type(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR.getResponseType()).build(),
                        "should return not numeric error message or greater then zero when not numeric"
                ),
                Arguments.of(
                        BetDto.builder().title("title").ethTicketPrice("-0.05").build(),
                        ResponseDto.builder().responseType(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR).type(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR.getResponseType()).build(),
                        "should return not numeric error message or greater then zero when smaller then zero"
                ),
                Arguments.of(
                        BetDto.builder().title("title").ethTicketPrice("0.000").build(),
                        ResponseDto.builder().responseType(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR).type(NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR.getResponseType()).build(),
                        "should return not numeric error message or greater then zero when equal to zero"
                )
        );
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = LOCALHOST + port;
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("mockBetIncorrectBetDtos")
    void createAdminBet_shouldReturnCorrectResponse_whenGivenIncorrectBetDto(BetDto currentBetDto, ResponseDto expectedResponseDto, String description) {

        ResponseDto responseDto = createAdminBetCheckResponse(currentBetDto);
        assertThat(responseDto).isEqualTo(expectedResponseDto);
    }

    @Test
    @DisplayName("should create admin bet when given correct betDto")
    void createAdminBet_shouldReturnCorrectResponse_whenGivenCorrectBetDto() {
        // when
        BetDto currentBetDto = BetDto.builder().title("title").adminUserId(1L).ethTicketPrice("0.05").build();

        // given
        ResponseDto responseDto = createAdminBetCheckResponse(currentBetDto);

        // then
        assertThat(responseDto.getResponseType()).isEqualTo(ADMIN_GAME_CREATION);
        assertThat(responseDto.getType()).isEqualTo(ResponseType.SUCCESS);
        assertTrue(betRepository.existsById(responseDto.getBetDto().getId()));
    }

    @Test
    @DisplayName("should return bad request when bet status is not changed")
    void changeAdminBetStatus_shouldReturnBadRequestWhenBetDtoIsIncorrect() {
        // when
        User admin = testUtilService.createAdminUser();
        Bet savedBet = testUtilService.createAndSaveBet(admin.getId(), CREATED);
        BetDto savedBetDto = betConverter.convert(savedBet);

        // given then
        changeBetStatusCheckResponse(savedBetDto, HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(value = BetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "RESOLVED")
    @DisplayName("should change bet status as expected")
    void changeAdminBetStatus_shouldChangeBetStatusCorrectly(BetStatus betStatus) {
        // when
        User adminUser = testUtilService.createAdminUser();
        Bet savedBet = testUtilService.createAndSaveBet(adminUser.getId(), betStatus);
        BetDto savedBetDto = betConverter.convert(savedBet);
        savedBetDto.setStatus(nextValidStatus(betStatus));
        // given then
        BetDto savedBetDtoResponse = changeBetStatusCheckResponse(savedBetDto, HttpStatus.OK);
        assertNotNull(savedBetDtoResponse);
        switch (betStatus) {
            case CREATED -> assertThat(savedBetDtoResponse.getStatus()).isEqualTo(OPENED);
            case OPENED -> assertThat(savedBetDtoResponse.getStatus()).isEqualTo(CLOSED);
            case CLOSED, RESOLVED -> assertThat(savedBetDtoResponse.getStatus()).isEqualTo(RESOLVED);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Outcome.class, mode = EnumSource.Mode.EXCLUDE, names = "NOT_DETERMINED")
    @DisplayName("change bet status should process payout correctly")
    void changeAdminBetStatus_shouldProcessPayoutCorrectlyAndNotifyUsersWhenBetIsResolved(Outcome outcome) {
        // given
        User adminUser = testUtilService.createAdminUser();
        Bet savedBet = testUtilService.createAndSaveBet(adminUser.getId(), OPENED);

        List<Long> usersLinkedToBetGetIds = testUtilService.createUsersLinkedToBetAndGetUserIds(savedBet);
        Map<Long, BetTransaction> userIdBetTxMap = createUserTransactionsOnBetInAsyncManner(usersLinkedToBetGetIds, savedBet);
        BetDto savedBetDto = betConverter.convert(savedBet);
        savedBetDto.setStatus(nextValidStatus(RESOLVED));
        savedBetDto.setOutcome(outcome);
        User adminUserBeforeBetIsResolved = userRepository.findById(adminUser.getId()).orElseThrow();
        User nimofyUserBeforeBetIsResolved = userRepository.findById(nimofyId).orElseThrow();

        // given then
        BetDto savedBetDtoResponse = changeBetStatusCheckResponse(savedBetDto, HttpStatus.OK);

        // then
        assertNotNull(savedBetDtoResponse);
        assertThat(savedBetDtoResponse.getStatus()).isEqualTo(RESOLVED);
        assertThat(savedBetDtoResponse.getOutcome()).isEqualTo(outcome);

        BigDecimal totalEth = getTotalEth(outcome, userIdBetTxMap);
        BigDecimal adminProfit = bigDecimalCalculator.calculateAdminProfit(totalEth);
        BigDecimal nimofyProfit = bigDecimalCalculator.calculateNimofyProfit(totalEth);

        User nimofyUserAfterBetIsResolved = userRepository.findById(nimofyId).orElseThrow();
        User adminUserAfterBetIsResolved = userRepository.findById(adminUser.getId()).orElseThrow();


        assertThat(nimofyUserAfterBetIsResolved.getEthBalance()).isEqualTo(bigDecimalCalculator.add(nimofyProfit, nimofyUserBeforeBetIsResolved.getEthBalance()));
        assertThat(nimofyUserAfterBetIsResolved.getTotalEthProfitEarned()).isEqualTo(bigDecimalCalculator.add(nimofyProfit, nimofyUserBeforeBetIsResolved.getTotalEthProfitEarned()));

        assertThat(adminUserAfterBetIsResolved.getEthBalance()).isEqualTo(bigDecimalCalculator.add(adminProfit, adminUserBeforeBetIsResolved.getEthBalance()));
        assertThat(adminUserAfterBetIsResolved.getTotalEthProfitEarned()).isEqualTo(bigDecimalCalculator.add(adminProfit, adminUserBeforeBetIsResolved.getTotalEthProfitEarned()));

        checkUserBalanceAndTotalEthEarned(userIdBetTxMap, savedBetDtoResponse.getId());

        betTransactionRepository.deleteAll();
    }

    private BetDto changeBetStatusCheckResponse(BetDto savedBetDto, HttpStatus expectedHttpStatus) {
        ValidatableResponse validatableResponse = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + testUtilService.createToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(savedBetDto)
                .when()
                .put("/v1/all-bet/admin/bets/status")
                .then()
                .assertThat()
                .statusCode(expectedHttpStatus.value());

        if (expectedHttpStatus == HttpStatus.OK) {
            return validatableResponse.extract()
                    .as(BetDto.class);
        }
        return null;
    }


    private ResponseDto createAdminBetCheckResponse(BetDto currentBetDto) {
        return given().header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + testUtilService.createToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(currentBetDto)
                .when()
                .post("/v1/all-bet/admin/bets")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ResponseDto.class);
    }

    private void checkUserBalanceAndTotalEthEarned(Map<Long, BetTransaction> userIdBetTxMap, Long betId) {
        Bet currentBet = betRepository.findById(betId).orElseThrow();
        BigDecimal transactionProfit = paymentService.calculateTransactionProfit(currentBet);
        userIdBetTxMap.forEach((key, value) -> {
            User user = userRepository.findById(key).orElseThrow();
            if (currentBet.getOutcome() == value.getPredictedOutcome()) {
                assertThat(user.getEthBalance()).isEqualTo(bigDecimalCalculator.add(INITIAL_USER_BALANCE, transactionProfit));
                assertThat(user.getTotalEthProfitEarned()).isEqualTo(transactionProfit);
            } else {
                assertThat(user.getEthBalance()).isEqualTo(bigDecimalCalculator.subtract(INITIAL_USER_BALANCE, value.getEthAmount()));
                assertThat(user.getTotalEthProfitEarned()).isEqualTo(BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN));
            }
        });
    }

    private BigDecimal getTotalEth(Outcome outcome, Map<Long, BetTransaction> userIdBetTxMap) {
        return userIdBetTxMap.values().stream()
                .filter(betTransaction -> betTransaction.getPredictedOutcome() != outcome)
                .map(BetTransaction::getEthAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, BetTransaction> createUserTransactionsOnBetInAsyncManner(List<Long> userIds, Bet bet) {
        userIds.stream()
                .map(userId -> CompletableFuture.supplyAsync(() -> userBetService.isTransactionCreated(testUtilService.createBetTransactionDto(userId, bet.getId())), cryptoTaskExecutor))
                .toList().forEach(CompletableFuture::join);
        return betTransactionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(tx -> tx.getUser().getId(), Function.identity()));
    }

    private BetStatus nextValidStatus(BetStatus betStatus) {
        return switch (betStatus) {
            case CREATED -> OPENED;
            case OPENED -> CLOSED;
            case CLOSED, RESOLVED -> RESOLVED;
        };
    }
}