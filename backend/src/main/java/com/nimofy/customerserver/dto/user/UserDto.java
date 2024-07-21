package com.nimofy.customerserver.dto.user;

import com.nimofy.customerserver.dto.bet.BetDto;
import com.nimofy.customerserver.model.user.Authority;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

import static com.nimofy.customerserver.dto.UpdateType.ACCOUNT_INFORMATION;

@Data
@Builder
public class UserDto {
    private Long userId;
    private String token;
    private String tokenSalt;
    private String ethBalance;
    private String totalEthProfitEarned;
    private Authority role;
    private Instant tokenExpiresAt;
    private List<BetDto> betDtos;

    public String toJson() {
        return String.format("""
                        {
                            "updateType": "%s",
                            "ethBalance": "%s",
                            "totalEthProfitEarned": "%s",
                            "role": "%s"
                        }
                        """,
                ACCOUNT_INFORMATION.name(),
                ethBalance,
                totalEthProfitEarned,
                role
        );
    }
}