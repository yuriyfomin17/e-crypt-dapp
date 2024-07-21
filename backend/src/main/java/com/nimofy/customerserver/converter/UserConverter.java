package com.nimofy.customerserver.converter;

import com.nimofy.customerserver.dto.user.UserDto;
import com.nimofy.customerserver.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConverter {

    public UserDto convert(User user, Jwt jwt) {
        UserDto userDto = convert(user);
        userDto.setToken(jwt.getTokenValue());
        userDto.setTokenExpiresAt(jwt.getExpiresAt());
        return userDto;
    }

    public UserDto convert(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .role(user.getAuthority())
                .ethBalance(user.getEthBalance().toPlainString())
                .totalEthProfitEarned(user.getTotalEthProfitEarned().toPlainString())
                .tokenSalt(user.getPassword())
                .build();
    }
}