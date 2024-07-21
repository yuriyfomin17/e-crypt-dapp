package com.nimofy.customerserver.converter.rest;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimofy.customerserver.converter.ContainerStarter;
import com.nimofy.customerserver.converter.testutil.TestUtilService;
import com.nimofy.customerserver.repository.user.UserRepository;
import com.nimofy.customerserver.rest.AuthController;
import com.nimofy.customerserver.service.auth.AuthService;
import com.nimofy.customerserver.service.auth.JwtService;
import com.nimofy.customerserver.util.CryptographyUtil;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.ParseException;

import static com.nimofy.customerserver.converter.testutil.TestUtilService.LOCALHOST;
import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Slf4j
public class AuthControllerTest extends ContainerStarter {

    private static final String LOGIN_PATH = "/v1/all-bet/api/auth/login";
    @Autowired
    TestUtilService testUtilService;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CryptographyUtil cryptographyUtil;
    @Autowired
    AuthService authService;
    @Autowired
    AuthController authController;
    @Mock
    AuthService mockedAuthService;
    @Mock
    JwtService mockedJwtService;

    @LocalServerPort
    private Integer port;

    @Mock
    private CryptographyUtil mockedCryptographyUtil;


    @BeforeEach
    void setUp() {
        RestAssured.baseURI = LOCALHOST + port;
    }


    @Test
    @DisplayName("should return unauthorized when retrieved user is null")
    void login_shouldReturnUnauthorizedWhenUserRetrievedIsNull() throws ParseException {
        // given
        when(mockedCryptographyUtil.isNotExpired(any(JWTClaimsSet.class))).thenReturn(true);
        when(mockedCryptographyUtil.isAudienceValid(anyList(), anyString())).thenReturn(true);
        when(mockedCryptographyUtil.isEmailVerified(any(JWTClaimsSet.class))).thenReturn(true);
        ReflectionTestUtils.setField(jwtService, "cryptographyUtil", mockedCryptographyUtil);
        String googleToken = "mocked incorrect token";
        // when then
        loginCheckResponse(googleToken, HttpStatus.UNAUTHORIZED, LOGIN_PATH);
    }

    @Test
    @DisplayName("should return 401 if google email is null")
    void login_shouldReturnUnauthorizedWhenGoogleEmailIsNull(){
        //given
        when(mockedAuthService.retrieveEmail(anyString())).thenReturn(null);
        when(mockedJwtService.verifyGoogleToken(anyString())).thenReturn(true);
        ReflectionTestUtils.setField(authController, "authService", mockedAuthService);
        ReflectionTestUtils.setField(authController, "jwtService", mockedJwtService);

        String googleToken = "mocked incorrect token";
        // when then
        loginCheckResponse(googleToken, HttpStatus.UNAUTHORIZED, LOGIN_PATH);

        ReflectionTestUtils.setField(authController, "authService", authService);
        ReflectionTestUtils.setField(authController, "jwtService", jwtService);
    }

    private ValidatableResponse loginCheckResponse(String token, HttpStatus expectedHttpCode, String path) {
        return given().header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .assertThat()
                .statusCode(expectedHttpCode.value());
    }

}