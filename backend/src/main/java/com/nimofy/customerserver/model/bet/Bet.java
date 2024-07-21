package com.nimofy.customerserver.model.bet;


import com.nimofy.customerserver.model.Outcome;
import com.nimofy.customerserver.model.transaction.bet.BetTransaction;
import com.nimofy.customerserver.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Data
@Builder
@ToString(exclude = {"participants", "betTransactions"})
@Entity
@Table(name = "bets")
@AllArgsConstructor
@NoArgsConstructor
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;
    @Column(nullable = false)
    private String title;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status = BetStatus.CREATED;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Outcome outcome = Outcome.NOT_DETERMINED;
    @Builder.Default
    @Column(nullable = false, name = "eth_win_stake_amount")
    private BigDecimal ethWinStakeAmount = BigDecimal.ZERO;
    @Builder.Default
    @Column(nullable = false, name = "eth_loss_stake_amount")
    private BigDecimal ethLossStakeAmount = BigDecimal.ZERO;
    @Builder.Default
    @Column(nullable = false, name = "bets_for_win_count")
    private Long betsForWinCount = 0L;
    @Builder.Default
    @Column(nullable = false, name = "bets_for_loss_count")
    private Long betsForLossCount = 0L;
    @Builder.Default
    @Column(nullable = false, name = "eth_profit")
    private BigDecimal ethProfit = BigDecimal.ZERO;
    @Column(nullable = false, name = "eth_ticket_price")
    private BigDecimal ethTicketPrice;
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    @ManyToMany(mappedBy = "bets")
    private Set<User> participants = new HashSet<>();
    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "bet")
    private List<BetTransaction> betTransactions = new ArrayList<>();
    @Builder.Default
    @Column(nullable = false, name = "updated_by_admin_at")
    private LocalDateTime updatedByAdminAt = LocalDateTime.now(ZoneOffset.UTC);
    @Builder.Default
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bet bet)) return false;
        return Objects.equals(id, bet.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}