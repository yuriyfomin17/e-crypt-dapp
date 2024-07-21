package com.nimofy.customerserver.service.auth;


import com.nimbusds.jwt.SignedJWT;
import com.nimofy.customerserver.dto.user.UserDto;
import com.nimofy.customerserver.model.user.Authority;
import com.nimofy.customerserver.model.user.User;
import com.nimofy.customerserver.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String EMAIL = "email";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EntityManager entityManager;

    @Transactional
    public boolean checkOrLinkUserWalletAddress(Long userId, String newWalletAddress){
        User user = userRepository.findByIdWithLock(userId).orElse(null);
        if (user == null){
            return false;
        }
        if (user.getWalletAddress() == null){
            user.setWalletAddress(newWalletAddress);
            return true;
        }
        return StringUtils.equalsIgnoreCase(user.getWalletAddress(), newWalletAddress);
    }

    @Transactional
    public User getOrCreateUser(String userEmail) {
        return userRepository
                .findByEmailFetchBets(userEmail)
                .orElseGet(() -> createNewUser(userEmail));
    }

    public Jwt generateUserToken(User user) {
        log.info("Generating token for userId [{}]", user.getId());
        return jwtService.createToken(user);
    }

    @Transactional
    public UserDto refreshUserToken(Long userId, String oldPassword) {
        log.info("Refreshing token for userId [{}]", userId);
        User user = userRepository.findByIdWithLock(userId).orElseThrow(() -> new RuntimeException(String.format("User with %d does not exist", userId)));
        if (!StringUtils.equals(user.getPassword(), oldPassword)) {
            log.error("could not refresh token for userId [{}] ", userId);
            return null;
        }
        String newPassword = UUID.randomUUID().toString();
        user.setPassword(newPassword);
        Jwt newToken = jwtService.createToken(user);
        return UserDto.builder()
                .userId(userId)
                .ethBalance(user.getEthBalance().toPlainString())
                .totalEthProfitEarned(user.getTotalEthProfitEarned().toPlainString())
                .token(newToken.getTokenValue())
                .tokenSalt(user.getPassword())
                .tokenExpiresAt(newToken.getExpiresAt())
                .role(user.getAuthority())
                .build();
    }

    @Transactional
    public void updatePassword(Long userId) {
        User userReference = entityManager.getReference(User.class, userId);
        String newPassword = UUID.randomUUID().toString();
        userReference.setPassword(newPassword);
    }

    public String retrieveEmail(String googleToken) {
        try {
            SignedJWT parsedToken = SignedJWT.parse(googleToken);
            return parsedToken.getJWTClaimsSet().getStringClaim(EMAIL);
        } catch (ParseException e) {
            log.error("could not parse email in google token");
            return null;
        }
    }

    private User createNewUser(String email) {
        return userRepository.save(User.builder()
                .enabled(true)
                .email(email.toLowerCase())
                .authority(Authority.ROLE_USER)
                .password(UUID.randomUUID().toString())
                .build()
        );
    }
}