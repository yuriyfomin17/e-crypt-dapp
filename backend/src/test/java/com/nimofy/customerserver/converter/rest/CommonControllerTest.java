package com.nimofy.customerserver.converter.rest;

import com.nimofy.customerserver.converter.ContainerStarter;
import com.nimofy.customerserver.converter.testutil.TestUtilService;
import com.nimofy.customerserver.dto.crypto.UserTransactionDto;
import com.nimofy.customerserver.dto.response.ResponseDto;
import com.nimofy.customerserver.dto.response.ResponseMessage;
import com.nimofy.customerserver.dto.response.ResponseType;
import com.nimofy.customerserver.dto.user.BalanceDto;
import com.nimofy.customerserver.dto.user.UserDto;
import com.nimofy.customerserver.model.bet.Bet;
import com.nimofy.customerserver.model.transaction.crypto.CryptoTransaction;
import com.nimofy.customerserver.model.transaction.crypto.Type;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.bet.BetRepository;
import com.nimofy.customerserver.repository.transaction.bet.BetTransactionRepository;
import com.nimofy.customerserver.repository.transaction.crypto.CryptoTransactionRepository;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.rest.CommonController;
import com.nimofy.customerserver.service.auth.JwtService;
import com.nimofy.customerserver.service.payment.CryptoPaymentService;
import com.nimofy.customerserver.service.payment.Web3Service;
import com.nimofy.customerserver.service.users.UserBetService;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.*;

import static com.nimofy.customerserver.converter.testutil.TestUtilService.BEARER_PREFIX;
import static com.nimofy.customerserver.converter.testutil.TestUtilService.LOCALHOST;
import static com.nimofy.customerserver.dto.response.ResponseMessage.NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR;
import static com.nimofy.customerserver.dto.response.ResponseMessage.WITHDRAW_TRANSACTION_CREATED;
import static com.nimofy.customerserver.dto.response.ResponseType.SUCCESS;
import static com.nimofy.customerserver.model.bet.BetStatus.OPENED;
import static com.nimofy.customerserver.service.auth.JwtService.ROLES;
import static io.restassured.RestAssured.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Slf4j
public class CommonControllerTest extends ContainerStarter {

    private static final String REFRESH_TOKEN_PATH = "/v1/all-bet/common/refresh";
    private static final String LINK_WALLET_PATH = "/v1/all-bet/common/linkWallet";
    private static final String ETH_USD_PRICE_PATH = "/v1/all-bet/common/price/usd";
    private static final String TEN_LATEST_CRYPTO_TXS_PATH = "/v1/all-bet/common/transactions";
    private static final String TEN_LATEST_GAME_TXS_PATH = "/v1/all-bet/common/game/transactions";
    private static final String USER_BETS_PATH = "/v1/all-bet/common/bets";
    private static final String CREATE_DEPOSIT_PATH = "/v1/all-bet/common/create/deposit";
    private static final String CREATE_WITHDRAW_PATH = "/v1/all-bet/common/create/withdraw";
    private static final String GET_USER_DATA_PATH = "/v1/all-bet/common/account/userData";
    private static final String EMPTY_BODY = """
            """;

    @Autowired
    TestUtilService testUtilService;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NimbusJwtEncoder jwtEncoder;
    @Autowired
    CryptoTransactionRepository cryptoTransactionRepository;
    @Autowired
    UserBetService userBetService;
    @Autowired
    BetTransactionRepository betTransactionRepository;
    @Autowired
    CommonController commonController;
    @Autowired
    CryptoPaymentService cryptoPaymentService;
    @Autowired
    BetRepository betRepository;
    @Mock
    CryptoPaymentService mockedCryptoPaymentService;
    @Mock
    Web3Service mockedWeb3Service;
    @Autowired
    Web3Service web3Service;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = LOCALHOST + port;
    }

    @Test
    @DisplayName("should link user to wallet if user exists. Should return ok response " +
            "if wallet is already linked and wallets are identical ")
    void linkWallet_shouldLinkUserToWalletCorrectly() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        String mockedAddress = UUID.randomUUID().toString();

        // when
        putRequestCheckResponse(user, token, mockedAddress, HttpStatus.OK, LINK_WALLET_PATH);

        // then
        User userAfterWalletUpdate = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals(userAfterWalletUpdate.getWalletAddress(), mockedAddress);

        // then
        putRequestCheckResponse(user, token, mockedAddress, HttpStatus.OK, LINK_WALLET_PATH);
        // then
        String newWallet = UUID.randomUUID().toString();
        putRequestCheckResponse(user, token, newWallet, HttpStatus.BAD_REQUEST, LINK_WALLET_PATH);

        User notSavedUser = testUtilService.createUser();
        notSavedUser.setId(Long.MAX_VALUE);
        String notSavedUserToken = jwtService.createToken(notSavedUser).getTokenValue();
        putRequestCheckResponse(notSavedUser, notSavedUserToken, UUID.randomUUID().toString(), HttpStatus.BAD_REQUEST, LINK_WALLET_PATH);
    }

    @Test
    @DisplayName("should not refresh user token if format is invalid")
    void refreshToken_shouldNotRefreshUserTokenIfTokenFormatIsInvalid() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();

        // when then
        postRequestCheckResponseAndGetResult(token, user, EMPTY_BODY, HttpStatus.UNAUTHORIZED, REFRESH_TOKEN_PATH, UserDto.class);
        postRequestCheckResponseAndGetResult(BEARER_PREFIX + mockInvalidToken(user).getTokenValue(), user, EMPTY_BODY, HttpStatus.BAD_REQUEST, REFRESH_TOKEN_PATH, UserDto.class);
    }

    @Test
    @DisplayName("should refresh token successfully and not deny refreshing if user token is invalid or somebody has stolen old valid token")
    void refreshToken_shouldRefreshTokenSuccessfully() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        // when then
        UserDto refreshedUserDto = postRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user, EMPTY_BODY, HttpStatus.OK, REFRESH_TOKEN_PATH, UserDto.class);
        Assertions.assertNotNull(refreshedUserDto);

        // given
        User refreshedUser = userRepository.findById(refreshedUserDto.getUserId()).orElseThrow();
        Assertions.assertEquals(refreshedUser.getPassword(), refreshedUserDto.getTokenSalt());
        // when then
        postRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user, EMPTY_BODY, HttpStatus.BAD_REQUEST, REFRESH_TOKEN_PATH, UserDto.class);
    }

    @Test
    @DisplayName("should fetch eth usd price successfully")
    void getApproximateEthUsdPrice_shouldFetchEthUsdPrice() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        Mockito.when(mockedWeb3Service.getEthUsdRate()).thenReturn(500.0);
        ReflectionTestUtils.setField(commonController, "web3Service", mockedWeb3Service);
        // when then
        Double ethUsdPrice = getRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user.getId(), HttpStatus.OK, ETH_USD_PRICE_PATH, Double.class);
        ReflectionTestUtils.setField(commonController, "web3Service", web3Service);
        Assertions.assertNotNull(ethUsdPrice);
    }

    @Test
    @DisplayName("should fetch successfully ten latest transactions")
    @SuppressWarnings("unchecked")
    void getTenLatestCryptoTransactions_shouldFetchTenLatestCryptoTransactions() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        CryptoTransaction cryptoTransaction = testUtilService.createCryptoTransaction(user);
        // when then
        List<?> cryptoTxsListJson = getRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user.getId(), HttpStatus.OK, TEN_LATEST_CRYPTO_TXS_PATH, List.class);
        LinkedHashMap<String, Object> txMap = (LinkedHashMap<String, Object>) cryptoTxsListJson.get(0);

        Assertions.assertNotNull(txMap);
        Assertions.assertEquals(txMap.get("hash"), cryptoTransaction.getHash());
    }

    @Test
    @DisplayName("should fetch ten latest game transactions")
    void getTenLatestGameTransactions_shouldFetchTenLatestGameTransactions() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        Bet savedBet = testUtilService.createAndSaveBet(user.getId(), OPENED);

        userBetService.isTransactionCreated(testUtilService.createBetTransactionDto(user.getId(), savedBet.getId()));
        // when then
        List<?> gameTxsListJson = getRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user.getId(), HttpStatus.OK, TEN_LATEST_GAME_TXS_PATH, List.class);
        Assertions.assertEquals(gameTxsListJson.size(), 1);
        betTransactionRepository.deleteAll();

    }

    @Test
    @DisplayName("should fetch user bets successfully")
    @SuppressWarnings("unchecked")
    void fetchUserBets_shouldFetchUserBetsSuccessfully() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        Bet savedBet = testUtilService.createAndSaveBet(Long.MAX_VALUE, OPENED);
        userBetService.linkGameToUser(user.getId(), savedBet.getId());
        // when then
        List<?> userBets = getRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user.getId(), HttpStatus.OK, USER_BETS_PATH, List.class);
        Assertions.assertEquals(userBets.size(), 1);
        Map<String, Object> betMap = (Map<String, Object>) userBets.get(0);
        Assertions.assertEquals(NumberUtils.toLong(betMap.get("id").toString()), savedBet.getId());
    }

    @Test
    @DisplayName("should create deposit successfully")
    void createDepositTx_shouldCreateDepositSuccessfully() {
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        UserTransactionDto userTransactionDto = testUtilService.createUserTransactionDto(user.getId(), Type.DEPOSIT);
        // when then
        ResponseDto responseDto = postRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user, userTransactionDto, HttpStatus.OK, CREATE_DEPOSIT_PATH, ResponseDto.class);
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(responseDto.getResponseType(), ResponseMessage.DEPOSIT_TRANSACTION_CREATED);
        Assertions.assertEquals(responseDto.getType(), ResponseType.SUCCESS);
        Assertions.assertTrue(cryptoTransactionRepository.existsByHash(userTransactionDto.getHash()));
    }

    @Test
    @DisplayName("should return correct response when transaction is less then zero")
    void createWithdrawCryptoTransaction_shouldReturnCorrectResponseGivenTransactionIsLessThenZero(){
        // given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        UserTransactionDto userTransactionDto = testUtilService.createUserTransactionDto(user.getId(), Type.WITHDRAW);
        userTransactionDto.setEthAmount("fdjsal;fjda");
        // when then
        ResponseDto responseDto = postRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user, userTransactionDto, HttpStatus.OK, CREATE_WITHDRAW_PATH, ResponseDto.class);
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(responseDto.getResponseType(), NUMBER_SHOULD_BE_NUMERIC_AND_GREATER_THEN_ZERO_ERROR);
    }

    @Test
    @DisplayName("should return correct response when transaction is good")
    void createWithdrawCryptoTransaction_shouldReturnCorrectResponseWhenTransactionIsGood(){
        //given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        ResponseDto mockedSuccessWithdrawResponse = mockSuccessWithdrawResponse();
        UserTransactionDto userTransactionDto = testUtilService.createUserTransactionDto(user.getId(), Type.WITHDRAW);
        userTransactionDto.setEthAmount("1.0");
        Mockito.when(mockedCryptoPaymentService.isSignatureIsVerified(userTransactionDto)).thenReturn(true);
        Mockito.when(mockedCryptoPaymentService.createWithdrawTxUpdateCache(userTransactionDto)).thenReturn(mockedSuccessWithdrawResponse);
        ReflectionTestUtils.setField(commonController, "cryptoPaymentService", mockedCryptoPaymentService);
        // when then
        ResponseDto responseDto = postRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user, userTransactionDto, HttpStatus.OK, CREATE_WITHDRAW_PATH, ResponseDto.class);
        Assertions.assertEquals(responseDto, mockedSuccessWithdrawResponse);
        ReflectionTestUtils.setField(commonController, "cryptoPaymentService", cryptoPaymentService);
    }

    @Test
    @DisplayName("should return bas response if transaction is not verified")
    void createWithdrawCryptoTransaction_shouldReturnBadResponseIfTransactionIsNotVerified(){
        //given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        ResponseDto mockedSuccessWithdrawResponse = mockSuccessWithdrawResponse();
        UserTransactionDto userTransactionDto = testUtilService.createUserTransactionDto(user.getId(), Type.WITHDRAW);
        userTransactionDto.setEthAmount("1.0");

        Mockito.when(mockedCryptoPaymentService.isSignatureIsVerified(userTransactionDto)).thenReturn(false);
        Mockito.when(mockedCryptoPaymentService.createWithdrawTxUpdateCache(userTransactionDto)).thenReturn(mockedSuccessWithdrawResponse);
        ReflectionTestUtils.setField(commonController, "cryptoPaymentService", mockedCryptoPaymentService);
        // when then
        postRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user, userTransactionDto, HttpStatus.BAD_REQUEST, CREATE_WITHDRAW_PATH, ResponseDto.class);
        ReflectionTestUtils.setField(commonController, "cryptoPaymentService", cryptoPaymentService);
    }

    @Test
    @DisplayName("should get user data successfully")
    void getUserData_shouldGetUserDataSuccessfully(){
        //given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        // when then
        BalanceDto balanceDto = getRequestCheckResponseAndGetResult(BEARER_PREFIX + token, user.getId(), HttpStatus.OK, GET_USER_DATA_PATH, BalanceDto.class);
        Assertions.assertNotNull(balanceDto);
        Assertions.assertEquals(user.getPassword(), balanceDto.getTokenSalt());
    }

    @Test
    @DisplayName("should unlink user from bet successfully")
    void unlinkBet_shouldUnlinkUserFromBetSuccessfully(){
        //given
        User user = userRepository.save(testUtilService.createUser());
        String token = jwtService.createToken(user).getTokenValue();
        Bet savedBet = testUtilService.createAndSaveBet(Long.MAX_VALUE, OPENED);

        //when then
        userBetService.linkGameToUser(user.getId(), savedBet.getId());
        Bet bet = betRepository.fetchBetParticipants(savedBet.getId()).orElseThrow();
        Assertions.assertTrue(bet.getParticipants().contains(user));
        given().headers(AUTHORIZATION, BEARER_PREFIX +  token, "userId", user.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put("/v1/all-bet/common/unlink/bet/" + user.getId() + "/" + savedBet.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract();

        Assertions.assertNull(betRepository.fetchBetParticipants(savedBet.getId()).orElse(null));
    }



    private <T> T getRequestCheckResponseAndGetResult(String token, Long userId, HttpStatus expectedStatus, String path, Class<T> clazz) {
        return given().headers(AUTHORIZATION, token, "userId", userId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .assertThat()
                .statusCode(expectedStatus.value())
                .extract()
                .as(clazz);
    }

    private <T, B> T postRequestCheckResponseAndGetResult(String token, User user, B body, HttpStatus expectedStatus, String path, Class<T> clazz) {
        ValidatableResponse validatableResponse = given().headers(AUTHORIZATION, token, "userId", user.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when()
                .post(path)
                .then()
                .assertThat()
                .statusCode(expectedStatus.value());
        if (expectedStatus == HttpStatus.OK) {
            return validatableResponse.extract()
                    .as(clazz);
        }
        return null;
    }

    private void putRequestCheckResponse(User user, String token, String newWallet, HttpStatus expectedStatus, String path) {
        given()
                .headers(AUTHORIZATION, BEARER_PREFIX + token, "Address", newWallet, "userId", user.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(path)
                .then()
                .assertThat()
                .statusCode(expectedStatus.value());
    }

    private ResponseDto mockSuccessWithdrawResponse(){
        return ResponseDto.builder()
                .responseType(WITHDRAW_TRANSACTION_CREATED)
                .type(SUCCESS)
                .build();
    }

    private Jwt mockInvalidToken(User user) {
        Instant now = Instant.now();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .claim(ROLES, user.getAuthority().name())
                .build())
        );
    }
}