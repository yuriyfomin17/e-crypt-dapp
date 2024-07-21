package com.nimofy.customerserver.rest;

import com.nimofy.customerserver.converter.BetConverter;
import com.nimofy.customerserver.converter.UserConverter;
import com.nimofy.customerserver.dto.user.UserDto;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.service.auth.AuthService;
import com.nimofy.customerserver.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/all-bet/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LogoutHandler logoutHandler;
    private final AuthService authService;
    private final UserConverter userConverter;
    private final BetConverter betConverter;
    private final JwtService jwtService;

    @GetMapping("/login")
    public ResponseEntity<UserDto> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String googleToken) {
        boolean isTokenVerified = jwtService.verifyGoogleToken(googleToken);
        if (!isTokenVerified){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = authService.retrieveEmail(googleToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = authService.getOrCreateUser(userEmail);
        Jwt jwt = authService.generateUserToken(user);
        UserDto userDto = userConverter.convert(user, jwt);
        userDto.setBetDtos(user.getBets().stream().map(betConverter::convert).toList());
        return ResponseEntity.ok(userDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/logout")
    public ResponseEntity<HttpStatus> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.ok().build();
    }
}